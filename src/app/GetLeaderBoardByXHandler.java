package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.GetLeaderBoardByXListResObj;
import pojos.GetLeaderBoardByXReqObj;
import pojos.GetLeaderBoardByXResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetLeaderBoardByXHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetLeaderBoardByXHandler.class.getName());

	private static GetLeaderBoardByXHandler instance = null;

	public static GetLeaderBoardByXHandler getInstance() {
		if (instance == null)
			return new GetLeaderBoardByXHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside Process Method of GetLeaderBoardByXHandler");

		GetLeaderBoardByXReqObj rq = (GetLeaderBoardByXReqObj) req;
		GetLeaderBoardByXListResObj rs = new GetLeaderBoardByXListResObj();

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null, ps3 = null;
		ResultSet rs1 = null, rs2 = null, rs3 = null;
		
		// number of rows we want
		int limit = rq.getLimit();

		try {
			LOGGER.info("Getting the most requested items...");
			String GetItemSql = "SELECT tb1.request_item_id AS most_requested_item_id, COUNT(tb1.request_item_id) AS request_count, tb2.item_name AS most_requested_item_name, tb3.user_full_name AS most_requested_item_user_name FROM `requests` tb1 INNER JOIN `items` tb2 ON tb1.request_item_id = tb2.item_id INNER JOIN users tb3 ON tb2.item_user_id = tb3.user_id GROUP BY tb1.request_item_id ORDER BY COUNT(tb1.request_item_id) DESC, tb1.request_lastmodified DESC LIMIT ?";
			ps1 = hcp.prepareStatement(GetItemSql);
			ps1.setInt(1, limit);
			rs1 = ps1.executeQuery();

			LOGGER.info("Getting the highest credits value");
			String GetUsersSql = "SELECT tb1.user_credit AS highest_credit_value, tb1.user_full_name AS highest_credit_user FROM users tb1 ORDER BY tb1.user_credit DESC LIMIT ?";
			ps2 = hcp.prepareStatement(GetUsersSql);
			ps2.setInt(1, limit);
			rs2 = ps2.executeQuery();

			LOGGER.info("Getting monthly credit users");
			String GetUsersMonthlySql = "SELECT tb1.credit_user_id AS credit_monthly_user, SUM(tb1.credit_amount) AS totalCredit_monthly, tb2.user_full_name AS monthly_credit_user FROM `credit_log` tb1 LEFT JOIN users tb2 ON tb1.credit_user_id = tb2.user_id WHERE tb1.credit_date BETWEEN (CURDATE() - INTERVAL 30 DAY) AND CURDATE() GROUP BY credit_user_id ORDER BY totalCredit_monthly DESC LIMIT ?";
			ps3 = hcp.prepareStatement(GetUsersMonthlySql);
			ps3.setInt(1, limit);
			rs3 = ps3.executeQuery();

			for (int i = 0; i < limit; i++) {
				
				GetLeaderBoardByXResObj leaderRow = new GetLeaderBoardByXResObj();
				
				if (rs1.next()) {
					leaderRow.setMostRequestedItemName(rs1.getString("most_requested_item_name"));
					leaderRow.setMostRequestedItemUserName(rs1.getString("most_requested_item_user_name"));
				}

				if (rs2.next()) {
					leaderRow.setHighestCreditValue(rs2.getInt("highest_credit_value"));
					leaderRow.setHighestCreditUser(rs2.getString("highest_credit_user"));
				}

				if (rs3.next()) {
					leaderRow.setMonthlyCreditUserName(rs3.getString("monthly_credit_user"));
					leaderRow.setTotalCreditMonthly(rs3.getInt("totalCredit_monthly"));
				}
				
				rs.addResList(leaderRow);
				
			}
			
		} catch (SQLException e) {
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		} catch (Exception e) {
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		} finally {
			try {
				if (rs1 != null) rs1.close();
				if (rs2 != null) rs2.close();
				if (rs3 != null) rs3.close();
				if (ps1 != null) ps1.close();
				if (ps2 != null) ps2.close();
				if (ps3 != null) ps3.close();
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
