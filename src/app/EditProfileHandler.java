package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.EditProfileReqObj;
import pojos.EditProfileResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.FlsPlan;
import util.OAuth;

public class EditProfileHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(EditProfileHandler.class.getName());

	private static EditProfileHandler instance = null;

	public static EditProfileHandler getInstance() {
		if (instance == null)
			instance = new EditProfileHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		
		EditProfileReqObj rq = (EditProfileReqObj) req;
		EditProfileResObj rs = new EditProfileResObj();
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		int rs1, rs2;

		LOGGER.info("Inside Process Method of EditProfileHandler");

		try {
			
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if(!oauthcheck.equals(rq.getUserId())){
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}
			
			if(havePendingRequests(rq.getUserId())){
				if(!hasSameLocality(rq.getUserId(), rq.getLocality())){
					rs.setCode(FLS_ACTIVE_REQUEST);
					rs.setMessage(FLS_ACTIVE_REQUEST_M);
					return rs;
				}
			}
			
			if(havePendingLeases(rq.getUserId())){
				if(!hasSameLocality(rq.getUserId(), rq.getLocality())){
					rs.setCode(FLS_ACTIVE_LEASE);
					rs.setMessage(FLS_ACTIVE_LEASE_M);
					return rs;
				}
			}
			
			LOGGER.info("Updating users table...");
			String sql = "UPDATE users SET user_full_name=?, user_location=?, user_locality=?, user_sublocality=?, user_lat=?, user_lng=? WHERE user_id=?";
			ps1 = hcp.prepareStatement(sql);
			ps1.setString(1, rq.getFullName());
			ps1.setString(2, rq.getLocation());
			ps1.setString(3, rq.getLocality());
			ps1.setString(4, rq.getSublocality());
			ps1.setFloat(5, rq.getLat());
			ps1.setFloat(6, rq.getLng());
			ps1.setString(7, rq.getUserId());

			rs1 = ps1.executeUpdate();
			
			if(rs1 == 1){
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_EDIT_PROFILE);
				LOGGER.info("Users table updated for : " + rq.getUserId());
			}
			else{
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				LOGGER.info("Users table not updated for : " + rq.getUserId());
			}
			
			// updating items table with lat lng
			LOGGER.info("Updating items table with lat lng...");
			String updateItemsLatLng = "UPDATE items SET item_lat=?, item_lng=? WHERE item_user_id=?";
			
			ps2 = hcp.prepareStatement(updateItemsLatLng);
			ps2.setFloat(1, rq.getLat());
			ps2.setFloat(2, rq.getLng());
			ps2.setString(3, rq.getUserId());
			
			rs2 = ps2.executeUpdate();
			
			if(rs2 == 1)
				LOGGER.info("Items table updated for : " + rq.getUserId());
			else
				LOGGER.info("Items table not updated for : " + rq.getUserId());

			FlsPlan plan = new FlsPlan();
			plan.checkPlan(rq.getUserId());

		} catch (SQLException e) {
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (NullPointerException e) {
			rs.setCode(FLS_NULL_POINT);
			rs.setMessage(FLS_NULL_POINT_M);
			e.printStackTrace();
		} finally {
			try{
				if(ps1 != null) ps1.close();
				if(ps2 != null) ps2.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				rs.setCode(FLS_INVALID_OPERATION);
				rs.setMessage(FLS_INVALID_OPERATION_M);
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
	
	private boolean havePendingRequests(String userId){
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		try{
			
			String sqlActiveRequest = "SELECT tb1.*, tb2.item_user_id FROM requests tb1 INNER JOIN items tb2 ON tb1.request_item_id=tb2.item_id WHERE (tb1.request_requser_id=? OR tb2.item_user_id=?) AND tb1.request_status=?";
			
			ps1 = hcp.prepareStatement(sqlActiveRequest);
			ps1.setString(1, userId);
			ps1.setString(2, userId);
			ps1.setString(3, "Active");
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				return true;
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(ps1 != null) ps1.close();
				if(rs1 != null) rs1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return false;
		
	}
	
	private boolean havePendingLeases(String userId){
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		try{
			
			String sqlActiveLease = "SELECT * FROM leases WHERE (lease_requser_id=? OR lease_user_id=?) AND lease_status=?";
			
			ps1 = hcp.prepareStatement(sqlActiveLease);
			ps1.setString(1, userId);
			ps1.setString(2, userId);
			ps1.setString(3, "Active");
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				return true;
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(ps1 != null) ps1.close();
				if(rs1 != null) rs1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return false;
		
	}
	
	private boolean hasSameLocality(String userId, String locality){
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		try{
			
			String sqlGetLocality = "SELECT user_locality FROM users WHERE user_id=? AND user_locality=?";
			ps1 = hcp.prepareStatement(sqlGetLocality);
			ps1.setString(1, userId);
			ps1.setString(2, locality);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				return true;
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(ps1 != null) ps1.close();
				if(rs1 != null) rs1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return false;
	}

}
