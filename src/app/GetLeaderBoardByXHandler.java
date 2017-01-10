package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import adminOps.Response;
import connect.Connect;
import pojos.GetEventsByXResObj;
import pojos.GetLeaderBoardByXListResObj;
import pojos.GetLeaderBoardByXReqObj;
import pojos.GetLeaderBoardByXResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetLeaderBoardByXHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetLeaderBoardByXHandler.class.getName());

	private Response res = new Response();

	private static GetLeaderBoardByXHandler instance = null;

	public static GetLeaderBoardByXHandler getInstance() {
		if (instance == null)
			return new GetLeaderBoardByXHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub

		LOGGER.info("Inside Post Method");
		GetLeaderBoardByXReqObj rq = (GetLeaderBoardByXReqObj) req;
		GetLeaderBoardByXListResObj rs = new GetLeaderBoardByXListResObj();
		PreparedStatement ps1 = null,ps2=null,ps3=null;
		ResultSet itemsrs1 = null,usersrs2=null,usersrs3=null;
		Connection hcp = getConnectionFromPool();
		try {
			String GetItemSql = "SELECT tb1.request_item_id AS most_requested_item_id, tb2.item_name AS most_requested_item_name, tb3.user_full_name AS most_requested_item_user_name FROM `requests` tb1 INNER JOIN `items` tb2 ON tb1.request_item_id = tb2.item_id INNER JOIN users tb3 ON tb2.item_user_id = tb3.user_id GROUP BY tb1.request_item_id ORDER BY COUNT(tb1.request_item_id) DESC LIMIT 3";
			LOGGER.info("Creating 1st Statement");
			
			String GetUsersSql = "SELECT tb1.user_credit AS highest_credit_value, tb1.user_full_name AS highest_credit_user FROM users tb1 ORDER BY tb1.user_credit DESC LIMIT 3";
			LOGGER.info("Creating 2nd Statement");
			
			String GetUsersMonthlySql = "SELECT tb1.credit_user_id AS credit_monthly_user, SUM(tb1.credit_amount) AS totalCredit_monthly, tb2.user_full_name AS monthly_credit_user FROM `credit_log` tb1 LEFT JOIN users tb2 ON tb1.credit_user_id = tb2.user_id WHERE tb1.credit_date BETWEEN (CURDATE() - INTERVAL 30 DAY) AND CURDATE() GROUP BY credit_user_id ORDER BY totalCredit_monthly DESC LIMIT 3";
			LOGGER.info("Creating 2nd Statement");

			ps1 = hcp.prepareStatement(GetItemSql);
			ps2 = hcp.prepareStatement(GetUsersSql);
			ps3 = hcp.prepareStatement(GetUsersMonthlySql);
			LOGGER.info("Created statement...executing select query for Count");

			itemsrs1 = ps1.executeQuery();
			LOGGER.info("Created statement...executing select query for Items");
			
			usersrs2 = ps2.executeQuery();
			LOGGER.info("Created statement...executing select query for Users");
			
			usersrs3 = ps3.executeQuery();
			LOGGER.info("Created statement...executing select query for Users Monthly");

			if (itemsrs1.isBeforeFirst() && usersrs2.isBeforeFirst() && usersrs3.isBeforeFirst()) {
				while (itemsrs1.next() && usersrs2.next() && usersrs3.next()) {
					GetLeaderBoardByXResObj rs1 = new GetLeaderBoardByXResObj();
					
					rs1.setMostRequestedItemId(itemsrs1.getInt("most_requested_item_id"));
					rs1.setMostRequestedItemName(itemsrs1.getString("most_requested_item_name"));
					rs1.setMostRequestedItemUserName(itemsrs1.getString("most_requested_item_user_name"));
					rs1.setHighestCreditValue(usersrs2.getInt("highest_credit_value"));
					rs1.setHighestCreditUser(usersrs2.getString("highest_credit_user"));
					rs1.setMonthlyCreditUserId(usersrs3.getString("credit_monthly_user"));
					rs1.setMonthlyCreditUserName(usersrs3.getString("monthly_credit_user"));
					rs1.setTotalCreditMonthly(usersrs3.getInt("totalCredit_monthly"));
								
					rs.addResList(rs1);
					//offset = offset + 1;
				}
				rs.setLastLeadId(0);
			} else {
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				LOGGER.info("Inside else statement");
			}
		} catch (SQLException e) {
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		}catch (Exception e1) {
			LOGGER.warning("Error Check Stacktrace");
			e1.printStackTrace();
		}finally{
			try {
				if(itemsrs1!=null) itemsrs1.close();
				if(usersrs2!=null) usersrs2.close();
				if(usersrs3!=null) usersrs3.close();
				if(ps1!=null) ps1.close();
				if(ps2!=null) ps2.close();
				if(ps3!=null) ps3.close();
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

}
