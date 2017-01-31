package app;

import java.sql.Connection;
import java.sql.PreparedStatement;

import connect.Connect;
import pojos.EditFriendReqObj;
import pojos.EditFriendResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.OAuth;

public class EditFriendHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(EditFriendHandler.class.getName());

	private static EditFriendHandler instance = null;

	public static EditFriendHandler getInstance() {
		if (instance == null)
			instance = new EditFriendHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside Process Method of EditFriendHandler");

		EditFriendReqObj rq = (EditFriendReqObj) req;
		EditFriendResObj rs = new EditFriendResObj();

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int rs1;

		try {

			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if (!oauthcheck.equals(rq.getUserId())) {
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}

			String updateFriendDetails = "UPDATE friends SET friend_full_name=?, friend_mobile=? WHERE friend_id=? AND friend_user_id=?";
			ps1 = hcp.prepareStatement(updateFriendDetails);
			ps1.setString(1, rq.getFriendName());
			ps1.setString(2, rq.getFriendMobile());
			ps1.setString(3, rq.getFriendId());
			ps1.setString(4, rq.getUserId());

			rs1 = ps1.executeUpdate();

			if (rs1 == 1) {
				LOGGER.info("Updated friend: " + rq.getFriendId() + " details of the userId " + rq.getUserId());
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
			} else {
				LOGGER.info("Not able to update friend: " + rq.getFriendId() + " details of the userId " + rq.getUserId());
				rs.setCode(FLS_END_OF_DB);
				rs.setMessage(FLS_END_OF_DB_M);
			}

		} catch (Exception e) {
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} finally {
			try {
				if (ps1 != null) ps1.close();
				if (hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		LOGGER.info("Finished process method ");
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
	}

}
