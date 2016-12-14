package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import connect.Connect;
import pojos.GetUserBadgesResObj;

public class FlsBadges extends Connect {

	private FlsLogger LOGGER = new FlsLogger(FlsBadges.class.getName());

	private String userId;

	public FlsBadges(String UserId) {
		this.userId = UserId;
	}

	public GetUserBadgesResObj getBadges(){
		
		LOGGER.info("Inside getBadges Method");
		
		GetUserBadgesResObj rs = new GetUserBadgesResObj();
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;

		try {

			LOGGER.info("Getting badges for user id - " + userId);

			String sqlGetBadges = "SELECT user_status, user_verified_flag, user_items, user_leases, user_response_time, user_response_count, user_signup_date, user_credit FROM users WHERE user_id=?";
			ps1 = hcp.prepareStatement(sqlGetBadges);
			ps1.setString(1, userId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
				rs.setUserStatus(rs1.getString("user_status"));
				rs.setUserVeifiedFlag(rs1.getBoolean("user_verified_flag"));
				rs.setUserItems(rs1.getInt("user_items"));
				rs.setUserLeases(rs1.getInt("user_leases"));
				rs.setUserSignupDate(rs1.getString("user_signup_date"));
				rs.setResponseTime(rs1.getInt("user_response_time"));
				rs.setResponseCount(rs1.getInt("user_response_count"));
				rs.setUserCredit(rs1.getInt("user_credit"));
				
				LOGGER.info("Posted items count - " + rs.getUserItems() + " Total Leases - " + rs.getUserLeases());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
		} finally {
			try {
				if (rs1 != null) rs1.close();
				if (ps1 != null) ps1.close();
				if (hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return rs;
		
	}
	
	public void updateItemsCount(){
		
		LOGGER.info("Inside updateItemsCount Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null;
		
		try{
			
			String sqlGetTotalItems = "SELECT COUNT(*) AS items_count FROM items WHERE item_user_id=? AND item_status NOT IN ('Archived','Wished')";
			ps1 = hcp.prepareStatement(sqlGetTotalItems);
			ps1.setString(1, userId);
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				String sqlUpdateItemsCount = "UPDATE users SET user_items=? WHERE user_id=?";
				ps2 = hcp.prepareStatement(sqlUpdateItemsCount);
				ps2.setInt(1, rs1.getInt("items_count"));
				ps2.setString(2, userId);
				
				LOGGER.info("items count of the user - " + userId + " updated to " + rs1.getInt("items_count"));
				
				ps2.executeUpdate();
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(ps2 != null) ps2.close();
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
	public void updateLeasesCount(){
		
		LOGGER.info("Inside updateLeasesCount Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null;
		
		try{
			
			String sqlGetTotalLeases = "SELECT COUNT(*) AS leases_count FROM leases WHERE lease_user_id=? OR lease_requser_id=?";
			ps1 = hcp.prepareStatement(sqlGetTotalLeases);
			ps1.setString(1, userId);
			ps1.setString(2, userId);
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				String sqlUpdateItemsCount = "UPDATE users SET user_leases=? WHERE user_id=?";
				ps2 = hcp.prepareStatement(sqlUpdateItemsCount);
				ps2.setInt(1, rs1.getInt("leases_count"));
				ps2.setString(2, userId);
				
				LOGGER.info("Leases count of the user - " + userId + " updated to " + rs1.getInt("leases_count"));
				
				ps2.executeUpdate();
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(ps2 != null) ps2.close();
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
	public void updateRequestResponseTime(String requestDate){

		LOGGER.info("Inside updateLeasesCount Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null;
		int rs2;
		
		try{
			
			String sqlGetResponseTime = "SELECT TIMESTAMPDIFF(DAY, ?, NOW()) AS response_time";
			ps1 = hcp.prepareStatement(sqlGetResponseTime);
			ps1.setString(1, requestDate);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				int response = Math.abs(rs1.getInt("response_time"));
				LOGGER.info("Got a response time of - " + response);
				
				String sqlUpdateResponseTime = "UPDATE users SET user_response_time=user_response_time+?, user_response_count=user_response_count+1 WHERE user_id=?";
				ps2 = hcp.prepareStatement(sqlUpdateResponseTime);
				ps2.setInt(1, response);
				ps2.setString(2, userId);
				
				rs2 = ps2.executeUpdate();
				
				if(rs2 == 1)
					LOGGER.info("user response time incremented for user - " + userId + " by - " + response);
				else
					LOGGER.info("user response time not incremented for user - " + userId);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(ps2 != null) ps2.close();
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
}
