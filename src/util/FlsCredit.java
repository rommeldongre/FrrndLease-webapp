package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import connect.Connect;
import pojos.PromoCodeModel.Code_Type;

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

	public int getCreditLogId(String userId, String promoCode) {
		
		LOGGER.info("Inside getCreditLogId Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		int id = -1;
		
		try {
			
			String sqlGetCreditLogId = "SELECT credit_log_id FROM credit_log WHERE credit_user_id=? AND credit_desc=? ORDER BY credit_date DESC LIMIT 1";
			ps1 = hcp.prepareStatement(sqlGetCreditLogId);
			ps1.setString(1, userId);
			ps1.setString(2, promoCode);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				id = rs1.getInt("credit_log_id");
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
		
		return id;
	}
	
	public int getCreditValue() {
		
		LOGGER.info("Inside getCreditValue Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		try {
			
			String sqlGetCreditValue = "SELECT `value` FROM `config` WHERE `option`='credit_amount'";
			ps1 = hcp.prepareStatement(sqlGetCreditValue);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				return rs1.getInt("value");
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

	public void addOrder(String userId, int amount, String promoCode, int razorPayId, int creditLogId, Code_Type flsInternal) {
		
		LOGGER.info("Inside addOrder Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int rs1;
		
		try {
			
			String sqlInsertOrder = "INSERT INTO `orders` (`order_user_id`, `amount`, `promo_code`, `razor_pay_id`, `credit_log_id`, `order_type`) VALUES (?, ?, ?, ?, ?, ?)";
			ps1 = hcp.prepareStatement(sqlInsertOrder);
			ps1.setString(1, userId);
			ps1.setInt(2, amount);
			ps1.setString(3, promoCode);
			ps1.setInt(4, razorPayId);
			ps1.setInt(5, creditLogId);
			ps1.setString(6, flsInternal.name());
			
			rs1 = ps1.executeUpdate();
			
			if(rs1 == 1){
				LOGGER.info("New order created for the userId - " + userId + " and creditLogId - " + creditLogId);
			}else{
				LOGGER.info("Not able to create a new order");
			}		
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	public int getCurrentCredits(String userId) {
		
		LOGGER.info("Inside getCurrentCredits Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		try {
			
			String sqlGetCurrentCredits = "SELECT user_credit FROM users WHERE user_id=?";
			ps1 = hcp.prepareStatement(sqlGetCurrentCredits);
			ps1.setString(1, userId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				return rs1.getInt("user_credit");
			}	
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return 0;
	}
	
	public boolean expired(Date expiry) {

		int result = 0;

		try {
			Date current = new Date();
			current.setTime(0);
			result = current.compareTo(expiry);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}

		if (result <= 0)
			return false;
		else
			return true;
	}

}
