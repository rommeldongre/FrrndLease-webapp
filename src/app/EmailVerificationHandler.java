package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import adminOps.Response;
import connect.Connect;
import pojos.EmailVerificationReqObj;
import pojos.EmailVerificationResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.LogCredit;

public class EmailVerificationHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(EmailVerificationHandler.class.getName());

	private Response res = new Response();

	private static EmailVerificationHandler instance = null;

	public static EmailVerificationHandler getInstance() {
		if (instance == null)
			return new EmailVerificationHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub

		EmailVerificationReqObj rq = (EmailVerificationReqObj) req;
		EmailVerificationResObj rs = new EmailVerificationResObj();
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null,ps2=null,ps3=null;
		ResultSet result1 = null,rs2=null;
		String referral_code =null, referrer_code=null;
		
		LOGGER.info("Inside Process Method " + rq.getVerification());

		try {
			String select_status_sql = "Select user_status,user_id FROM users WHERE user_activation=?";
			ps1 = hcp.prepareStatement(select_status_sql);
			ps1.setString(1, rq.getVerification());

			result1 = ps1.executeQuery();
			
			if (result1.next()) {
				if (result1.getString("user_status").equals("email_pending")) {
					String update_status_sql = "UPDATE users SET user_status=? WHERE user_activation=?";
					LOGGER.info("Creating Statement...");
					ps2 = hcp.prepareStatement(update_status_sql);
					ps2.setString(1, "email_activated");
					ps2.setString(2, rq.getVerification());

					LOGGER.info("statement created...executing update to users query");
					int result2 = ps2.executeUpdate();
					int result3 =0;
					LOGGER.info("Update Query Result : " + result2);
					
					if (result2 == 1) {
						String get_activation_sql = "SELECT * FROM `users` WHERE user_activation=?";
						LOGGER.info("Creating Select Statement to fetch referral & referrer Code...");
						ps3 = hcp.prepareStatement(get_activation_sql);
						ps3.setString(1, rq.getVerification());

						LOGGER.info("statement created...executing update to users query");
						rs2 = ps3.executeQuery();
						
						if(rs2.next()){
							referral_code = rs2.getString("user_referral_code");
							referrer_code = rs2.getString("user_referrer_code");
							result3 = updateCredits(referral_code,referrer_code);
						}
					}

					if (result3 == 1) {
						rs.setCode(FLS_SUCCESS);
						rs.setUserId(result1.getString("user_id"));
						rs.setMessage("Your account has been activated!!");
					} else {
						rs.setCode(FLS_SQL_EXCEPTION);
						rs.setUserId("");
						rs.setMessage(
								"Could not activate your account due to some internal problems!! Trying to fix it ASAP");
					}
					ps2.close();
				} else {
					rs.setCode(FLS_END_OF_DB);
					rs.setUserId("");
					rs.setMessage("This account is already activated!! No point activating it again");
				}
			} else {
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setUserId("");
				rs.setMessage("This account is not registered!! I wonder how you got this link");
			}

		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		} finally {
			try {
				if(result1!=null) result1.close();
				if(ps1!=null) ps1.close();
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
	
	public int updateCredits(String referral, String referrer){
		
			Connection hcp = getConnectionFromPool();
			PreparedStatement stmt= null,stmt1=null;
			Integer send_val =1;
			LogCredit lc = new LogCredit();
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
					lc.addLogCredit(referrer,10,"SignUp with Code","");
					
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
					lc.addLogCredit(referral,10,"SignUp with Referral","");
					hcp.commit();
				}
				
			} catch (SQLException e) {
				// TODO: handle exception
			}finally{
				try {
					if(stmt!=null) stmt.close();
					if(stmt1!=null) stmt1.close();
					if(hcp!=null) hcp.close();
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
			return send_val;
	}

}
