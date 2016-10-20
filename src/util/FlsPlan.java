package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import connect.Connect;

public class FlsPlan extends Connect{

	private FlsLogger LOGGER = new FlsLogger(FlsPlan.class.getName());
	
	public enum Fls_Plan{
		FLS_SELFIE,
		FLS_PRIME,
		FLS_UBER
	}
	
	public enum Delivery_Plan{
		FLS_NONE,
		FLS_SELF,
		FLS_OPS
	}
	
	public void checkPlan(String userId){
		
		LOGGER.info("Inside check plan for the user id : " + userId);
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null, rs2 = null;
		
		try{
			
			String sqlSelectUser = "SELECT user_locality, user_verified_flag, user_plan FROM users where user_id=?";
			ps1 = hcp.prepareStatement(sqlSelectUser);
			ps1.setString(1, userId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				if(rs1.getInt("user_verified_flag") == 1){
					String userLocality = (rs1.getString("user_locality")).toLowerCase();
					String sqlCheckPlaces = "SELECT * FROM places WHERE locality=?";
					
					ps2 = hcp.prepareStatement(sqlCheckPlaces);
					ps2.setString(1, userLocality);
					
					rs2 = ps2.executeQuery();
					
					if(rs2.next()){
						if(!Fls_Plan.FLS_PRIME.name().equals(rs1.getString("user_plan")))
							setUserPlan(userId, Fls_Plan.FLS_PRIME);
					}else{
						if(!Fls_Plan.FLS_SELFIE.name().equals(rs1.getString("user_plan")))
							setUserPlan(userId, Fls_Plan.FLS_SELFIE);
					}
				}else{
					LOGGER.info("The user is not verified");
					if(!Fls_Plan.FLS_SELFIE.name().equals(rs1.getString("user_plan")))
						setUserPlan(userId, Fls_Plan.FLS_SELFIE);
				}
			}else{
				LOGGER.info("The user id : " + userId + " does not exist");
			}
			
		}catch(Exception e){
			LOGGER.warning("Exception occured while checking plan");
			e.printStackTrace();
		}finally{
			try{
				if(rs2 != null)	rs2.close();
				if(ps2 != null) ps2.close();
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
	public void setUserPlan(String userId, Fls_Plan user_plan){
		
		LOGGER.info("Changing user plan to : " + user_plan);
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int rs1;
		
		try{
			
			String sql = "UPDATE users SET user_plan=? WHERE user_id=?";
			ps1 = hcp.prepareStatement(sql);
			ps1.setString(1, user_plan.name());
			ps1.setString(2, userId);
			
			rs1 = ps1.executeUpdate();
			
			if(rs1 == 1)
				LOGGER.info("user plan updated to : " + user_plan);
			else
				LOGGER.info("Not able to change user plan");
			
		}catch(Exception e){
			LOGGER.warning("Exception occured while checking plan");
			e.printStackTrace();
		}finally{
			try{
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
}
