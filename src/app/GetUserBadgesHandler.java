package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import connect.Connect;
import pojos.GetUserBadgesReqObj;
import pojos.GetUserBadgesResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetUserBadgesHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetUserBadgesHandler.class.getName());

	private static GetUserBadgesHandler instance = null;

	public static GetUserBadgesHandler getInstance() {
		if (instance == null)
			instance = new GetUserBadgesHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside Process Method of GetUserBadgesHandler");
		GetUserBadgesReqObj rq = (GetUserBadgesReqObj) req;
		GetUserBadgesResObj rs = new GetUserBadgesResObj();

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null, ps3 = null;
		ResultSet rs1 = null, rs2 = null, rs3 = null;

		try {

			String userId = rq.getUserId();
			LOGGER.info("Getting badges for user id - " + userId);

			String sqlGetProfile = "SELECT user_status, user_verified_flag, (SELECT COUNT(*) FROM items WHERE item_user_id=? AND item_status NOT IN ('Archived','Wished')) AS items_posted, (SELECT COUNT(*) FROM leases WHERE lease_user_id=?) AS total_leases FROM users WHERE user_id=?";
			ps1 = hcp.prepareStatement(sqlGetProfile);
			ps1.setString(1, userId);
			ps1.setString(2, userId);
			ps1.setString(3, userId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
				rs.setIdVerified(rs1.getBoolean("user_verified_flag"));
				rs.setSignUpStatus(rs1.getString("user_status"));
				rs.setItemsPosted(rs1.getInt("items_posted"));
				rs.setLeaseCount(rs1.getInt("total_leases"));
				rs.setResponseTime(-1);
			}
			
			LOGGER.info("Posted items count - " + rs.getItemsPosted() + " Total Leases - " + rs.getLeaseCount());
			
			String sqlGetRequests = "SELECT from_user_id, datetime, item_id FROM `events` WHERE to_user_id=? AND notification_type IN ('FLS_MAIL_MAKE_REQUEST_TO')";
			ps2 = hcp.prepareStatement(sqlGetRequests);
			ps2.setString(1, userId);
			
			rs2 = ps2.executeQuery();
			
			int count = 0;
			long seconds = 0;
			
			while(rs2.next()){
				String sqlGetRequestResponse = "SELECT TIMESTAMPDIFF(SECOND, ?, datetime) AS res FROM `events` WHERE from_user_id=? AND to_user_id=? AND item_id=? AND notification_type IN ('FLS_MAIL_REJECT_REQUEST_TO', 'FLS_MAIL_GRANT_LEASE_TO_SELF', 'FLS_MAIL_GRANT_LEASE_TO_PRIME') LIMIT 1";
				ps3 = hcp.prepareStatement(sqlGetRequestResponse);
				ps3.setString(1, rs2.getString("datetime"));
				ps3.setString(2, userId);
				ps3.setString(3, rs2.getString("from_user_id"));
				ps3.setInt(4, rs2.getInt("item_id"));
				
				rs3 = ps3.executeQuery();
				
				if(rs3.next()){
					count++;
					seconds = seconds + Math.abs(rs3.getLong("res"));
					LOGGER.info("Count - " + count + " Total Seconds - " + seconds + " For Item Id - " + rs2.getInt("item_id"));
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs3 != null) rs3.close();
				if (ps3 != null) ps3.close();
				if (rs2 != null) rs2.close();
				if (ps2 != null) ps2.close();
				if (rs1 != null) rs1.close();
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
