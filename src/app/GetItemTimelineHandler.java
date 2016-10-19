package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.GetItemTimelineListResObj;
import pojos.GetItemTimelineReqObj;
import pojos.GetItemTimelineResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetItemTimelineHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetItemTimelineHandler.class.getName());
	
	private static GetItemTimelineHandler instance = null;
	
	public static GetItemTimelineHandler getInstance(){
		if(instance == null)
			instance = new GetItemTimelineHandler();
		return instance;
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
				GetItemTimelineReqObj rq = (GetItemTimelineReqObj) req;

				GetItemTimelineListResObj rs = new GetItemTimelineListResObj();
				Connection hcp = getConnectionFromPool();
				PreparedStatement sql_stmt = null;
				ResultSet dbResponse = null;

				LOGGER.info("Inside process method " + rq.getItemId() + ", " + rq.getCookie());
				// TODO: Core of the processing takes place here
				LOGGER.info("Inside GetItemTimeline method");

				try {
					// storing the front end data in appropriate variable
					int itemId = rq.getItemId();
					int offset = rq.getCookie();
					int limit = rq.getLimit();
					
					// Prepare SQL
					String sql = "SELECT * FROM `item_log` WHERE item_id=? ORDER BY item_log_date DESC LIMIT ?,?";	
					sql_stmt = hcp.prepareStatement(sql);
					sql_stmt.setInt(1, itemId);
					sql_stmt.setInt(2, offset);
					sql_stmt.setInt(3, limit);
					
					dbResponse = sql_stmt.executeQuery();
					
					if (dbResponse.isBeforeFirst()) {
						while (dbResponse.next()) {
							GetItemTimelineResObj rs1 = new GetItemTimelineResObj();
							rs1.setItemId(dbResponse.getInt("item_id"));
							rs1.setItemLogDate(dbResponse.getString("item_log_date"));
							rs1.setItemLogType(dbResponse.getString("item_log_type"));
							rs1.setItemLogDesc(dbResponse.getString("item_log_desc"));
							rs1.setItemLogImageLink(dbResponse.getString("item_log_image_link"));
							rs.addResList(rs1);
							offset = offset + 1;
						}
						rs.setCookie(offset);
						rs.setCode(FLS_SUCCESS);
						rs.setMessage(FLS_SUCCESS_M);
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
