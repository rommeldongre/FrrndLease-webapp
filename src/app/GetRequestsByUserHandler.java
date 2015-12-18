package app;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import adminOps.Response;
import pojos.GetRequestsByUserReqObj;
import pojos.GetRequestsByUserResObj;
import pojos.ReqObj;
import pojos.ResObj;
import pojos.StoreModel;
import connect.Connect;

public class GetRequestsByUserHandler implements AppHandler {
	
	private String Id=null,operation,message;
	private int Code,itemId,check=0,token=0;
	private StoreModel sm;
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
		
		GetRequestsByUserReqObj r = (GetRequestsByUserReqObj) req;
		
		
		//TODO: Core of the processing takes place here
		
		//Create the response
		GetRequestsByUserResObj response=new GetRequestsByUserResObj();
		
		//Populate the response
		
		response.setTitle("");;
		response.setDesc("");
		response.setOwneruserId("");
		response.setRequestId(0);
		response.setTitle("");
		response.setToken(0);
		
		
		//return the response
		return response;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
	}
	
	private void getNext() {
		check = 0;
		System.out.println("Inside GetNext method");
		String sql = "SELECT requests.request_date, items.item_name, items.item_desc, items.item_user_id  FROM requests INNER JOIN items on requests.request_item_id = items.item_id WHERE request.request_requser_id= '' and requests.request_status='Active' ";
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getNext query...");
			stmt.setInt(1, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getInt("store_item_id"));
				
				message = json.toString();
				System.out.println(message);
				check = rs.getInt("store_item_id");
			}
			
			if(check != 0 ) {
				Code = FLS_SUCCESS;
				Id = String.valueOf(check);
			}
			
			else {
				Id = "0";
				message = FLS_END_OF_DB_M;
				Code = FLS_END_OF_DB;
			}
			
			res.setData(Code,Id,message);
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (JSONException e) {
			res.setData(FLS_JSON_EXCEPTION,"0",FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
		}	
	}

}
