package app;

import java.sql.PreparedStatement;
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
			String sql = "UPDATE users SET user_status=? WHERE user_id=?";
			LOGGER.fine("Creating Statement...");
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, "1");
			ps.setString(2, rq.getVerification());

			LOGGER.fine("statement created...executing update to users query");
			int result = ps.executeUpdate();

			LOGGER.fine("Update Query Result : " + result);

			if (result == 1) {
				rs.setStatus("1");
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
			} else {
				rs.setStatus("");
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
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
