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
		System.out.println("Inside process method "+ rq.getUserId()+", "+ rq.getCookie());
		//TODO: Core of the processing takes place here
		check = null;
		System.out.println("Inside GetOutgoingrequests method");
		
		try {
			getConnection();
			String sql = "SELECT requests.request_date, requests.request_item_id, requests.request_status, items.item_name, items.item_desc, items.item_user_id  FROM requests INNER JOIN items on requests.request_item_id = items.item_id WHERE requests.request_requser_id=? AND requests.request_item_id>? HAVING requests.request_status='Active' LIMIT 1";
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing GetOutgoingrequests query...");
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
					
					message = rs.getTitle()+", "+rs.getDesc() +", "+rs.getOwner_Id() +", "+rs.getRequest_status() +", "+rs.getRequest_item_id()+", "+rs.getRequest_date();
					System.out.println("Printing out Resultset: "+message);
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
		System.out.println("Finished process method ");
		//return the response
		return rs;
		
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
	}
}
