package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.GetRequestsByUserReqObj;
import pojos.GetRequestsByUserResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import adminOps.Response;

public class GetRequestsByUserHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetRequestsByUserHandler.class.getName());

	private String user_name, check = null, Id = null, token, message;
	private int Code;
	private Response res = new Response();

	private static GetRequestsByUserHandler instance = null;

	public static GetRequestsByUserHandler getInstance() {
		if (instance == null)
			instance = new GetRequestsByUserHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		GetRequestsByUserReqObj rq = (GetRequestsByUserReqObj) req;
		GetRequestsByUserResObj rs = new GetRequestsByUserResObj();
		Connection hcp = getConnectionFromPool();

		LOGGER.info("Inside process method " + rq.getUserId() + ", " + rq.getCookie());
		// TODO: Core of the processing takes place here
		check = null;
		LOGGER.info("Inside GetOutgoingrequests method");

		try {
			String sql = "SELECT tb1.request_date, tb1.request_item_id, tb1.request_id, tb1.request_status, tb2.item_name, tb2.item_desc, tb2.item_user_id, tb2.item_category, tb2.item_lease_value, tb2.item_lease_term, tb2.item_primary_image_link, tb2.item_uid, tb3.user_full_name, tb3.user_mobile, tb3.user_address, tb3.user_locality, tb3.user_sublocality FROM requests tb1 INNER JOIN items tb2 on tb1.request_item_id = tb2.item_id INNER JOIN users tb3 on tb2.item_user_id = tb3.user_id WHERE tb1.request_requser_id=? AND tb1.request_id>? HAVING tb1.request_status=? ORDER by tb1.request_id ASC LIMIT 1";
			LOGGER.info("Creating a statement .....");
			PreparedStatement stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing GetOutgoingrequests query...");
			stmt.setString(1, rq.getUserId());
			stmt.setInt(2, rq.getCookie());
			stmt.setString(3, "Active");

			ResultSet dbResponse = stmt.executeQuery();

			if (dbResponse.next()) {
				check = dbResponse.getString("request_item_id");

				if (check != null) {
					// Populate the response
					rs.setTitle(dbResponse.getString("item_name"));
					rs.setDesc(dbResponse.getString("item_desc"));
					rs.setOwner_Id(dbResponse.getString("item_user_id"));
					rs.setRequest_status(dbResponse.getString("request_status"));
					rs.setRequest_id(dbResponse.getInt("request_id"));
					rs.setRequest_item_id(dbResponse.getInt("request_item_id"));
					rs.setRequest_date(dbResponse.getString("request_date"));
					rs.setOwner_name(dbResponse.getString("user_full_name"));
					rs.setCategory(dbResponse.getString("item_category"));
					rs.setLeaseValue(dbResponse.getString("item_lease_value"));
					rs.setLeaseTerm(dbResponse.getString("item_lease_term"));
					rs.setPrimaryImageLink(dbResponse.getString("item_primary_image_link"));
					rs.setUid(dbResponse.getString("item_uid"));
					rs.setOwner_mobile(dbResponse.getString("user_mobile"));
					rs.setOwner_address(dbResponse.getString("user_address"));
					rs.setOwner_locality(dbResponse.getString("user_locality"));
					rs.setOwner_sublocality(dbResponse.getString("user_sublocality"));

					message = rs.getTitle() + ", " + rs.getDesc() + ", " + rs.getOwner_Id() + ", "
							+ rs.getRequest_status() + ", " + rs.getRequest_item_id() + ", " + rs.getRequest_date();
					LOGGER.info("Printing out Resultset: " + message);
					Code = FLS_SUCCESS;
					Id = check;
				} else {
					Id = "0";
					message = FLS_END_OF_DB_M;
					Code = FLS_END_OF_DB;
					rs.setErrorString("End of table reached");
				}
			}

			stmt.close();
			// res.setData(Code,Id,message);
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		} finally {
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
