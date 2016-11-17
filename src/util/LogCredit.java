package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import connect.Connect;
import util.FlsLogger;

public class LogCredit extends Connect{
	
	private FlsLogger LOGGER = new FlsLogger(LogCredit.class.getName());
	
	public void addLogCredit(String user, int credit, String type, String description) throws SQLException{
		
		int days = 0;
		PreparedStatement stmt= null;
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, days);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(cal.getTime());
		
		LOGGER.info("Inside Log Credit");
		String sql = "insert into credit_log (credit_user_id,credit_date,credit_amount,credit_type,credit_desc) values (?,?,?,?,?)";
		Connection hcp = getConnectionFromPool();
		
		try {
			LOGGER.info("executing insert query on credit_log...");
			stmt = hcp.prepareStatement(sql);
			stmt.setString(1, user);
			stmt.setString(2, date);
			stmt.setInt(3, credit);
			stmt.setString(4, type);
			stmt.setString(5, description);
			
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			stmt.close();
			hcp.close();
		}
	}
	
	public int updateCredits(String referral, String referrer){
		
			Connection hcp = getConnectionFromPool();
			PreparedStatement stmt= null,stmt1=null;
			Integer send_val =1;
			try {
				
				if(referrer!=null){
					hcp.setAutoCommit(false);
					
					// add credit to existing user whose referral_code was used
					int addReferrerCredit =0;
					String sqladdReferrerCredit = "UPDATE users SET user_credit=user_credit+10 WHERE user_referral_code=?";
					stmt = hcp.prepareStatement(sqladdReferrerCredit);
					stmt.setString(1, referrer);
					addReferrerCredit = stmt.executeUpdate();
					LOGGER.info("Credits of Referer incremented by 10.....");
					
					
					if(addReferrerCredit == 0){
						send_val = 0;
						hcp.rollback();
						return send_val;
					}
					addLogCredit(referrer,10,"SignUp with Code","");
					
					// add credit to new user whose referral_code was generated
					int addReferralCredit =0;
					String sqladdReferralCredit = "UPDATE users SET user_credit=user_credit+10 WHERE user_referral_code=?";
					stmt1 = hcp.prepareStatement(sqladdReferralCredit);
					stmt1.setString(1, referral);
					addReferralCredit = stmt1.executeUpdate();
					LOGGER.info("Credits of new user incremented by 10.....");
					
					if(addReferralCredit == 0){
						send_val = 0;
						hcp.rollback();
						return send_val;
					}
					addLogCredit(referral,10,"SignUp with Referral","");
					hcp.commit();
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				try {
					if(stmt!=null) stmt.close();
					if(stmt1!=null) stmt1.close();
					if(hcp!=null) hcp.close();
				} catch (Exception e2) {
				}
			}
			return send_val;
	}
	
	public void addCredit(String userId, int credits){
		LOGGER.info("Adding " + credits + " Credits to the userId : " + userId);
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int rs1;
		
		try{
			String sqlAddCredits = "UPDATE users SET user_credit=user_credit+" + credits + " WHERE user_id=?";
			ps1 = hcp.prepareStatement(sqlAddCredits);
			ps1.setString(1, userId);
			
			rs1 = ps1.executeUpdate();
			
			if(rs1 == 1)
				LOGGER.info("Added credits to users account.");
			else
				LOGGER.info("Not able to add credits to users account.");
			
		}catch(Exception e){
			LOGGER.warning("Error occured while adding credits to the user : " + userId);
			e.printStackTrace();
		}
		
	}
	
	public void subtractCredit(String userId, int credits){
		LOGGER.info("Subtracting " + credits + " Credits from the userId : " + userId);
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int rs1;
		
		try{
			String sqlAddCredits = "UPDATE users SET user_credit=user_credit-" + credits + " WHERE user_id=?";
			ps1 = hcp.prepareStatement(sqlAddCredits);
			ps1.setString(1, userId);
			
			rs1 = ps1.executeUpdate();
			
			if(rs1 == 1)
				LOGGER.info("successfully subtracted credits from users account.");
			else
				LOGGER.info("Not able to subtract credits from users account.");
			
		}catch(Exception e){
			LOGGER.warning("Error occured while subtracting credits from the user : " + userId);
			e.printStackTrace();
		}
		
	}
}
