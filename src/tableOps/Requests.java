package tableOps;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import adminOps.Response;
import connect.Connect;
import pojos.RequestsModel;

public class Requests extends Connect{
	private String check=null, Id=null,token,userId,itemId,operation,message;
	private int Code;
	private RequestsModel rm;
	private Response res = new Response();
	
	public Response selectOp(String Operation, RequestsModel rtm, JSONObject obj) {
		operation = Operation.toLowerCase();
		rm = rtm;
		
		switch(operation) {
		
		case "add" :
			System.out.println("Add op is selected..");
			Add();
			break;
			
		case "delete" : 
			System.out.println("Delete operation is selected");
			Delete();
			break;
			
		/*case "deleteone" :
			System.out.println("DeleteOne op is selected");
			DeleteOne();
			break;*/
			
		case "edits" :
			System.out.println("Edit s operation is selected");
			EditS();
			break;
			
		case "editone" :
			System.out.println("Edit one operation is selected");
			EditOne();
			break;
			
		case "getnext" :
			System.out.println("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getNext();
			} catch (JSONException e) {
				res.setData(202, String.valueOf(token), "JSON Data not parsed/found(JSON Exception)");
				e.printStackTrace();
			}
			break;
			
		case "getprevious" :
			System.out.println("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getPrevious();
			} catch (JSONException e) {
				res.setData(202, String.valueOf(token), "JSON Data not parsed/found(JSON Exception)");
				e.printStackTrace();
			}
			break;
			
		case "getnextr" :
			System.out.println("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getNextR();
			} catch (JSONException e) {
				res.setData(202, String.valueOf(token), "JSON Data not parsed/found(JSON Exception)");
				e.printStackTrace();
			}
			break;
			
		case "getpreviousr" :
			System.out.println("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getPreviousR();
			} catch (JSONException e) {
				res.setData(202, String.valueOf(token), "JSON Data not parsed/found(JSON Exception)");
				e.printStackTrace();
			}
			break;
			
		default:
			res.setData(202, "0", "Invalid Operation!!");;
			break;
		}
		
		return res;
	}
	
	private void Add() {
		userId = rm.getUserId();
		itemId = rm.getItemId();
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(cal.getTime());
		
		String sql = "insert into requests (request_requser_id,request_item_id,request_date) values (?,?,?)";		 //
		getConnection();
		
		try {
			System.out.println("Creating statement.....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing query.....");
			stmt.setString(1, userId);
			stmt.setString(2, itemId);
			stmt.setString(3, date);
			stmt.executeUpdate();
			System.out.println("Entry added into requests table");
			
			message = "Entry added into requests table";
			Code = 25;
			Id = itemId;
			
			res.setData(Code,Id,message);
		} catch (SQLException e) {
			System.out.println("Couldn't create statement");
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
			e.printStackTrace();
		}
	}
	
	private void Delete() {
		itemId = rm.getItemId();
		check = null;
		System.out.println("Inside delete method....");
		
		getConnection();
		String sql = "DELETE FROM requests WHERE request_item_id=?";			//
		String sql2 = "SELECT * FROM requests WHERE request_item_id=?";			//
		
		try {
			System.out.println("Creating statement...");
			
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, itemId);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getString("request_item_id");
			}
			
