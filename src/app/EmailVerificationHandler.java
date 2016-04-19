package app;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import adminOps.Response;
import connect.Connect;
import pojos.EmailVerificationReqObj;
import pojos.EmailVerificationResObj;
import pojos.ReqObj;
import pojos.ResObj;

public class EmailVerificationHandler extends Connect implements AppHandler {

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

		LOGGER.fine("Inside Process Method " + rq.getVerification());

		try {
			getConnection();
			String select_status_sql = "Select user_status,user_id FROM users WHERE user_activation=?";
			PreparedStatement ps1 = connection.prepareStatement(select_status_sql);
			ps1.setString(1, rq.getVerification());
			
			ResultSet result1 = ps1.executeQuery();
			
			if(result1.next()){
				if(result1.getString("user_status").equals("email_pending")){
					String update_status_sql = "UPDATE users SET user_status=? WHERE user_activation=?";
					LOGGER.fine("Creating Statement...");
					PreparedStatement ps2 = connection.prepareStatement(update_status_sql);
					ps2.setString(1, "email_activated");
					ps2.setString(2, rq.getVerification());

					LOGGER.fine("statement created...executing update to users query");
					int result2 = ps2.executeUpdate();

					LOGGER.fine("Update Query Result : " + result2);
					
					if (result2 == 1) {
						rs.setCode(FLS_SUCCESS);
						rs.setUserId(result1.getString("user_id"));
						rs.setMessage("Your account has been activated!!");
					} else {
						rs.setCode(FLS_SQL_EXCEPTION);
						rs.setUserId("");
						rs.setMessage("Could not activate your account due to some internal problems!! Trying to fix it ASAP");
					}
				}else{
					rs.setCode(FLS_END_OF_DB);
					rs.setUserId("");
					rs.setMessage("This account is already activated!! No point activating it again");
				}
			}else{
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setUserId("");
				rs.setMessage("This account is not registered!! I wonder how you got this link");
			}

		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			System.out.println("Error Check Stacktrace");
			e.printStackTrace();
		}
		LOGGER.fine("Finished process method ");
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

}
