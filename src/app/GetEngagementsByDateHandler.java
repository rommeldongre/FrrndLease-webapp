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
import pojos.GetEngagementsByDateListResObj;
import pojos.GetEngagementsByDateReqObj;
import pojos.GetEngagementsByDateResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetEngagementsByDateHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetEngagementsByDateHandler.class.getName());

	private static GetEngagementsByDateHandler instance = null;

	public static GetEngagementsByDateHandler getInstance() {
		if (instance == null)
			instance = new GetEngagementsByDateHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		GetEngagementsByDateReqObj rq = (GetEngagementsByDateReqObj) req;

		GetEngagementsByDateListResObj rs = new GetEngagementsByDateListResObj();
		Connection hcp = getConnectionFromPool();
		PreparedStatement sql_stmt = null,edgestmt = null;
		ResultSet dbResponse = null, edgeDbResponse = null;

		LOGGER.info("Inside process method " + rq.getUserId() + ", " + rq.getCookie());
		// TODO: Core of the processing takes place here
		LOGGER.info("Inside GetEngagementsByDate method");

		try {

			// Prepare SQL
			String sql = null;
			int intCount=0,countIncrement=1,diff=-1;
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
			
			if(interval.equals("weekly")){
				diff = -7;
				LOGGER.info("Weekly duration selected ");
			}else if(interval.equals("monthly")){
				diff = -30;
				LOGGER.info("Monthly duration selected ");
			}else{
				diff = -1;
				LOGGER.info("Daily duration selected ");
			}
			
			LOGGER.info("User ID is "+userId);
			LOGGER.info("Start date is "+interimDate);
			c.setTime(sdf.parse(interimDate));
			c.add(Calendar.DATE, diff);  // number of days to subtract
			interimDate = sdf.format(c.getTime());
			
			LOGGER.info("Start date is "+interimDate);
			LOGGER.info("To date is "+toDate);
			LOGGER.info("Before While Loop");
			
				while (intCount<limit) {
					GetEngagementsByDateResObj rs1 = new GetEngagementsByDateResObj();
				
					LOGGER.info("Inside While Loop");
					//already getting all data from events table
					sql = "SELECT SUM(tb1.credit_amount) AS totalCredits FROM `credit_log` tb1 WHERE ";
					
					sql = sql + "tb1.credit_date BETWEEN '"+interimDate+"' AND '"+toDate+"'";
					
					if(userId!=null){
						sql = sql + "AND tb1.credit_user_id ='"+userId+"'";
					}else{
						LOGGER.info("USer ID  null");
					}
					
					LOGGER.info(sql);
					
					sql_stmt = hcp.prepareStatement(sql);
		
					dbResponse = sql_stmt.executeQuery();
					LOGGER.info("Excuted Query");
					
					if (dbResponse.next()) {
							rs1.setTotalCredits(dbResponse.getInt("totalCredits"));
							rs1.setStartDate(interimDate);
							rs1.setEndDate(toDate);
							LOGGER.info("Response Start date is "+interimDate);
							LOGGER.info("Response To date is "+toDate);
							rs.addResList(rs1);	
					} else {
						rs1.setTotalCredits(0);
						rs1.setStartDate(interimDate);
						rs1.setEndDate(toDate);
						rs.addResList(rs1);
					}
					
					toDate = interimDate;
					c.add(Calendar.DATE, diff);  // number of days to subtract
					interimDate = sdf.format(c.getTime());
					intCount = intCount + countIncrement;
					sql= null;
					LOGGER.info("New Start date is "+interimDate);
					LOGGER.info("To date is "+toDate);
				}
				
			LOGGER.info("After While Loop");
			rs.setCode(FLS_SUCCESS);
			rs.setMessage(FLS_SUCCESS_M);
			rs.setLastEngagementId(toDate);
		} catch (SQLException e) {
			LOGGER.warning("Error Check Stacktrace");
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} finally {
			try {
				if(dbResponse!=null) dbResponse.close();
				if(edgeDbResponse!=null) edgeDbResponse.close();
				if(edgestmt!=null) edgestmt.close();
				if(sql_stmt!=null) sql_stmt.close();
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
