package app;

//import com.mysql.jdbc.PreparedStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.GetLeadsByXListResObj;
import pojos.GetLeadsByXReqObj;
import pojos.GetLeadsByXResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.FlsRating;

public class GetLeadsByXHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetLeadsByXHandler.class.getName());

	private static GetLeadsByXHandler instance = null;

	public static GetLeadsByXHandler getInstance() {
		if (instance == null)
			instance = new GetLeadsByXHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		GetLeadsByXReqObj rq = (GetLeadsByXReqObj) req;

		GetLeadsByXListResObj rs = new GetLeadsByXListResObj();
		Connection hcp = getConnectionFromPool();
		PreparedStatement sql_stmt = null;
		ResultSet dbResponse = null;

		LOGGER.info("Inside process method " + rq.getUserId() + ", " + rq.getCookie());
		// TODO: Core of the processing takes place here
		LOGGER.info("Inside GetLeadsByX method");

		try {

			// Prepare SQL
			String sql = null;
			
			// storing the front end data in appropriate variables
			int offset = rq.getCookie();
			int limit = rq.getLimit();
			String userId = rq.getUserId();
			String fromDate = rq.getFromDate();
			String toDate = rq.getToDate();
			String type = rq.getLeadType();
			
			//already getting all data from events table
			sql = "SELECT tb1.* FROM leads tb1 WHERE ";
			
			if(fromDate.equals(toDate)){
				sql = sql + "lead_datetime LIKE '"+fromDate+"%' AND ";
			}else{
				sql = sql + "lead_datetime BETWEEN '"+fromDate+"' AND '"+toDate+"' AND ";
			}
			
			sql = sql + "tb1.lead_type='" + type + "' ORDER BY tb1.lead_id DESC LIMIT " + offset + ","+limit;
			
			sql_stmt = hcp.prepareStatement(sql);

			dbResponse = sql_stmt.executeQuery();
			
			if (dbResponse.isBeforeFirst()) {
				while (dbResponse.next()) {
					GetLeadsByXResObj rs1 = new GetLeadsByXResObj();
					
					rs1.setLeadId(dbResponse.getInt("lead_id"));
					rs1.setLeadLogDate(dbResponse.getString("lead_datetime"));
					rs1.setLeadUserId(dbResponse.getString("lead_email"));
					rs1.setLeadType(dbResponse.getString("lead_type"));
					rs1.setLeadURL(dbResponse.getString("lead_url"));
								
					rs.addResList(rs1);
					offset = offset + 1;
				}
				rs.setLastLeadId(offset);
			} else {
				rs.setCode(FLS_END_OF_DB);
				rs.setMessage(FLS_END_OF_DB_M);
				LOGGER.warning(FLS_END_OF_DB_M);
			}

		} catch (SQLException e) {
			LOGGER.warning("Error Check Stacktrace");
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} finally {
			try {
				if(dbResponse!=null) dbResponse.close();
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