			if(check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing delete query on ..." + check);
				stmt.setString(1, itemId);
				stmt.executeUpdate();
				message = "operation successfull deleted request item id : "+itemId;
				Code = 26;
				Id = check;
				res.setData(Code, Id, message);
			}
			else{
				System.out.println("Entry not found in database!!");
				res.setData(201, "0", "Entry not found in database!!");
			}
		} catch (SQLException e) {
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
			e.printStackTrace();
		}
		
	}
	
	/*private void DeleteOne() {
		itemId = rm.getItemId();
		userId = rm.getUserId();
		check = null;
		System.out.println("Inside delete method....");
		
		getConnection();
		String sql = "DELETE FROM requests WHERE request_item_id=? AND request_requser_id=?";			//
		String sql2 = "SELECT * FROM requests WHERE request_item_id=? AND request_requser_id=?";			//
		
		try {
			System.out.println("Creating statement...");
			
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, itemId);
			stmt2.setString(2, userId);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getString("request_item_id");
			}
			
			if(check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing delete query on ..." + check);
				stmt.setString(1, itemId);
				stmt.setString(2, userId);
				stmt.executeUpdate();
				message = "operation successfull deleted request item id : "+itemId;
				Code = 56;
				Id = check;
				res.setData(Code, Id, message);
			}
			else{
				System.out.println("Entry not found in database!!");
				res.setData(201, "0", "Entry not found in database!!");
			}
		} catch (SQLException e) {
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
			e.printStackTrace();
		}
	}*/
	
	private void EditS() {
		itemId = rm.getItemId();
		userId = rm.getUserId();
		String status = "Archived";
		check = null;
		
		System.out.println("inside edit method");
		getConnection();
		String sql = "UPDATE requests SET request_status=? WHERE request_item_id=?";			//
		String sql2 = "SELECT * FROM requests WHERE request_item_id=?";								//
		
		try {
			System.out.println("Creating Statement....");
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, itemId);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getString("request_item_id");
			}
			
			if(check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing edit query on ..." + check);
				stmt.setString(1, status);
				stmt.setString(2,itemId);
				stmt.executeUpdate();
				message = "operation successfull edited item id : "+itemId;
				Code = 56; /////////
				Id = check;
				res.setData(Code, Id, message);
			}
			else{
				System.out.println("Entry not found in database!!");
				res.setData(201, "0", "Entry not found in database!!");
			}
		} catch (SQLException e) {
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
			e.printStackTrace();
		}
	}
	
	private void EditOne(){
		itemId = rm.getItemId();
		userId = rm.getUserId();
		String status = "Archived";
		check = null;
		
		System.out.println("inside edit method");
		getConnection();
		String sql = "UPDATE requests SET request_status=? WHERE request_item_id=? AND request_requser_id=?";			//
		String sql2 = "SELECT * FROM requests WHERE request_item_id=? AND request_requser_id=?";								//
		
		try {
			System.out.println("Creating Statement....");
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, itemId);
			stmt2.setString(2, userId);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getString("request_item_id");
			}
			
			if(check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing edit query on ..." + check);
				stmt.setString(1, status);
				stmt.setString(2,itemId);
				stmt.setString(3, userId);
				stmt.executeUpdate();
				message = "operation successfull edited item id : "+itemId;
				Code = 56;
				Id = check;
				res.setData(Code, Id, message);
			}
			else{
				System.out.println("Entry not found in database!!");
				res.setData(201, "0", "Entry not found in database!!");
			}
		} catch (SQLException e) {
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
			e.printStackTrace();
		}
	}
	
	private void getNext() {
		check = null;
		System.out.println("Inside GetNext method");
		String sql = "SELECT * FROM requests WHERE request_item_id > ? ORDER BY request_item_id LIMIT 1";		//
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getNext query...");
			stmt.setString(1, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getString("request_item_id"));
				json.put("userId", rs.getString("request_requser_id"));
				json.put("date", rs.getString("request_date"));
				
				message = json.toString();
				System.out.println(message);
				check = rs.getString("request_item_id");
			}
			
			if(check != null ) {
				Code = 27;
				Id = check;
			}
			
			else {
				Id = null;
				message = "End of Database!!!";
				Code = 199;
			}
			
			res.setData(Code,Id,message);
		} catch (SQLException e) {
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
			e.printStackTrace();
		} catch (JSONException e) {
			res.setData(204,"0", "JSON Exception");
			e.printStackTrace();
		}	
	}
	
	private void getPrevious() {
		check = null;
		System.out.println("Inside GetPrevious method");
		String sql = "SELECT * FROM requests WHERE request_item_id < ? ORDER BY request_item_id DESC LIMIT 1";			//
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getPrevious query...");
			stmt.setString(1, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getString("request_item_id"));
				json.put("userId", rs.getString("request_requser_id"));
				json.put("date", rs.getString("request_date"));
				
				message = json.toString();
				System.out.println(message);
				check = rs.getString("request_item_id");
			}
			
			if(check != null ) {
				Code = 28;
				Id = check;
			}
			
			else {
				Id = null;
				message = "End of Database!!!";
				Code = 199;
			}
			
			res.setData(Code,Id,message);
		} catch (SQLException e) {
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
			e.printStackTrace();
		} catch (JSONException e) {
			res.setData(204,"0", "JSON Exception");
			e.printStackTrace();
		}	
	}
	
	private void getNextR() {
		check = null;
		System.out.println("Inside GetNextR method");
		String sql = "SELECT * FROM requests WHERE request_item_id > ? AND request_status=? ORDER BY request_item_id LIMIT 1";		//
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getNext query...");
			stmt.setString(1, token);
			stmt.setString(2, "Active");
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getString("request_item_id"));
				json.put("userId", rs.getString("request_requser_id"));
				json.put("date", rs.getString("request_date"));
				
				message = json.toString();
				System.out.println(message);
				check = rs.getString("request_item_id");
			}
			
			if(check != null ) {
				Code = 27;
				Id = check;
			}
			
			else {
				Id = null;
				message = "End of Database!!!";
				Code = 199;
			}
			
			res.setData(Code,Id,message);
		} catch (SQLException e) {
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
			e.printStackTrace();
		} catch (JSONException e) {
			res.setData(204,"0", "JSON Exception");
			e.printStackTrace();
		}	
	}
	
	private void getPreviousR() {
		check = null;
		System.out.println("Inside GetPrevious method");
		String sql = "SELECT * FROM requests WHERE request_item_id < ? AND request_status=? ORDER BY request_item_id DESC LIMIT 1";			//
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getPrevious query...");
			stmt.setString(1, token);
			stmt.setString(2, "Active");
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getString("request_item_id"));
				json.put("userId", rs.getString("request_requser_id"));
				json.put("date", rs.getString("request_date"));
				
				message = json.toString();
				System.out.println(message);
				check = rs.getString("request_item_id");
			}
			
			if(check != null ) {
				Code = 28;
				Id = check;
			}
			
			else {
				Id = null;
				message = "End of Database!!!";
				Code = 199;
			}
			
			res.setData(Code,Id,message);
		} catch (SQLException e) {
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
			e.printStackTrace();
		} catch (JSONException e) {
			res.setData(204,"0", "JSON Exception");
			e.printStackTrace();
		}	
	}

}
