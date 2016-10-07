package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.GetRequestsByXReqObj;
import pojos.GetRequestsByXResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetRequestsByXHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetRequestsByXHandler.class.getName());
	
	private static GetRequestsByXHandler instance = null;

	public static GetRequestsByXHandler getInstance() {
		if (instance == null)
			instance = new GetRequestsByXHandler();
		return instance;
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		GetRequestsByXReqObj rq = (GetRequestsByXReqObj) req;
		
		GetRequestsByXResObj rs = new GetRequestsByXResObj();
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement sql_stmt = null;
		ResultSet dbResponse = null;
		
		LOGGER.info("Inside process method UserId:" + rq.getUserId() + " ReqUserId: " + rq.getReqUserId() + "Cookie: " + rq.getCookie());
		
		try{
			
			// Prepare SQL
			String sql = null;
			
			// request data getting from the front end
			String UserId = rq.getUserId();
			String ReqUserId = rq.getReqUserId();
			int offset = rq.getCookie();
			String status = rq.getStatus();
			
			//already getting all data from tb1. for tb3 add new coloum
			sql = "SELECT tb1.*, tb2.*, tb3.user_full_name AS OwnerName, tb3.user_address AS OwnerAddress, tb3.user_mobile AS OwnerMobile, tb3.user_locality AS OwnerLocality, tb3.user_sublocality AS OwnerSublocality, tb3.user_profile_picture as OwnerProfilePic, tb4.* FROM requests tb1 INNER JOIN users tb2 ON tb1.request_requser_id = tb2.user_id INNER JOIN items tb4 ON tb1.request_item_id = tb4.item_id INNER JOIN users tb3 ON tb4.item_user_id = tb3.user_id WHERE ";
			
			if(UserId != "")
				sql = sql + "tb4.item_user_id='" + UserId + "' AND ";
			else if(ReqUserId != "")
				sql = sql + "tb1.request_requser_id='" + ReqUserId + "' AND ";
			
			sql = sql + " tb1.request_status='" + status + "' ORDER BY tb1.request_id LIMIT " + offset + ", 1";
			
			sql_stmt = hcp.prepareStatement(sql);

			dbResponse = sql_stmt.executeQuery();
			
			if(dbResponse.next()){
				rs.setRequestId(dbResponse.getInt("request_id"));
				rs.setRequestorUserId(dbResponse.getString("request_requser_id"));
				rs.setRequestorFullName(dbResponse.getString("user_full_name"));
				rs.setRequestorAddress(dbResponse.getString("user_address"));
				rs.setRequestorMobile(dbResponse.getString("user_mobile"));
				rs.setRequestorLocality(dbResponse.getString("user_locality"));
				rs.setRequestorSublocality(dbResponse.getString("user_sublocality"));
				rs.setRequestorProfilePic(dbResponse.getString("user_profile_picture"));
				
				rs.setOwnerUserId(dbResponse.getString("item_user_id"));
				rs.setOwnerFullName(dbResponse.getString("OwnerName"));
				rs.setOwnerAddress(dbResponse.getString("OwnerAddress"));
				rs.setOwnerMobile(dbResponse.getString("OwnerMobile"));
				rs.setOwnerLocality(dbResponse.getString("OwnerLocality"));
				rs.setOwnerSublocality(dbResponse.getString("OwnerSublocality"));
				rs.setOwnerProfilePic(dbResponse.getString("OwnerProfilePic"));
				
				rs.setRequestDate(dbResponse.getString("request_date"));
				
				rs.setItemId(dbResponse.getInt("item_id"));
				rs.setTitle(dbResponse.getString("item_name"));
				rs.setDescription(dbResponse.getString("item_desc"));
				rs.setCategory(dbResponse.getString("item_category"));
				rs.setLeaseValue(dbResponse.getString("item_lease_value"));
				rs.setLeaseTerm(dbResponse.getString("item_lease_term"));
				rs.setPrimaryImageLink(dbResponse.getString("item_primary_image_link"));
				rs.setStatus(dbResponse.getString("item_status"));
				rs.setUid(dbResponse.getString("item_uid"));
				
				offset = offset + 1;
				rs.setCookie(offset);
				
				rs.setCode(FLS_SUCCESS);
			}else{
				rs.setCode(FLS_END_OF_DB);
				rs.setMessage(FLS_END_OF_DB_M);
				LOGGER.warning("End of DB");
			}
			
		}catch(SQLException e){
			rs.setCode(FLS_JSON_EXCEPTION);
			rs.setMessage(FLS_JSON_EXCEPTION_M);
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		}finally {
			try {
				if(dbResponse!=null) dbResponse.close();
				if(sql_stmt!=null) sql_stmt.close();
				if(hcp!=null) hcp.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
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
