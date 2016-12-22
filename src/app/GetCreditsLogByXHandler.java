package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import connect.Connect;
import pojos.GetCreditsLogByXListResObj;
import pojos.GetCreditsLogByXReqObj;
import pojos.GetCreditsLogByXResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetCreditsLogByXHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetCreditsLogByXHandler.class.getName());

	private static GetCreditsLogByXHandler instance = null;

	public static GetCreditsLogByXHandler getInstance() {
		if (instance == null)
			instance = new GetCreditsLogByXHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		GetCreditsLogByXReqObj rq = (GetCreditsLogByXReqObj) req;

		GetCreditsLogByXListResObj rs = new GetCreditsLogByXListResObj();
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;

		LOGGER.info("Inside process method " + rq.getUserId() + ", " + rq.getCookie());
		// TODO: Core of the processing takes place here
		LOGGER.info("Inside GetCreditsLogByX method");

		try {

			// Prepare SQL
			String sql = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar c = Calendar.getInstance();
			
			// storing the front end data in appropriate variables
			int offset = rq.getCookie();
			int limit = rq.getLimit();
			String userId = rq.getUserId();
			String fromDate = rq.getFromDate();
			String toDate = rq.getToDate();
			String interval = rq.getInterval();
			String interimDate = toDate;
					
					//already getting all data from events table
					sql = "SELECT tb1.credit_date, tb1.credit_amount, tb1.credit_type, tb2.user_full_name FROM `credit_log` tb1 INNER JOIN users tb2 WHERE ";
					
					sql = sql + "tb1.credit_date BETWEEN '"+fromDate+"' AND '"+toDate+"'";
					
					if(userId!=null){
						sql = sql + "AND tb1.credit_user_id ='"+userId+"'";
						LOGGER.info("USer ID  is NOT null");
					}else{
						LOGGER.info("USer ID  null");
					}
					
					sql = sql + " AND tb1.credit_user_id=tb2.user_id ORDER BY tb1.credit_date DESC LIMIT " + offset + ","+limit;
					
					LOGGER.info(sql);
					
					ps1 = hcp.prepareStatement(sql);
		
					rs1 = ps1.executeQuery();
					LOGGER.info("Excuted Query");
					
					if (rs1.isBeforeFirst()) {
						LOGGER.info("inside if statement");
						while (rs1.next()) {
							LOGGER.info("Inside While Loop");
							GetCreditsLogByXResObj credtlog = new GetCreditsLogByXResObj();
							
							credtlog.setCredits(rs1.getInt("credit_amount"));
							credtlog.setCreditDate(rs1.getString("credit_date"));
							credtlog.setCreditType(rs1.getString("credit_type"));
							credtlog.setUserName(rs1.getString("user_full_name"));
							rs.addResList(credtlog);	
							offset = offset + 1;
							
							LOGGER.info("Response Start date is "+interimDate);
							LOGGER.info("Response To date is "+toDate);
							LOGGER.info("after if statement");
							
							toDate = rs1.getString("credit_date");
						}
					} else {
						rs.setCode(FLS_END_OF_DB);
						rs.setMessage(FLS_END_OF_DB_M);
						LOGGER.warning(FLS_END_OF_DB_M);
					}
			rs.setLastEngagementId(offset);
		} catch (SQLException e) {
			LOGGER.warning("Error Check Stacktrace");
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} finally {
			try {
				if(rs1!=null) rs1.close();
				if(ps1!=null) ps1.close();
				if(hcp!=null) hcp.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
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
