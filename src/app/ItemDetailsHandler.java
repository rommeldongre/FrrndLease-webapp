package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.ItemDetailsReqObj;
import pojos.ItemDetailsResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.FlsS3Bucket;

public class ItemDetailsHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(ItemDetailsHandler.class.getName());

	private static ItemDetailsHandler instance = null;

	public static ItemDetailsHandler getInstance() {
		if (instance == null)
			return new ItemDetailsHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside Post Method of ItemDetailsHandler");

		ItemDetailsReqObj rq = (ItemDetailsReqObj) req;
		ItemDetailsResObj rs = new ItemDetailsResObj();
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null, rs2 = null;
		
		String locality = "", sublocality = "";

		try {

			LOGGER.info("Creating sql statement for fetching items details");
			String itemDetailsSql = "SELECT * FROM items WHERE item_uid=?";

			ps1 = hcp.prepareStatement(itemDetailsSql);
			ps1.setString(1, rq.getUid());

			rs1 = ps1.executeQuery();

			if (rs1.next()) {
				rs.setId(Integer.parseInt(rs1.getString("item_id")));
				rs.setTitle(rs1.getString("item_name"));
				rs.setCategory(rs1.getString("item_category"));
				rs.setDescription(rs1.getString("item_desc"));
				rs.setUserId(rs1.getString("item_user_id"));
				rs.setLeaseTerm(rs1.getString("item_lease_term"));
				rs.setStatus(rs1.getString("item_status"));
				rs.setPrimaryImageLink(rs1.getString("item_primary_image_link"));
				rs.setLeaseValue(Integer.parseInt(rs1.getString("item_lease_value")));
				rs.setUid(rs1.getString("item_uid"));
				
				FlsS3Bucket s3Bucket = new FlsS3Bucket(rs1.getString("item_uid"));
				rs.setImageLinks(s3Bucket.getImagesLinks());
				
				rs.setCode(FLS_SUCCESS);
				
				if(rs.getUserId() != "" || rs.getUserId() != null){
					
					LOGGER.info("Creating statement to select user location data.....");
					String userLocationData = "SELECT user_locality, user_sublocality FROM users WHERE user_id=?";
					
					ps2 = hcp.prepareStatement(userLocationData);
					ps2.setString(1, rs.getUserId());
					
					rs2 = ps2.executeQuery();
					
					if(rs2.next()){
						locality = rs2.getString("user_locality");
						sublocality = rs2.getString("user_sublocality");
					}
					
					rs.setLocality(locality);
					rs.setSublocality(sublocality);
				}
				
			} else {
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
			}

		} catch (SQLException e) {
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (Exception e) {
			rs.setCode(FLS_INVALID_OPERATION);
			rs.setMessage(FLS_INVALID_OPERATION_M);
			e.printStackTrace();
		} finally {
			try{
				if(rs2 != null) rs2.close();
				if(ps2 != null) ps2.close();
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		LOGGER.info("Finished process method ");
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
	}

}
