package app;

//import com.mysql.jdbc.PreparedStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
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
		GetItemStoreByXResObj rs = new GetItemStoreByXResObj();
		
		LOGGER.info("Inside process method "+ rq.getUserId()+", "+ rq.getCookie());
		//TODO: Core of the processing takes place here
		LOGGER.info("Inside GetItemStore method");
		
		try {

			//Prepare SQL
			String sql = null;
			PreparedStatement sql_stmt = null;
			
			//All Category in index page
			if (rq.getCategory() == null && rq.getUserId() == null) {
				sql = "SELECT tb1.*, tb2.user_full_name, tb2.user_location FROM items tb1 INNER JOIN users tb2 ON tb1.item_user_id = tb2.user_id WHERE tb1.item_id > ? AND tb1.item_status= 'InStore' ORDER BY item_id LIMIT 1";
				
				sql_stmt = getConnectionFromPool().prepareStatement(sql);
				sql_stmt.setInt(1, rq.getCookie());
			} 
			// Category selected in index page
			if(rq.getCategory() != null && rq.getUserId() == null){
				sql = "SELECT tb1.*, tb2.user_full_name, tb2.user_location FROM items tb1 INNER JOIN users tb2 ON tb1.item_user_id = tb2.user_id WHERE tb1.item_id > ? AND tb1.item_status = 'InStore' AND tb1.item_category=? LIMIT 1";
				
				sql_stmt = getConnectionFromPool().prepareStatement(sql);
				sql_stmt.setInt(1, rq.getCookie());
				sql_stmt.setString(2, rq.getCategory());
			}
			//All Category mypostings page
			if(rq.getCategory() == null && rq.getUserId() != null){
				sql = "SELECT tb1.*, tb2.user_full_name, tb2.user_location FROM items tb1 INNER JOIN users tb2 ON tb1.item_user_id = tb2.user_id WHERE item_id > ? AND item_status = 'InStore' AND item_user_id=? LIMIT 1";
				sql_stmt = getConnectionFromPool().prepareStatement(sql);
				sql_stmt.setInt(1, rq.getCookie());
				sql_stmt.setString(2, rq.getUserId());
			}
			//Category selected in mypostings page
			if(rq.getCategory() != null && rq.getUserId() != null){
				sql = "SELECT tb1.*, tb2.user_full_name, tb2.user_location FROM items tb1 INNER JOIN users tb2 ON tb1.item_user_id = tb2.user_id WHERE item_id > ? AND item_status = 'InStore' AND item_category=? AND item_user_id=? LIMIT 1";
				sql_stmt = getConnectionFromPool().prepareStatement(sql);
				sql_stmt.setInt(1, rq.getCookie());
				sql_stmt.setString(2, rq.getCategory());
				sql_stmt.setString(3, rq.getUserId());
			}
			
			ResultSet dbResponse = sql_stmt.executeQuery();
			if(dbResponse.next()){
					rs.setItemId(dbResponse.getInt("item_id"));
					rs.setTitle(dbResponse.getString("item_name"));
					rs.setCategory(dbResponse.getString("item_category"));
					rs.setDesc(dbResponse.getString("item_desc"));
					rs.setFullName(dbResponse.getString("user_full_name"));
					rs.setLeaseValue(dbResponse.getInt("item_lease_value"));
					rs.setLeaseTerm(dbResponse.getString("item_lease_term"));
					rs.setImage(dbResponse.getString("item_image"));
					rs.setStatus(dbResponse.getString("item_status"));
					rs.setUid(dbResponse.getString("item_uid"));
					
			}else {
				rs.setReturnCode(404);
				LOGGER.warning("End of DB");
			}
		} catch (SQLException e) {
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		}	
		LOGGER.info("Finished process method ");
		//return the response
		
		return rs;
		
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
	}
}
