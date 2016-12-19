package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import connect.Connect;

public class FlsCredit extends Connect {

	private FlsLogger LOGGER = new FlsLogger(FlsCredit.class.getName());

	private String userId;

	public FlsCredit(String userId) {
		this.userId = userId;
	}
	
	public enum Credit_Type{
		ADD_CREDIT,
		SUB_CREDIT
	}

	public void logCredit(int credits, String type, String description, Credit_Type creditType) {

		LOGGER.info("Inside LogCredit Method");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		int rs1, rs2;

		try {
			String sqlCreateCreditLog = "insert into credit_log (credit_user_id, credit_amount, credit_type, credit_desc) values (?,?,?,?)";
			ps1 = hcp.prepareStatement(sqlCreateCreditLog);
			ps1.setString(1, userId);
			if(creditType == Credit_Type.ADD_CREDIT)
				ps1.setInt(3, credits);
			else
				ps1.setInt(3, -credits);
			ps1.setString(4, type);
			ps1.setString(5, description);

			rs1 = ps1.executeUpdate();

			if (rs1 == 1) {
				
				String sql = "UPDATE users SET ";
				if(creditType == Credit_Type.ADD_CREDIT){
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
					if(creditType == Credit_Type.ADD_CREDIT){
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

}
