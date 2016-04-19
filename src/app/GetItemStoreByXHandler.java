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
import adminOps.Response;


public class GetItemStoreByXHandler extends Connect implements AppHandler {
	
	private String user_name, check=null,Id=null,token, message, category;
	private int Code;
	private Response res = new Response();
	
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
		
		LOGGER.fine("Inside process method "+ rq.getUserId()+", "+ rq.getCookie());
		//TODO: Core of the processing takes place here
		check = null;
		LOGGER.fine("Inside GetItemStore method");
		
		try {
			getConnection();

			//Prepare SQL
			String sql = null;
			PreparedStatement sql_stmt = null;
			
			//if (rq.getCategory() == null && rq.getUserId().equals("myindex")) {
			//All category in index page
			if (rq.getCategory() == null && rq.getUserId() == null) {
				//sql = "SELECT * from items WHERE item_id > ? AND item_status = 'InStore' LIMIT 1";
				sql = "SELECT tb1.*, tb2.user_full_name, tb2.user_location FROM items tb1 INNER JOIN users tb2 ON tb1.item_user_id = tb2.user_id WHERE tb1.item_id > ? AND tb1.item_status= 'InStore' ORDER BY item_id LIMIT 1";
				
				sql_stmt = connection.prepareStatement(sql);
				sql_stmt.setInt(1, rq.getCookie());
			} 
			// category selected in index page
			if(rq.getCategory() != null && rq.getUserId() == null){
				//sql = "SELECT * from items WHERE item_id > ? AND item_status = 'InStore' AND item_category=? LIMIT 1";
				sql = "SELECT tb1.*, tb2.user_full_name, tb2.user_location FROM items tb1 INNER JOIN users tb2 ON tb1.item_user_id = tb2.user_id WHERE tb1.item_id > ? AND tb1.item_status = 'InStore' AND tb1.item_category=? LIMIT 1";
				
				sql_stmt = connection.prepareStatement(sql);
				sql_stmt.setInt(1, rq.getCookie());
				sql_stmt.setString(2, rq.getCategory());
			}
			//All category mypostings page
			if(rq.getCategory() == null && rq.getUserId() != null){
				//sql = "SELECT * from items WHERE item_id > ? AND item_status = 'InStore' AND item_user_id=? LIMIT 1";
				sql = "SELECT tb1.*, tb2.user_full_name, tb2.user_location FROM items tb1 INNER JOIN users tb2 ON tb1.item_user_id = tb2.user_id WHERE item_id > ? AND item_status = 'InStore' AND item_user_id=? LIMIT 1";
				sql_stmt = connection.prepareStatement(sql);
				sql_stmt.setInt(1, rq.getCookie());
				sql_stmt.setString(2, rq.getUserId());
			}
			//category selected in mypostings page
			if(rq.getCategory() != null && rq.getUserId() != null){
				//sql = "SELECT * from items WHERE item_id > ? AND item_status = 'InStore' AND item_category=? AND item_user_id=? LIMIT 1";
				sql = "SELECT tb1.*, tb2.user_full_name, tb2.user_location FROM items tb1 INNER JOIN users tb2 ON tb1.item_user_id = tb2.user_id WHERE item_id > ? AND item_status = 'InStore' AND item_category=? AND item_user_id=? LIMIT 1";
				sql_stmt = connection.prepareStatement(sql);
				sql_stmt.setInt(1, rq.getCookie());
				sql_stmt.setString(2, rq.getCategory());
				sql_stmt.setString(3, rq.getUserId());
			}
			
			ResultSet dbResponse = sql_stmt.executeQuery();
			if(dbResponse.next()){
				check = dbResponse.getString("item_name");
				
				if (check!= null) {
					//Populate the response

					rs.setItemId(dbResponse.getInt("item_id"));
					rs.setTitle(dbResponse.getString("item_name"));
					rs.setCategory(dbResponse.getString("item_category"));
					rs.setDesc(dbResponse.getString("item_desc"));
					rs.setFullName(dbResponse.getString("user_full_name"));
					rs.setLeaseValue(dbResponse.getInt("item_lease_value"));
					rs.setLeaseTerm(dbResponse.getString("item_lease_term"));
					rs.setImage(dbResponse.getString("item_image"));
					rs.setStatus(dbResponse.getString("item_status"));
					
					System.out.println(check);
					if (check== null){
						System.out.println("check value is null");
					}
					//message = rs.getTitle()+", "+rs.getDesc() +", "+rs.getOwner_Id() +", "+rs.getRequest_status() +", "+rs.getRequest_item_id()+", "+rs.getRequest_date();
					//LOGGER.fine("Printing out Resultset: "+message);
					Code = FLS_SUCCESS;
					Id = check;
				}
				else {
					Id = "0";
					message = FLS_END_OF_DB_M;
					Code = FLS_END_OF_DB;
					rs.setErrorString("End of table reached");
					
				}
			}else {
				rs.setReturnCode(404);
				System.out.println("End of DB");
			}
			
			//res.setData(Code,Id,message);
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			System.out.println("Error Check Stacktrace");
			e.printStackTrace();
		}	
		LOGGER.fine("Finished process method ");
		//return the response
		
		return rs;
		
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
	}
}
