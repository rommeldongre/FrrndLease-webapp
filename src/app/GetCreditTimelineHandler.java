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
		Connection hcp = getReadConnectionFromPool();
		PreparedStatement sql_stmt = null;
		ResultSet dbResponse = null;

		LOGGER.info("Inside process method " + rq.getUserId() + ", " + rq.getCookie());
		// TODO: Core of the processing takes place here
		LOGGER.info("Inside GetCreditTimeline method");

		try {
			// storing the front end data in appropriate variable
			int offset = rq.getCookie();
			int limit = rq.getLimit();
			int credit_id = rq.getCreditId();
			String user_id= rq.getUserId();
			
			// Prepare SQL
			String sql = "SELECT tb_credit.credit_log_id , tb_credit.credit_date, tb_credit.credit_user_id, tb_users.user_full_name, tb_credit.credit_amount, tb_credit.credit_type, tb_credit.credit_desc FROM `credit_log` tb_credit INNER JOIN users tb_users ON tb_credit.credit_user_id = tb_users.user_id WHERE ";
			
			if(credit_id!=-1){
				 sql = sql + "tb_credit.credit_log_id='"+credit_id+"' AND ";
			}
					
				   sql = sql + "tb_credit.credit_user_id='"+user_id+"' ORDER BY tb_credit.credit_date DESC LIMIT " + offset + ","+limit;	
			
			sql_stmt = hcp.prepareStatement(sql);
			
			dbResponse = sql_stmt.executeQuery();
			
			if (dbResponse.isBeforeFirst()) {
				while (dbResponse.next()) {
					GetCreditTimelineResObj rs1 = new GetCreditTimelineResObj();
					rs1.setUserId(dbResponse.getString("credit_user_id"));
					rs1.setUserName(dbResponse.getString("user_full_name"));
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
				rs.setErrorString("End of DB");
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
