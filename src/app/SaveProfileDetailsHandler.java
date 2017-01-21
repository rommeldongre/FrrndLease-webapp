package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import connect.Connect;
import pojos.ReqObj;
import pojos.ResObj;
import pojos.SaveProfileDetailsReqObj;
import pojos.SaveProfileDetailsResObj;
import util.FlsLogger;
import util.OAuth;

public class SaveProfileDetailsHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(SaveProfileDetailsHandler.class.getName());

	private static SaveProfileDetailsHandler instance;

	public static SaveProfileDetailsHandler getInstance() {
		if (instance == null)
			instance = new SaveProfileDetailsHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside Process Method of SaveProfileDetailsHandler");

		SaveProfileDetailsReqObj rq = (SaveProfileDetailsReqObj) req;
		SaveProfileDetailsResObj rs = new SaveProfileDetailsResObj();

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int rs1;

		try {

			String userId = rq.getUserId();

			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if (!oauthcheck.equals(userId)) {
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}

			String sql = "UPDATE users SET ";
			
			switch (rq.getDetailsType()) {
			case "ADDRESS":
				sql = sql + "user_address=? WHERE user_id=?";
				ps1 = hcp.prepareStatement(sql);
				ps1.setString(1, rq.getUserAddress());
				ps1.setString(2, userId);
				break;
			case "ABOUT":
				sql = sql + "about=? WHERE user_id=?";
				ps1 = hcp.prepareStatement(sql);
				ps1.setString(1, rq.getAbout());
				ps1.setString(2, userId);
				break;
			case "CONTACT_INFO":
				sql = sql + "website=?, email=?, phone_no=?, business_hours=? WHERE user_id=?";
				ps1 = hcp.prepareStatement(sql);
				ps1.setString(1, rq.getWebsite());
				ps1.setString(2, rq.getEmail());
				ps1.setString(3, rq.getPhoneNo());
				ps1.setString(4, rq.getBusinessHours());
				ps1.setString(5, userId);
				break;
			}

			rs1 = ps1.executeUpdate();

			if (rs1 == 1) {
				LOGGER.info("Updated the " + rq.getDetailsType() + " for the user id - " + userId);
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
			} else {
				LOGGER.info("Not able to update the " + rq.getDetailsType() + " for the user id - " + userId);
				rs.setCode(FLS_INVALID_MESSAGE);
				rs.setMessage(FLS_INVALID_MESSAGE_M);
			}

		} catch (Exception e) {
			e.printStackTrace();
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
		} finally {
			try {
				if (ps1 != null) ps1.close();
				if (hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		LOGGER.info("Finished Process Method");
		return rs;
	}

	@Override
	public void cleanup() {
	}

}
