package app;

//import com.mysql.jdbc.PreparedStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.GetEventsByXListResObj;
import pojos.GetEventsByXReqObj;
import pojos.GetEventsByXResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.FlsRating;

public class GetEventsByXHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetEventsByXHandler.class.getName());

	private static GetEventsByXHandler instance = null;

	public static GetEventsByXHandler getInstance() {
		if (instance == null)
			instance = new GetEventsByXHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		GetEventsByXReqObj rq = (GetEventsByXReqObj) req;

		GetEventsByXListResObj rs = new GetEventsByXListResObj();
		Connection hcp = getConnectionFromPool();
		PreparedStatement sql_stmt = null;
		ResultSet dbResponse = null;

		LOGGER.info("Inside process method " + rq.getUserId() + ", " + rq.getCookie());
		// TODO: Core of the processing takes place here
		LOGGER.info("Inside GetEventsByX method");

		try {

			// Prepare SQL
			String sql = null;
			
			// storing the front end data in appropriate variables
			int offset = rq.getCookie();
			int limit = rq.getLimit();
			String userId = rq.getUserId();
			String fromDate = rq.getFromDate();
			String toDate = rq.getToDate();
			String status = rq.getStatus();
			
			//already getting all data from events table
			sql = "SELECT tb1.* FROM events tb1 WHERE ";
			
			if(fromDate.equals(toDate)){
				sql = sql + "datetime LIKE '"+fromDate+"%'";
			}else{
				sql = sql + "datetime BETWEEN '"+fromDate+"' AND '"+toDate+"'";
			}
			
			switch (status) {
			case "FLS_ACTIVE_IN_APP":
				sql = sql + " AND tb1.archived='FLS_ACTIVE' AND tb1.event_type='FLS_EVENT_NOTIFICATION'";
				break;
			case "FLS_ARCHIVED_IN_APP":
				sql = sql + " AND tb1.archived='FLS_ARCHIVED' AND tb1.event_type='FLS_EVENT_NOTIFICATION'";
				break;
			case "FLS_NOT_IN_APP":
				sql = sql + " AND tb1.event_type='FLS_EVENT_NOT_NOTIFICATION'";
				break;
			case "FLS_ALL":
				break;

			default:
				break;
			}
			
			sql = sql + " ORDER BY tb1.event_id DESC LIMIT " + offset + ","+limit;
			
			LOGGER.info(sql);
			
			sql_stmt = hcp.prepareStatement(sql);

			dbResponse = sql_stmt.executeQuery();
			
			if (dbResponse.isBeforeFirst()) {
				while (dbResponse.next()) {
					GetEventsByXResObj rs1 = new GetEventsByXResObj();
					
					rs1.setEventId(dbResponse.getInt("event_id"));
					rs1.setEventLogDate(dbResponse.getString("datetime"));
					rs1.setFromUserId(dbResponse.getString("from_user_id"));
					rs1.setToUserId(dbResponse.getString("to_user_id"));
					rs1.setEventType(dbResponse.getString("event_type"));
					rs1.setReadStatus(dbResponse.getString("read_status"));
					rs1.setDeliveryStatus(dbResponse.getString("delivery_status"));
					rs1.setNotificationType(dbResponse.getString("notification_type"));
					rs1.setItemId(dbResponse.getInt("item_id"));
					rs1.setMessage(dbResponse.getString("message"));
					rs1.setStatus(dbResponse.getString("archived"));
								
					rs.addResList(rs1);
					offset = offset + 1;
				}
				rs.setLastItemId(offset);
			} else {
				rs.setCode(404);
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
