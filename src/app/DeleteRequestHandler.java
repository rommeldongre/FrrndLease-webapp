package app;

//import com.mysql.jdbc.PreparedStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.DeleteRequestReqObj;
import pojos.DeleteRequestResObj;
import pojos.RequestsModel;
import pojos.ItemsModel;

import org.json.JSONException;
import org.json.JSONObject;

import pojos.ReqObj;
import pojos.ResObj;
import adminOps.Response;
import util.FlsSendMail;
import util.AwsSESEmail;


public class DeleteRequestHandler extends Connect implements AppHandler {
	
	private String user_name,item_Id=null,Id=null,token, message;
	private int Code;
	private Response res = new Response();
	
	private static DeleteRequestHandler instance = null;

	public static DeleteRequestHandler getInstance() {
		if (instance == null)
			instance = new DeleteRequestHandler();
		return instance;
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		DeleteRequestReqObj rq = (DeleteRequestReqObj) req;
		DeleteRequestResObj rs = new DeleteRequestResObj();
		LOGGER.info("Inside process method "+ rq.getRequest_Id()+", "+ rq.getUserId());
		//TODO: Core of the processing takes place here
		
		LOGGER.info("inside DeleteRequestHandler method");
		getConnection();
		String sql2 = "SELECT * FROM requests WHERE request_id=?";								//
		
		try {
			LOGGER.info("Creating Statement....");
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setInt(1, rq.getRequest_Id());
			ResultSet rs1 = stmt2.executeQuery();
			while(rs1.next()) {
				item_Id = rs1.getString("request_item_id");
			}
			
			if(item_Id != null) {
				
               //code for populating item pojo for sending requester email
				RequestsModel rm1 = new RequestsModel();
				ItemsModel im = new ItemsModel();
				String sql1= "SELECT * FROM items WHERE item_id=?";
				LOGGER.info("Creating a statement .....");
				PreparedStatement stmt1 = connection.prepareStatement(sql1);
				
				LOGGER.info("Statement created. Executing select row query of FLS_MAIL_REJECT_REQUEST_TO...");
				stmt1.setString(1,item_Id);
				
				ResultSet dbResponse = stmt1.executeQuery();
				LOGGER.info("Query to request pojos fired into requests table");
				if(dbResponse.next()){
				
					if (dbResponse.getString("item_id")!= null) {
						LOGGER.info("Inside Nested check1 statement of FLS_MAIL_REJECT_REQUEST_TO");
						
						
						//Populate the response
						try {
							JSONObject obj1 = new JSONObject();
							obj1.put("title", dbResponse.getString("item_name"));
							obj1.put("description", dbResponse.getString("item_desc"));
							obj1.put("category", dbResponse.getString("item_category"));
							obj1.put("userId", dbResponse.getString("item_user_id"));
							obj1.put("leaseTerm", dbResponse.getString("item_lease_term"));
							obj1.put("id", dbResponse.getString("item_id"));
							obj1.put("leaseValue", dbResponse.getString("item_lease_value"));
							obj1.put("status", "InStore");
							obj1.put("image", " ");
							
							im.getData(obj1);
							LOGGER.info("Json parsed for FLS_MAIL_REJECT_REQUEST_TO");
						} catch (JSONException e) {
							System.out.println("Couldn't parse/retrieve JSON for FLS_MAIL_REJECT_REQUEST_TO");
							e.printStackTrace();
						}
						
						
						
					}
				}
				//code for populating item pojo for sending requester email ends here 
				
				String sql = "UPDATE requests SET request_status=? WHERE request_id=?";			//
				String status = "Archived";
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				LOGGER.info("Statement created. Executing edit query on ..." + rq.getRequest_Id());
				stmt.setString(1, status);
				stmt.setInt(2,rq.getRequest_Id());
				stmt.executeUpdate();
				message = "operation successfull edited request id : "+rq.getRequest_Id();
				Code = 56;
				Id = rq.getRequest_Id()+ "";
				res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
				
				try{
					AwsSESEmail newE = new AwsSESEmail();
					//ownerId= im.getUserId();
					newE.send(rq.getUserId(),FlsSendMail.Fls_Enum.FLS_MAIL_DELETE_REQUEST_FROM,im);
					newE.send(im.getUserId(),FlsSendMail.Fls_Enum.FLS_MAIL_DELETE_REQUEST_TO,im);
					rs.setErrorString("No Error");
					rs.setReturnCode(0);
					}catch(Exception e){
					  e.printStackTrace();
					}
			}
			else{
				rs.setErrorString(FLS_ENTRY_NOT_FOUND_M);
				rs.setReturnCode(FLS_ENTRY_NOT_FOUND);
				System.out.println("Entry not found in database!!");
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
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