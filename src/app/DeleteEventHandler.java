package app;

//import com.mysql.jdbc.PreparedStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.DeleteEventReqObj;
import pojos.DeleteEventResObj;

import pojos.ReqObj;
import pojos.ResObj;
import util.OAuth;
import util.FlsLogger;

public class DeleteEventHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(DeleteEventHandler.class.getName());

	private static DeleteEventHandler instance = null;

	public static DeleteEventHandler getInstance() {
		if (instance == null)
			instance = new DeleteEventHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		DeleteEventReqObj rq = (DeleteEventReqObj) req;
		DeleteEventResObj rs = new DeleteEventResObj();
		LOGGER.info("Inside process method " + rq.getEventId() + ", " + rq.getUserId());
		// TODO: Core of the processing takes place here

		LOGGER.info("inside DeleteEventHandler method");
		String sql2 = "SELECT * FROM events WHERE event_id=? AND to_user_id=?"; //

		PreparedStatement stmt2=null,stmt=null;
		ResultSet rs1 =null;
		Connection hcp = getConnectionFromPool();

		try {
			
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if(!oauthcheck.equals(rq.getUserId())){
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}

			LOGGER.info("Creating Statement....");
			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setInt(1, rq.getEventId());
			stmt2.setString(2, rq.getUserId());
			rs1 = stmt2.executeQuery();
			
			
			if (rs1.next()) {
				String sql = "UPDATE events SET archived=? WHERE event_id=? AND to_user_id=?"; //
				String status = "FLS_ARCHIVED";
				stmt = hcp.prepareStatement(sql);

				LOGGER.info("Statement created. Executing edit query on ..." + rq.getEventId());
				stmt.setString(1, status);
				stmt.setInt(2, rq.getEventId());
				stmt.setString(3, rq.getUserId());
				stmt.executeUpdate();
				LOGGER.warning("Archived event id : " + rq.getEventId());
	
				rs.setMessage(FLS_DELETE_EVENT);
				rs.setCode(FLS_SUCCESS);
			} else {
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				LOGGER.warning("Entry not found in database!!");
			}
		} catch (SQLException e) {
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (NullPointerException e) {
			rs.setCode(FLS_NULL_POINT);
			rs.setMessage(FLS_NULL_POINT_M);
		} finally {
			if(rs1 != null)rs1.close();
			if(stmt != null)stmt.close();
			if(stmt2 != null)stmt2.close();
			if(hcp != null)hcp.close();
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
