package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.GetRequestsPlusReqObj;
import pojos.GetRequestsPlusResObj;
import pojos.ReqObj;
import pojos.RequestObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetRequestsPlusHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetRequestsPlusHandler.class.getName());

	private static GetRequestsPlusHandler instance = null;

	public static GetRequestsPlusHandler getInstance() {
		if (instance == null)
			return new GetRequestsPlusHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		LOGGER.info("Inside Process Method of GetRequestsPlusHandler");

		GetRequestsPlusReqObj rq = (GetRequestsPlusReqObj) req;
		GetRequestsPlusResObj rs = new GetRequestsPlusResObj();
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null, rs2 = null;

		try {
			int offset = rq.getCookie();
			
			String sqlGetRequestedItem = "SELECT tb2.*, tb3.user_locality, tb3.user_sublocality FROM requests tb1 INNER JOIN items tb2 ON tb1.request_item_id=tb2.item_id INNER JOIN users tb3 ON tb2.item_user_id=tb3.user_id WHERE tb1.request_status=? AND tb2.item_user_id=? GROUP BY tb1.request_item_id ORDER BY tb1.request_lastmodified desc LIMIT ?, 1";
			ps1 = hcp.prepareStatement(sqlGetRequestedItem);
			ps1.setString(1, "Active");
			ps1.setString(2, rq.getUserId());
			ps1.setInt(3, offset);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				
				int itemId = rs1.getInt("item_id");
				
				LOGGER.info("Got request item id - " + itemId);
				offset = offset + 1;
				rs.setItemId(itemId);
				rs.setTitle(rs1.getString("item_name"));
				rs.setDescription(rs1.getString("item_desc"));
				rs.setCategory(rs1.getString("item_category"));
				rs.setLeaseTerm(rs1.getString("item_lease_term"));
				rs.setInsurance(rs1.getInt("item_lease_value"));
				rs.setSurcharge(rs1.getInt("item_surcharge"));
				rs.setPrimaryImageLink(rs1.getString("item_primary_image_link"));
				rs.setUid(rs1.getString("item_uid"));
				rs.setLocality(rs1.getString("user_locality"));
				rs.setSublocality(rs1.getString("user_sublocality"));
				rs.setOffset(offset);
				
				String sqlGetUserDetails = "SELECT tb1.request_message, tb1.request_id, tb1.request_lastmodified, tb2.*, (CASE WHEN tb1.request_requser_id=tb4.friend_id AND tb4.friend_user_id=? THEN true ELSE false END) AS isFriend, ( 6371 * acos( cos( radians("+rs1.getFloat("item_lat")+") ) * cos( radians( tb2.user_lat ) ) * cos( radians( tb2.user_lng ) - radians("+rs1.getFloat("item_lng")+") ) + sin( radians("+rs1.getFloat("item_lat")+") ) * sin( radians( tb2.user_lat ) ) ) ) AS distance FROM requests tb1 INNER JOIN users tb2 ON tb1.request_requser_id=tb2.user_id LEFT JOIN (SELECT * FROM friends WHERE friend_user_id=?) tb4 ON tb1.request_requser_id = tb4.friend_id WHERE tb1.request_status=? AND tb1.request_item_id=? ORDER BY tb1.request_lastmodified desc";
				ps2 = hcp.prepareStatement(sqlGetUserDetails);
				ps2.setString(1, rq.getUserId());
				ps2.setString(2, rq.getUserId());
				ps2.setString(3, "Active");
				ps2.setInt(4, itemId);
				
				rs2 = ps2.executeQuery();
				
				while(rs2.next()){
					LOGGER.info("Got request user id " + rs2.getString("user_id"));
					RequestObj request = new RequestObj();
					request.setRequestId(rs2.getInt("request_id"));
					request.setRequestDate(rs2.getString("request_lastmodified"));
					request.setFriend(rs2.getBoolean("isFriend"));
					request.setRequestorId(rs2.getString("user_id"));
					request.setRequestorUid(rs2.getString("user_uid"));
					request.setDistance(rs2.getFloat("distance"));
					request.setRequestorName(rs2.getString("user_full_name"));
					request.setRequestorProfilePic(rs2.getString("user_profile_picture"));
					request.setRequestorLocality(rs2.getString("user_locality"));
					request.setRequestorSublocality(rs2.getString("user_sublocality"));
					request.setRequestorMessage(rs2.getString("request_message"));
					
					rs.addRequests(request);
				}
				
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
				
			}else{
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
			}
			
		} catch (SQLException e) {
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			LOGGER.warning("Error Check Stacktrace");
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
				rs.setCode(FLS_INVALID_OPERATION);
				rs.setMessage(FLS_END_OF_DB_M);
			}
		}
		LOGGER.info("Finished process method of GetRequestsPlusHandler");
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
	}

}
