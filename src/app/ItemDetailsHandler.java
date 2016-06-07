package app;

import java.sql.PreparedStatement;

import java.sql.ResultSet;
import java.sql.SQLException;

import adminOps.Response;
import connect.Connect;
import pojos.ItemDetailsReqObj;
import pojos.ItemDetailsResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class ItemDetailsHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(ItemDetailsHandler.class.getName());

	private Response res = new Response();

	private static ItemDetailsHandler instance = null;

	public static ItemDetailsHandler getInstance() {
		if (instance == null)
			return new ItemDetailsHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub

		LOGGER.info("Inside Post Method");

		ItemDetailsReqObj rq = (ItemDetailsReqObj) req;

		ItemDetailsResObj rs = new ItemDetailsResObj();

		try {

			String itemDetailsSql = "SELECT * FROM items WHERE item_uid=?";
			LOGGER.info("Creating Statement");

			PreparedStatement ps = getConnectionFromPool().prepareStatement(itemDetailsSql);
			ps.setString(1, rq.getUid());
			LOGGER.info("Created statement...executing select from items query");

			ResultSet result = ps.executeQuery();
			LOGGER.info(result.toString());

			if (result.next()) {
				rs.setCode(FLS_SUCCESS);
				rs.setId(Integer.parseInt(result.getString("item_id")));
				rs.setTitle(result.getString("item_name"));
				rs.setCategory(result.getString("item_category"));
				rs.setDescription(result.getString("item_desc"));
				rs.setUserId(result.getString("item_user_id"));
				rs.setLeaseTerm(result.getString("item_lease_term"));
				rs.setStatus(result.getString("item_status"));
				rs.setImage(result.getString("item_image"));
				rs.setLeaseValue(Integer.parseInt(result.getString("item_lease_value")));
				rs.setUid(result.getString("item_uid"));
			} else {
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
			}
			
			
			String locality = "", sublocality = "";
			
			if(rs.getUserId() != "" || rs.getUserId() != null){
				LOGGER.info("Creating statement to select user location data.....");
				String userLocationData = "SELECT user_locality, user_sublocality FROM users WHERE user_id=?";
				
				LOGGER.info("Created statement...executing select from items query");
				PreparedStatement ps1 = getConnectionFromPool().prepareStatement(userLocationData);
				ps1.setString(1, rs.getUserId());
				
				ResultSet r1 = ps1.executeQuery();
				
				if(r1.next()){
					locality = r1.getString("user_locality");
					sublocality = r1.getString("user_sublocality");
				}
				
				rs.setLocality(locality);
				rs.setSublocality(sublocality);
				
				r1.close();
				ps1.close();
			}
			
			result.close();
			ps.close();

		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		}
		LOGGER.info("Finished process method ");
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
	}

}
