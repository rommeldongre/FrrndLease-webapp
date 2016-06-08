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
			
			// storing the front end data in appropriate variables
			int offset = rq.getCookie();
			int limit = rq.getLimit();
			String category = rq.getCategory();
			String userId = rq.getUserId();
			Float lat = rq.getLat(), lng = rq.getLng();
			String searchString = rq.getSearchString();
			
			sql = "SELECT tb1.*";
			
			if(lat == 0.0 || lng == 0.0)
				sql = sql + ", 0 AS distance, tb2.user_full_name, tb2.user_locality, tb2.user_sublocality FROM items tb1 INNER JOIN users tb2 ON tb1.item_user_id = tb2.user_id WHERE tb1.item_status = 'InStore'";
			else
				sql = sql + ", ( 6371 * acos( cos( radians("+lat+") ) * cos( radians( tb1.item_lat ) ) * cos( radians( tb1.item_lng ) - radians("+lng+") ) + sin( radians("+lat+") ) * sin( radians( tb1.item_lat ) ) ) ) AS distance, tb2.user_full_name, tb2.user_locality, tb2.user_sublocality FROM items tb1 INNER JOIN users tb2 ON tb1.item_user_id = tb2.user_id WHERE tb1.item_status = 'InStore'";
			
			if(category != null)
				sql = sql + " AND tb1.item_category='"+category+"'";
			
			if(userId != null)
				sql = sql + " AND tb1.item_user_id='"+userId+"'";
			
			if(searchString != "" || searchString != null)
				sql = sql + " AND (tb1.item_name LIKE '%"+searchString+"%' OR tb1.item_desc LIKE '%"+searchString+"%')";
			
			sql = sql + " ORDER BY distance LIMIT "+offset+", "+limit;
			
			sql_stmt = hcp.prepareStatement(sql);

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
					rs1.setLocality(dbResponse.getString("user_locality"));
					rs1.setSublocality(dbResponse.getString("user_sublocality"));
					rs1.setDistance(dbResponse.getFloat("distance"));
					rs.addResList(rs1);
					offset = offset + 1;
				}
				rs.setLastItemId(offset);
			} else {
				rs.setReturnCode(404);
				LOGGER.warning("End of DB");
			}
			sql_stmt.close();

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
