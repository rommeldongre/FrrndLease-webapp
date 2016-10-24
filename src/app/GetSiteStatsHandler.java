package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import adminOps.Response;
import connect.Connect;
import pojos.GetSiteStatsReqObj;
import pojos.GetSiteStatsResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetSiteStatsHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetSiteStatsHandler.class.getName());

	private Response res = new Response();

	private static GetSiteStatsHandler instance = null;

	public static GetSiteStatsHandler getInstance() {
		if (instance == null)
			return new GetSiteStatsHandler();
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
		GetSiteStatsReqObj rq = (GetSiteStatsReqObj) req;
		GetSiteStatsResObj rs = new GetSiteStatsResObj();
		PreparedStatement ps = null;
		ResultSet result = null;
		Connection hcp = getConnectionFromPool();
		try {
			String GetStatsSql = "SELECT ( SELECT COUNT(*) FROM users ) AS user_count, ( SELECT COUNT(*) FROM items where item_status='InStore' ) AS item_count, ( SELECT COUNT(*) FROM requests WHERE request_status='Active' ) AS request_count, ( SELECT COUNT(*) FROM leases WHERE lease_status='Active' ) AS lease_count FROM dual";
			LOGGER.info("Creating Statement");

			ps = hcp.prepareStatement(GetStatsSql);
			LOGGER.info("Created statement...executing select query for Count");

			result = ps.executeQuery();

			if (result.next()) {
				rs.setCode(FLS_SUCCESS);
				rs.setMessage("Success");
				rs.setItemCount(result.getInt("item_count"));
				rs.setLeaseCount(result.getInt("lease_count"));
				rs.setRequestCount(result.getInt("request_count"));
				rs.setUserCount(result.getInt("user_count"));
				LOGGER.info("Inside if statement");
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
				if(result!=null) result.close();
				if(ps!=null) ps.close();
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
