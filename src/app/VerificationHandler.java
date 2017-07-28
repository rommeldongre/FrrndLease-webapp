package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.ReqObj;
import pojos.ResObj;
import pojos.VerificationReqObj;
import pojos.VerificationResObj;
import util.Event;
import util.FlsCredit;
import util.FlsLogger;
import util.OAuth;
import util.Event.Event_Type;
import util.Event.Notification_Type;
import util.FlsCredit.Credit;

public class VerificationHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(VerificationHandler.class.getName());

	private static VerificationHandler instance = null;

	public static VerificationHandler getInstance() {
		if (instance == null)
			return new VerificationHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		VerificationReqObj rq = (VerificationReqObj) req;
		VerificationResObj rs = new VerificationResObj();
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null, ps3 = null, ps4 = null;
		ResultSet rs1 = null;
		
		String verification = rq.getVerification();
		Event event = new Event();
		
		LOGGER.info("Inside Process Method of verification handler");

		try {
			String sqlUserStatus = "Select user_id, user_status, user_full_name, user_email, user_sec_status, user_referral_code, user_referrer_code FROM users WHERE user_activation=?";
			ps1 = hcp.prepareStatement(sqlUserStatus);
			ps1.setString(1, verification);

			rs1 = ps1.executeQuery();
			
			String sqlUpdateStatus = null;
			
			FlsCredit credits = new FlsCredit();
			
			if (rs1.next()) {
				if(isUserId(verification)){
					switch(rs1.getString("user_status")){
						case "email_pending":
							sqlUpdateStatus = "UPDATE users SET user_status=? WHERE user_activation=?";
							ps2 = hcp.prepareStatement(sqlUpdateStatus);
							ps2.setString(1, "email_activated");
							ps2.setString(2, verification);

							int res2 = ps2.executeUpdate();
							LOGGER.info("Updating user_status to email_activated : " + res2);
							
							if (res2 == 1) {
								int r = credits.addRefferalCredits(rs1.getString("user_referrer_code"));
								if(r == 1)
									credits.logCredit(rs1.getString("user_id"), 10, "Used Referral for Sign Up", "", Credit.ADD);
								rs.setCode(FLS_SUCCESS);
								rs.setUserId(rs1.getString("user_id"));
								rs.setName(rs1.getString("user_full_name"));
								rs.setMessage("Your user id has been activated!!");
								OAuth oauth = new OAuth();
								String access_token = oauth.generateOAuth(rs1.getString("user_id"));
								rs.setAccess_token(access_token);
								event.createEvent(rs1.getString("user_id"), rs1.getString("user_id"), Event_Type.FLS_EVENT_NOT_NOTIFICATION, Notification_Type.FLS_MAIL_REGISTER, 0, "Your email has been registered on FrrndLease.");
							}else{
								rs.setCode(FLS_SQL_EXCEPTION);
								rs.setMessage("Some issues on our side. Trying to fix them ASAP!!");
							}
							break;
						case "mobile_pending":
							sqlUpdateStatus = "UPDATE users SET user_status=? WHERE user_activation=?";
							ps3 = hcp.prepareStatement(sqlUpdateStatus);
							ps3.setString(1, "mobile_activated");
							ps3.setString(2, verification);

							int res3 = ps3.executeUpdate();
							LOGGER.info("Updating user_status to mobile_activated : " + res3);
							
							if (res3 == 1) {
								int r = credits.addRefferalCredits(rs1.getString("user_referrer_code"));
								if(r == 1)
									credits.logCredit(rs1.getString("user_id"), 10, "Used Referral for Sign Up", "", Credit.ADD);
								rs.setCode(FLS_SUCCESS);
								rs.setUserId(rs1.getString("user_id"));
								rs.setName(rs1.getString("user_full_name"));
								rs.setMessage("Your user id has been activated!!");
								OAuth oauth = new OAuth();
								String access_token = oauth.generateOAuth(rs1.getString("user_id"));
								rs.setAccess_token(access_token);
								event.createEvent(rs1.getString("user_id"), rs1.getString("user_id"), Event_Type.FLS_EVENT_NOT_NOTIFICATION, Notification_Type.FLS_SMS_REGISTER, 0, "Your mobile number has been registered on FrrndLease.");
							}else{
								rs.setCode(FLS_SQL_EXCEPTION);
								rs.setMessage("Some issues on our side. Trying to fix them ASAP!!");
							}
							break;
						case "email_activated":
						case "mobile_activated":
							rs.setCode(FLS_END_OF_DB);
							rs.setMessage("This user id is already activated!! No point activating it again");
							break;
						default:
							rs.setCode(FLS_INVALID_OPERATION);
							rs.setMessage(FLS_INVALID_OPERATION_M);
					}
				}else{
					if(rs1.getInt("user_sec_status") == 0){
						String sqlUpdateSecStatus = "UPDATE users SET user_sec_status=? WHERE user_activation=?";
						ps4 = hcp.prepareStatement(sqlUpdateSecStatus);
						ps4.setInt(1, 1);
						ps4.setString(2, verification);
						
						int res3 = ps4.executeUpdate();
						LOGGER.info("Updating user_sec_status to '1' : " + res3);
						
						if(res3 == 1){
							rs.setCode(FLS_SUCCESS);
							if(rs1.getString("user_status").equals("email_activated"))
								rs.setMessage("Your email has been activated!!");
							else
								rs.setMessage("Your mobile number has been activated!!");
						}else{
							rs.setCode(FLS_SQL_EXCEPTION);
							rs.setMessage("Some issues on our side. Trying to fix them ASAP!!");
						}
						
					}else{
						rs.setCode(FLS_END_OF_DB);
						if(rs1.getString("user_status").equals("email_activated"))
							rs.setMessage("This email is already activated. No point activating it again.");
						else
							rs.setMessage("This mobile number is already activated. No point activating it again.");
					}
				}
				
			} else {
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage("This is an invalid link!!");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(ps4 != null) ps4.close();
				if(ps3 != null) ps3.close();
				if(ps2 != null) ps2.close();
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		LOGGER.info("Finished Verification process method ");
		return rs;
	}

	@Override
	public void cleanup() {
	}
	
	private boolean isUserId(String verification){
		
		if(verification.substring(verification.length() - 2).equals("_u"))
			return true;
		
		return false;
	}

}
