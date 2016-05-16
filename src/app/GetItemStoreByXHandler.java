package app;

//import com.mysql.jdbc.PreparedStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.GetItemStoreByXListResObj;
import pojos.GetItemStoreByXReqObj;
import pojos.GetItemStoreByXResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetItemStoreByXHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetItemStoreByXHandler.class.getName());

	private static GetItemStoreByXHandler instance = null;

	public static GetItemStoreByXHandler getInstance() {
		if (instance == null)
			instance = new GetItemStoreByXHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		GetItemStoreByXReqObj rq = (GetItemStoreByXReqObj) req;

		GetItemStoreByXListResObj rs = new GetItemStoreByXListResObj();
		Connection hcp = getConnectionFromPool();

		LOGGER.info("Inside process method " + rq.getUserId() + ", " + rq.getCookie());
		// TODO: Core of the processing takes place here
		LOGGER.info("Inside GetItemStore method");

		try {

			// Prepare SQL
			String sql = null;
			PreparedStatement sql_stmt = null;

			// All Category in index page
			if (rq.getCategory() == null && rq.getUserId() == null) {

				sql = "SELECT tb1.*, tb2.user_full_name, tb2.user_location FROM items tb1 INNER JOIN users tb2 ON tb1.item_user_id = tb2.user_id WHERE tb1.item_id > ? AND tb1.item_status= 'InStore' ORDER BY item_id LIMIT ?";
				sql_stmt = hcp.prepareStatement(sql);

				sql_stmt.setInt(1, rq.getCookie());
				sql_stmt.setInt(2, rq.getLimit());
			}
			// Category selected in index page

			if (rq.getCategory() != null && rq.getUserId() == null) {
				sql = "SELECT tb1.*, tb2.user_full_name, tb2.user_location FROM items tb1 INNER JOIN users tb2 ON tb1.item_user_id = tb2.user_id WHERE tb1.item_id > ? AND tb1.item_status = 'InStore' AND tb1.item_category=? LIMIT ?";
				sql_stmt = hcp.prepareStatement(sql);

				sql_stmt.setInt(1, rq.getCookie());
				sql_stmt.setString(2, rq.getCategory());
				sql_stmt.setInt(3, rq.getLimit());
			}

			// All Category mypostings page
			if (rq.getCategory() == null && rq.getUserId() != null) {
				sql = "SELECT tb1.*, tb2.user_full_name, tb2.user_location FROM items tb1 INNER JOIN users tb2 ON tb1.item_user_id = tb2.user_id WHERE item_id > ? AND item_status = 'InStore' AND item_user_id=? LIMIT ?";
				sql_stmt = hcp.prepareStatement(sql);

				sql_stmt.setInt(1, rq.getCookie());
				sql_stmt.setString(2, rq.getUserId());
				sql_stmt.setInt(3, rq.getLimit());
			}

			// Category selected in mypostings page
			if (rq.getCategory() != null && rq.getUserId() != null) {

				sql = "SELECT tb1.*, tb2.user_full_name, tb2.user_location FROM items tb1 INNER JOIN users tb2 ON tb1.item_user_id = tb2.user_id WHERE item_id > ? AND item_status = 'InStore' AND item_category=? AND item_user_id=? LIMIT ?";
				sql_stmt = hcp.prepareStatement(sql);

				sql_stmt.setInt(1, rq.getCookie());
				sql_stmt.setString(2, rq.getCategory());
				sql_stmt.setString(3, rq.getUserId());
				sql_stmt.setInt(4, rq.getLimit());
			}

			ResultSet dbResponse = sql_stmt.executeQuery();

			if (dbResponse.next()) {
				dbResponse.previous();
				while (dbResponse.next()) {
					GetItemStoreByXResObj rs1 = new GetItemStoreByXResObj();
					rs1.setItemId(dbResponse.getInt("item_id"));
					rs1.setTitle(dbResponse.getString("item_name"));
					rs1.setCategory(dbResponse.getString("item_category"));
					rs1.setDesc(dbResponse.getString("item_desc"));
					rs1.setFullName(dbResponse.getString("user_full_name"));
					rs1.setLeaseValue(dbResponse.getInt("item_lease_value"));
					rs1.setLeaseTerm(dbResponse.getString("item_lease_term"));
					rs1.setImage(dbResponse.getString("item_image"));
					rs1.setStatus(dbResponse.getString("item_status"));
					rs1.setUid(dbResponse.getString("item_uid"));
					rs.addResList(rs1);
					rs.setLastItemId(dbResponse.getInt("item_id"));
				}
			} else {
				rs.setReturnCode(404);
				LOGGER.warning("End of DB");
			}
			dbResponse.close();
			sql_stmt.close();
			hcp.close();

		} catch (SQLException e) {
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
