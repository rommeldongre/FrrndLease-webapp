package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.GetRequestsPlusReqObj;
import pojos.GetRequestsPlusResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetRequestsPlusHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetRequestsPlusHandler.class.getName());

	private static GetRequestsPlusHandler instance = null;

	public static GetRequestsPlusHandler getInstance() {
		if (instance == null)
			return new GetRequestsPlusHandler();
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

		GetRequestsPlusReqObj rq = (GetRequestsPlusReqObj) req;

		GetRequestsPlusResObj rs = new GetRequestsPlusResObj();
		Connection hcp = getConnectionFromPool();
		hcp.setAutoCommit(false);

		LOGGER.info("Inside process method " + rq.getUserId() + ", " + rq.getCookie());
		// TODO: Core of the processing takes place here
		
		PreparedStatement stmt = null;
		ResultSet dbResponse = null;
		LOGGER.info("Inside GetRequests process method");

		try {
			String sql = "SELECT tb1.request_date, tb1.request_item_id, tb1.request_id, tb1.request_requser_id, tb1.request_status, tb2.item_name, tb2.item_desc, tb2.item_user_id, tb2.item_category, tb2.item_lease_value, tb2.item_lease_term, tb2.item_image, tb2.item_uid, tb3.user_full_name, tb3.user_mobile, tb3.user_address, tb3.user_locality, tb3.user_sublocality FROM requests tb1 INNER JOIN items tb2 on tb1.request_item_id = tb2.item_id INNER JOIN users tb3 on tb1.request_requser_id = tb3.user_id WHERE tb2.item_user_id=? AND tb1.request_id>? HAVING tb1.request_status=? ORDER by tb1.request_id ASC LIMIT 1";
			LOGGER.info("Creating a statement .....");
		    stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing GetRequests query...");
			stmt.setString(1, rq.getUserId());
			stmt.setInt(2, rq.getCookie());
			stmt.setString(3, "Active");

			dbResponse = stmt.executeQuery();

			if (!dbResponse.next()) {
				//check = dbResponse.getString("request_item_id");
				System.out.println("Empty result while firing select query on 1st table(requests)");
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				hcp.rollback();
				hcp.close();
				return rs;
			   }
			
			rs.setCode(FLS_SUCCESS);
			rs.setId(rq.getUserId());
			rs.setMessage(FLS_SUCCESS_M);
			rs.setRequestDate(dbResponse.getString("request_date"));
			rs.setRequestorId(dbResponse.getString("request_requser_id"));
			rs.setRequestUserId(dbResponse.getString("item_user_id"));
			rs.setRequestUserName(dbResponse.getString("user_full_name"));
			rs.setTitle(dbResponse.getString("item_name"));
			rs.setDescription(dbResponse.getString("item_desc"));
			rs.setRequestId(dbResponse.getInt("request_id"));
			rs.setRequestItemId(dbResponse.getInt("request_item_id"));
			rs.setCategory(dbResponse.getString("item_category"));
			rs.setLeaseValue(dbResponse.getString("item_lease_value"));
			rs.setLeaseTerm(dbResponse.getString("item_lease_term"));
			rs.setImage(dbResponse.getString("item_image"));
			rs.setUid(dbResponse.getString("item_uid"));
			rs.setRequestor_mobile(dbResponse.getString("user_mobile"));
			rs.setRequestor_address(dbResponse.getString("user_address"));
			rs.setRequestor_locality(dbResponse.getString("user_locality"));
			rs.setRequestor_sublocality(dbResponse.getString("user_sublocality"));
			hcp.commit();
		} catch (SQLException e) {
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		} finally {
			dbResponse.close();
			stmt.close();
			hcp.close();
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
