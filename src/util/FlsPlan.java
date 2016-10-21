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
					String sqlCheckPlaces = "SELECT * FROM places WHERE locality=?";
					
					ps2 = hcp.prepareStatement(sqlCheckPlaces);
					ps2.setString(1, rs1.getString("user_locality"));
					
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
	
	public int changeDeliveryPlan(int leaseId, Delivery_Plan delivery_plan){
		
		LOGGER.info("Changing delivery plan to : " + delivery_plan);
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int rs1 = 0;
		
		try{
			
			String sql = "UPDATE leases SET delivery_plan=? WHERE lease_id=?";
			ps1 = hcp.prepareStatement(sql);
			ps1.setString(1, delivery_plan.name());
			ps1.setInt(2, leaseId);
			
			rs1 = ps1.executeUpdate();
			
			if(rs1 == 1)
				LOGGER.info("delivery plan updated to : " + delivery_plan);
			else
				LOGGER.info("Not able to change delivery plan for lease id : " + leaseId);
			
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
		
		return rs1;
		
	}
	
	public int changePickupStatus(int leaseId, String leaseUserId, String leaseReqUserId, boolean pickupStatus){
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null;
		int rs2 = 0;
		
		if(leaseUserId != ""){
			LOGGER.info("Changing pickup status of leaseId : " + leaseId + " for leaseUserId : " + leaseUserId + " to " + pickupStatus);
		}else if(leaseReqUserId != ""){
			LOGGER.info("Changing pickup status of leaseId : " + leaseId + " for leaseReqUserId : " + leaseReqUserId + " to " + pickupStatus);
		}else{
			return rs2;
		}

		try{
			
			String sqlgetBothStatus = "SELECT * FROM leases WHERE lease_id=? AND lease_status=? AND owner_pickup_status=? AND leasee_pickup_status=?";
			ps1 = hcp.prepareStatement(sqlgetBothStatus);
			ps1.setInt(1, leaseId);
			ps1.setString(2, "Active");
			ps1.setBoolean(3, true);
			ps1.setBoolean(4, true);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				return rs2;
			}
			
			String sql = "UPDATE leases SET ";
			if(leaseUserId != ""){
				sql = sql + "owner_pickup_status=? WHERE lease_id=?";
			}else if(leaseReqUserId != ""){
				sql = sql + "leasee_pickup_status=? WHERE lease_id=?";
			}
			
			ps2 = hcp.prepareStatement(sql);
			ps2.setBoolean(1, pickupStatus);
			ps2.setInt(2, leaseId);
			
			rs2 = ps2.executeUpdate();
			
			if(rs2 == 1)
				LOGGER.info("changed pickup status");
			else
				LOGGER.info("Not able to change pickup status");
			
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
		
		return rs2;
		
	}
	
}
