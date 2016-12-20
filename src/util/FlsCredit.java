package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;

public class FlsCredit extends Connect {

	private FlsLogger LOGGER = new FlsLogger(FlsCredit.class.getName());
	
	public enum Credit{
		ADD,
		SUB
	}

	public void logCredit(String userId, int credits, String type, String description, Credit creditType) {

		LOGGER.info("Inside logCredit Method");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		int rs1, rs2;

		try {
			String sqlCreateCreditLog = "insert into credit_log (credit_user_id, credit_amount, credit_type, credit_desc) values (?,?,?,?)";
			ps1 = hcp.prepareStatement(sqlCreateCreditLog);
			ps1.setString(1, userId);
			if(creditType == Credit.ADD)
				ps1.setInt(2, credits);
			else
				ps1.setInt(2, -credits);
			ps1.setString(3, type);
			ps1.setString(4, description);

			rs1 = ps1.executeUpdate();

			if (rs1 == 1) {
				
				String sql = "UPDATE users SET ";
				if(creditType == Credit.ADD){
					LOGGER.info("Adding " + credits + " Credits to the userId : " + userId);
					sql = sql + "user_credit=user_credit+" + credits;
				} else {
					LOGGER.info("Subtracting " + credits + " Credits from the userId : " + userId);
					sql = sql + "user_credit=user_credit-" + credits;
				}
				sql = sql + " WHERE user_id=?";
				
				ps2 = hcp.prepareStatement(sql);
				ps2.setString(1, userId);
				
				rs2 = ps2.executeUpdate();
				
				if(rs2 == 1){
					if(creditType == Credit.ADD){
						LOGGER.info("Added credits to users account.");
					}else{
						LOGGER.info("Subtracted credits from users account.");
					}
				} else {
					LOGGER.info("Not able to add or subtract credits");
				}
				
			} else {
				LOGGER.info("Not able to log credit for the userId : " + userId);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(ps2 != null) ps2.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public int addRefferalCredits(String referrerCode){
		
		LOGGER.info("Inside addRefferalCredits Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		try {
			
			if(referrerCode != null){
				
				String sqlAddReferrerCredits = "SELECT user_id FROM users WHERE user_referral_code=?";
				ps1 = hcp.prepareStatement(sqlAddReferrerCredits);
				ps1.setString(1, referrerCode);
				rs1 = ps1.executeQuery();
				
				if(rs1.next()){
					LOGGER.info("ReferrerCode belongs to the userId - " + rs1.getString("user_id"));
					logCredit(rs1.getString("user_id"), 10, "Referred for sign up", "", Credit.ADD);
					return 1;
				}else{
					LOGGER.info("Not able to find the referrerCode - " + referrerCode);
				}
				
			}else{
				LOGGER.info("referrerCode is null");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return 0;
	}

}
