package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.GetCreditTimelineListResObj;
import pojos.GetCreditTimelineReqObj;
import pojos.GetCreditTimelineResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetCreditTimelineHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetCreditTimelineHandler.class.getName());

	private static GetCreditTimelineHandler instance = null;

	public static GetCreditTimelineHandler getInstance() {
		if (instance == null)
			instance = new GetCreditTimelineHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		GetCreditTimelineReqObj rq = (GetCreditTimelineReqObj) req;

		GetCreditTimelineListResObj rs = new GetCreditTimelineListResObj();
		Connection hcp = getConnectionFromPool();
		PreparedStatement sql_stmt = null;
		ResultSet dbResponse = null;

		LOGGER.info("Inside process method " + rq.getUserId() + ", " + rq.getCookie());
		// TODO: Core of the processing takes place here
		LOGGER.info("Inside GetCreditTimeline method");

		try {
			// storing the front end data in appropriate variable
			int offset = rq.getCookie();
			
			// Prepare SQL
			String sql = "SELECT * FROM `credit_log` WHERE credit_user_id=? ORDER BY credit_date DESC LIMIT ?,?";	
			sql_stmt = hcp.prepareStatement(sql);
			sql_stmt.setString(1, rq.getUserId());
			sql_stmt.setInt(2, rq.getCookie());
			sql_stmt.setInt(3, rq.getLimit());
			
			dbResponse = sql_stmt.executeQuery();
			
			if (dbResponse.next()) {
				dbResponse.previous();
				while (dbResponse.next()) {
					GetCreditTimelineResObj rs1 = new GetCreditTimelineResObj();
					rs1.setUserId(dbResponse.getString("credit_user_id"));
					rs1.setCredit_date(dbResponse.getString("credit_date"));
					rs1.setCredit_amount(dbResponse.getInt("credit_amount"));
					rs1.setCredit_type(dbResponse.getString("credit_type"));
					rs1.setDescription(dbResponse.getString("credit_desc"));
					rs.addResList(rs1);
					offset = offset + 1;
				}
				rs.setLastItemId(offset);
			} else {
				rs.setReturnCode(404);
				LOGGER.warning("End of DB");
			}

		} catch (SQLException e) {
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		} finally {
			try {
				if(dbResponse!=null) dbResponse.close();
				if(sql_stmt!=null) sql_stmt.close();
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
