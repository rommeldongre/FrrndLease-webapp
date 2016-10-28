package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Random;

import connect.Connect;
import pojos.ForgotPasswordReqObj;
import pojos.ForgotPasswordResObj;
import pojos.ReqObj;
import pojos.ResObj;
import pojos.UsersModel;
import util.Event;
import util.Event.Event_Type;
import util.Event.Notification_Type;
import util.FlsLogger;

public class ForgotPasswordHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(ForgotPasswordHandler.class.getName());

	private static ForgotPasswordHandler instance = null;

	public static ForgotPasswordHandler getInstance() {
		if (instance == null)
			instance = new ForgotPasswordHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub

		ForgotPasswordReqObj rq = (ForgotPasswordReqObj) req;
		ForgotPasswordResObj rs = new ForgotPasswordResObj();

		String userId = rq.getUserId();
		
		Event event = new Event();

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null;

		try {

			String sqlGetUserStatus = "SELECT user_status, user_activation FROM users WHERE user_id=?";
			ps1 = hcp.prepareStatement(sqlGetUserStatus);
			ps1.setString(1, userId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				String status = rs1.getString("user_status");
				String activation = rs1.getString("user_activation");
				Random rnd = new Random();
				switch(status){
					case "google":
						rs.setCode(FLS_INVALID_USER_I);
						rs.setMessage("This email is signed up using google. Please continue with google.");
						break;
					case "facebook":
						rs.setCode(FLS_INVALID_USER_I);
						rs.setMessage("This email is signed up using facebook. Please continue with facebook.");
						break;
					case "email_pending":
						try{
							UsersModel um = new UsersModel();
							um.setActivation(activation);
							event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOT_NOTIFICATION, Notification_Type.FLS_MAIL_SIGNUP_VALIDATION, 0, "Click on the link sent to your registered email account.");
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						rs.setCode(FLS_NOT_VERIFIED);
						rs.setMessage("This email was not verified from the link sent during sign up.");
						break;
					case "email_activated":
						activation =  100000000 + rnd.nextInt(900000000)+"";
						String updateUserAct = "UPDATE users SET user_activation=? WHERE user_id=?";
						ps2 = hcp.prepareStatement(updateUserAct);
						ps2.setString(1, activation+"_u");
						ps2.setString(2, userId);
						ps2.executeUpdate();
						try{
							event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOT_NOTIFICATION, Notification_Type.FLS_MAIL_FORGOT_PASSWORD, 0, "A link has been sent to your registered email account for reseting the password.");
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						rs.setCode(FLS_SUCCESS);
						rs.setMessage("The link to reset password has been sent to your email.");
						break;
					case "mobile_pending":
						try{
							event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOT_NOTIFICATION, Notification_Type.FLS_SMS_SIGNUP_VALIDATION, 0, "Please enter this OTP for verifiying your mobile number.");
						}catch(Exception e){
							e.printStackTrace();
						}
						
						rs.setCode(FLS_NOT_VERIFIED);
						rs.setMessage("This mobile number is not verified. Please enter the otp sent to your mobile number to verify the same.");
						break;
					case "mobile_activated":
						activation =  100000 + rnd.nextInt(900000)+"";
						String updateUserCode = "UPDATE users SET user_activation=? WHERE user_id=?";
						ps2 = hcp.prepareStatement(updateUserCode);
						ps2.setString(1, activation+"_u");
						ps2.setString(2, userId);
						ps2.executeUpdate();
						try{
							event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOT_NOTIFICATION, Notification_Type.FLS_MAIL_FORGOT_PASSWORD, 0, "Please use this OTP for reseting your password.");
						}catch(Exception e){
							e.printStackTrace();
						}
						
						rs.setCode(FLS_SUCCESS);
						rs.setMessage("An OTP has been sent to reset the password.");
						break;
					default:
						rs.setCode(FLS_ENTRY_NOT_FOUND);
						rs.setMessage("Somethings wrong in our side. We'll get back to you as soon as it is fixed.");
						break;
				}
			}else{
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage("This user id does not exist!!");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
		} finally {
			try{
				if(ps2 != null) ps2.close();
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
				rs.setCode(FLS_INVALID_OPERATION);
				rs.setMessage(FLS_INVALID_OPERATION_M);
			}
		}

		LOGGER.info("Inside Process Method of Forgot Password with userId: " + rq.getUserId());

		return rs;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

}
