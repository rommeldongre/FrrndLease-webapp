package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;

		try {

			String sqlGetUserStatus = "SELECT user_status, user_activation FROM users WHERE user_id=?";
			ps1 = hcp.prepareStatement(sqlGetUserStatus);
			ps1.setString(1, userId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				String status = rs1.getString("user_status");
				String activation = rs1.getString("user_activation");
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
							Event event = new Event();
							event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOT_NOTIFICATION, Notification_Type.FLS_MAIL_SIGNUP_VALIDATION, 0, "Click on the link sent to your registered email account.");
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						rs.setCode(FLS_INVALID_USER_I);
						rs.setMessage("This email was not verified from the link sent during sign up.");
						break;
					case "email_activated":
						try{
							Event event = new Event();
							event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOT_NOTIFICATION, Notification_Type.FLS_MAIL_FORGOT_PASSWORD, 0, "A link has been sent to your registered email account for reseting the password.");
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						rs.setCode(FLS_SUCCESS);
						rs.setMessage("The link to reset password has been sent to your email.");
						break;
					default:
						rs.setCode(FLS_ENTRY_NOT_FOUND);
						rs.setMessage("Somethings wrong in our side. We'll get back to you as soon as it is fixed.");
						break;
				}
			}else{
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage("This email id does not exist!!");
			}
			
		} catch (SQLException e) {

		} finally {
			if (rs1 != null)rs1.close();
		}

		LOGGER.info("Inside Process Method of Forgot Password with userId: " + rq.getUserId());

		return rs;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

}
