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
import pojos.GetEngagementsByXListResObj;
import pojos.GetEngagementsByXReqObj;
import pojos.GetEngagementsByXResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetEngagementsByXHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetEngagementsByXHandler.class.getName());

	private static GetEngagementsByXHandler instance = null;

	public static GetEngagementsByXHandler getInstance() {
		if (instance == null)
			instance = new GetEngagementsByXHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		GetEngagementsByXReqObj rq = (GetEngagementsByXReqObj) req;

		GetEngagementsByXListResObj rs = new GetEngagementsByXListResObj();
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null,ps2=null;
		ResultSet rs1 = null,rs2=null;

		LOGGER.info("Inside process method " + rq.getUserId() + ", " + rq.getCookie());
		// TODO: Core of the processing takes place here
		LOGGER.info("Inside GetEngagementsByX method");

		try {

			// Prepare SQL
			String sql = null,minDateSql=null;
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
			String minDate= "";
			
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
			
			//get min date from credit_log table
			
			minDateSql = "SELECT *, MIN(credit_log_id) AS MinId FROM `credit_log`";
			
			if(userId!=null){
				minDateSql = minDateSql + " WHERE credit_user_id ='"+userId+"'";
				LOGGER.info(" Outer USer ID  is NOT null");
			}else{
				LOGGER.info(" Outer USer ID  null");
			}
			
			ps2 = hcp.prepareStatement(minDateSql);
			
			rs2 = ps2.executeQuery();
			LOGGER.info("Excuted Query");
			
			if(rs2.next()){
				minDate = rs2.getString("credit_date");
				}
			
			if (minDate==null || minDate.equals(null)) {
				rs.setCode(FLS_END_OF_DB);
				rs.setMessage(FLS_END_OF_DB_M);
				return rs;
			}
			
			LOGGER.info("1st Credit date is "+minDate);
			
			if(!sdf.parse(minDate).before(sdf.parse(toDate))){
				rs.setCode(FLS_END_OF_DB);
				rs.setMessage(FLS_END_OF_DB_M);
				LOGGER.warning(FLS_END_OF_DB_M);
				return rs;
			}
			
			while (intCount<limit && sdf.parse(minDate).before(sdf.parse(toDate))) {
				GetEngagementsByXResObj egmt = new GetEngagementsByXResObj();
			
				LOGGER.info("Inside While Loop");
				
				//already getting all data from events table
				sql = "SELECT SUM(tb1.credit_amount) AS totalCredits FROM `credit_log` tb1 WHERE ";
				
				sql = sql + "tb1.credit_date BETWEEN '"+interimDate+"' AND '"+toDate+"'";
				
				if(userId!=null){
					sql = sql + "AND tb1.credit_user_id ='"+userId+"'";
					LOGGER.info("USer ID  is NOT null");
				}else{
					LOGGER.info("USer ID  null");
				}
				
				LOGGER.info(sql);
				
				ps1 = hcp.prepareStatement(sql);
		
				rs1 = ps1.executeQuery();
				LOGGER.info("Excuted Query");
				
				if (rs1.next()) {
						egmt.setTotalCredits(rs1.getInt("totalCredits"));
						egmt.setStartDate(interimDate);
						egmt.setEndDate(toDate);
						LOGGER.info("Response Start date is "+interimDate);
						LOGGER.info("Response To date is "+toDate);
						rs.addResList( egmt);	
				} else {
					 egmt.setTotalCredits(0);
					 egmt.setStartDate(interimDate);
					 egmt.setEndDate(toDate);
					rs.addResList(egmt);
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
