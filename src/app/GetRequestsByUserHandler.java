package app;

//import com.mysql.jdbc.PreparedStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.GetRequestsByUserReqObj;
import pojos.GetRequestsByUserResObj;
import pojos.ReqObj;
import pojos.ResObj;
import adminOps.Response;


public class GetRequestsByUserHandler extends Connect implements AppHandler {
	
	private String user_name, check=null,Id=null,token, message;
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
		LOGGER.fine("Inside process method "+ rq.getUserId()+", "+ rq.getCookie());
		//TODO: Core of the processing takes place here
		check = null;
		LOGGER.fine("Inside GetOutgoingrequests method");
		
		try {
			getConnection();
			String sql = "SELECT tb1.request_date, tb1.request_item_id, tb1.request_status, tb2.item_name, tb2.item_desc, tb2.item_user_id, tb3.user_full_name FROM requests tb1 INNER JOIN items tb2 on tb1.request_item_id = tb2.item_id INNER JOIN users tb3 on tb2.item_user_id = tb3.user_id WHERE tb1.request_requser_id=? AND tb1.request_item_id>0 HAVING tb1.request_status=? LIMIT 1";
			LOGGER.fine("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			LOGGER.fine("Statement created. Executing GetOutgoingrequests query...");
			stmt.setString(1, rq.getUserId());
			stmt.setInt(2, rq.getCookie());
			
			ResultSet dbResponse = stmt.executeQuery();
			
			if(dbResponse.next()){
				check = dbResponse.getString("request_item_id");
				
				if (check!= null) {
					//Populate the response
					rs.setTitle(dbResponse.getString("item_name"));
					rs.setDesc(dbResponse.getString("item_desc"));
					rs.setOwner_Id(dbResponse.getString("item_user_id"));
					rs.setRequest_status(dbResponse.getString("request_status"));
					rs.setRequest_item_id(dbResponse.getInt("request_item_id"));
					rs.setRequest_date(dbResponse.getDate("request_date"));
					rs.setOwner_name(dbResponse.getString("user_full_name"));
					
					message = rs.getTitle()+", "+rs.getDesc() +", "+rs.getOwner_Id() +", "+rs.getRequest_status() +", "+rs.getRequest_item_id()+", "+rs.getRequest_date();
					LOGGER.fine("Printing out Resultset: "+message);
					Code = FLS_SUCCESS;
					Id = check;
				}
				else {
					Id = "0";
					message = FLS_END_OF_DB_M;
					Code = FLS_END_OF_DB;
					rs.setErrorString("End of table reached");
				}
			}
			
			//res.setData(Code,Id,message);
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			System.out.println("Error Check Stacktrace");
			e.printStackTrace();
		}	
		//System.out.println("Finished process method ");
		LOGGER.fine("Finished process method ");
		//return the response
		return rs;
		
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
	}
}
