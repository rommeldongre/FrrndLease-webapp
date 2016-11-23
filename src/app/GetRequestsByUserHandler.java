package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import connect.Connect;
import pojos.GetRequestsByUserReqObj;
import pojos.GetRequestsByUserResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetRequestsByUserHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetRequestsByUserHandler.class.getName());

	private static GetRequestsByUserHandler instance = null;

	public static GetRequestsByUserHandler getInstance() {
		if (instance == null)
			instance = new GetRequestsByUserHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		
		LOGGER.info("Inside Process Method of GetRequestsByUserHandler");
		
		GetRequestsByUserReqObj rq = (GetRequestsByUserReqObj) req;
		GetRequestsByUserResObj rs = new GetRequestsByUserResObj();
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		try {
			String userId = rq.getUserId();
			int offset = rq.getCookie();
			
			String sql = "SELECT tb1.request_id, tb1.request_lastmodified, tb1.request_message, tb2.*, tb3.*, (CASE WHEN tb2.item_user_id=tb4.friend_id AND tb4.friend_user_id=tb1.request_requser_id THEN true ELSE false END) AS isFriend, ( 6371 * acos( cos( radians(tb2.item_lat) ) * cos( radians( tb5.user_lat ) ) * cos( radians( tb5.user_lng ) - radians(tb2.item_lng) ) + sin( radians(tb2.item_lat) ) * sin( radians( tb5.user_lat ) ) ) ) AS distance FROM requests tb1 INNER JOIN items tb2 on tb1.request_item_id = tb2.item_id INNER JOIN users tb3 on tb2.item_user_id = tb3.user_id LEFT JOIN (SELECT * FROM friends WHERE friend_user_id=?) tb4 ON tb2.item_user_id = tb4.friend_id INNER JOIN users tb5 ON tb1.request_requser_id = tb5.user_id WHERE tb1.request_status=? AND tb1.request_requser_id=? ORDER BY tb1.request_lastmodified LIMIT ?, 1";
			ps1 = hcp.prepareStatement(sql);

			ps1.setString(1, userId);
			ps1.setString(2, "Active");
			ps1.setString(3, userId);
			ps1.setInt(4, offset);

			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				// Item Details
				rs.setItemId(rs1.getInt("item_id"));
				rs.setTitle(rs1.getString("item_name"));
				rs.setDescription(rs1.getString("item_desc"));
				rs.setCategory(rs1.getString("item_category"));
				rs.setLeaseTerm(rs1.getString("item_lease_term"));
				rs.setInsurance(rs1.getInt("item_lease_value"));
				rs.setPrimaryImageLink(rs1.getString("item_primary_image_link"));
				rs.setUid(rs1.getString("item_uid"));
				
				// Request Details
				rs.setRequestId(rs1.getInt("request_id"));
				rs.setRequestDate(rs1.getString("request_lastmodified"));
				rs.setMessage(rs1.getString("request_message"));
				
				// Owner Details
				rs.setOwnerId(rs1.getString("user_id"));
				rs.setOwnerName(rs1.getString("user_full_name"));
				rs.setOwnerProfilePic(rs1.getString("user_profile_picture"));
				rs.setOwnerLocality(rs1.getString("user_locality"));
				rs.setOwnerSublocality(rs1.getString("user_sublocality"));
				rs.setDistance(rs1.getFloat("distance"));
				rs.setFriend(rs1.getBoolean("isFriend"));
				
				rs.setOffset(offset + 1);
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
			}else{
				rs.setCode(FLS_END_OF_DB);
				rs.setMessage(FLS_END_OF_DB_M);
			}
			
		} catch (Exception e) {
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		} finally {
			try{
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
				rs.setCode(FLS_INVALID_OPERATION);
				rs.setMessage(FLS_END_OF_DB_M);
			}
		}

		LOGGER.info("Finished process method");
		// return the response
		return rs;

	}

	@Override
	public void cleanup() {
	}
}
