package tableOps;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import adminOps.Response;
import connect.Connect;
import pojos.UsersModel;

import util.FlsSendMail;
import util.AwsSESEmail;

public class Users extends Connect {
	private String userId,fullName,mobile,location,auth,message,operation,Id=null,check=null,token;
	private int Code;
	private UsersModel um;
	private Response res = new Response();
	
	public Response selectOp(String Operation, UsersModel usm, JSONObject obj) {
		operation = Operation.toLowerCase();
		um = usm;
		
		switch(operation) {
		
		case "add" :
			LOGGER.fine("Add op is selected..");
			Add();
			break;
			
		case "delete" : 
			LOGGER.fine("Delete operation is selected");
			Delete();
			break;
			
		case "edit" :
			LOGGER.fine("Edit operation is selected.");
			Edit();
			break;
			
		case "getnext" :
			LOGGER.fine("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getNext();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;
			
		case "getprevious" :
			LOGGER.fine("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getPrevious();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;
			
		case "info" :
			LOGGER.fine("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getUserInfo();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;
			
		default:
			res.setData(FLS_INVALID_OPERATION, "0", FLS_INVALID_OPERATION_M);
			break;
		}
		
		return res;
	}
	
	private void Add() {
		userId = um.getUserId();
		fullName = um.getFullName();
		mobile = um.getMobile();
		location = um.getLocation();
		auth = um.getAuth();
		
		String sql = "insert into users (user_id,user_full_name,user_mobile,user_location,user_auth) values (?,?,?,?,?)";
		getConnection();
		
		try {
			LOGGER.fine("Creating statement.....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			LOGGER.fine("Statement created. Executing query.....");
			stmt.setString(1, userId);
			stmt.setString(2, fullName);
			stmt.setString(3, mobile);
			stmt.setString(4, location);
			stmt.setString(5, auth);
			stmt.executeUpdate();
			LOGGER.fine("Entry added into users table");
			
			message = "Entry added into users table";
			Code = 37;
			Id = userId;
			
			try{
				AwsSESEmail newE = new AwsSESEmail();
				newE.send(userId,FlsSendMail.Fls_Enum.FLS_MAIL_REGISTER,um);
				}catch(Exception e){
				  e.printStackTrace();
				}
			
			
			res.setData(FLS_SUCCESS,Id,FLS_SUCCESS_M);
		} catch (SQLException e) {
			System.out.println("Couldn't create statement");
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}
	}
	
	private void Delete() {
		userId = um.getUserId();
		check = null;
		LOGGER.fine("Inside delete method....");
		
		getConnection();
		String sql = "DELETE FROM users WHERE user_id=?";
		String sql2 = "SELECT * FROM users WHERE user_id=?";
		
		try {
			LOGGER.fine("Creating statement...");
			
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, userId);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getString("user_id");
			}
			
			if(check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				LOGGER.fine("Statement created. Executing delete query on ..." + check);
				stmt.setString(1, userId);
				stmt.executeUpdate();
				message = "operation successfull deleted user id : "+userId;
				Code = 38;
				Id = check;
				res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
			}
			else{
				System.out.println("Entry not found in database!!");
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}
		
	}
	
	private void Edit() {
		userId = um.getUserId();
		fullName = um.getFullName();
		mobile = um.getMobile();
		location = um.getLocation();
		auth = um.getAuth();
		check = null;
		
		LOGGER.fine("inside edit method");
		getConnection();
		String sql = "UPDATE users SET user_full_name=?, user_mobile=?, user_location=?, user_auth=?  WHERE user_id=?";
		String sql2 = "SELECT * FROM users WHERE user_id=?";
		
		try {
			LOGGER.fine("Creating Statement....");
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, userId);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getString("user_id");
			}
			
			if(check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				LOGGER.fine("Statement created. Executing edit query on ..." + check);
				stmt.setString(1, fullName);
				stmt.setString(2, mobile);
				stmt.setString(3, location);
				stmt.setString(4, auth);
				stmt.setString(5, userId);
				stmt.executeUpdate();
				message = "operation successfull edited user id : "+userId;
				Code = 39;
				Id = check;
				res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
			}
			else{
				System.out.println("Entry not found in database!!");
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}
	}
	
	private void getNext() {
		check = null;
		LOGGER.fine("Inside GetNext method");
		String sql = "SELECT * FROM users WHERE user_id > ? ORDER BY user_id LIMIT 1";
		
		getConnection();
		try {
			LOGGER.fine("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			LOGGER.fine("Statement created. Executing getNext query...");
			stmt.setString(1, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("userId", rs.getString("user_id"));
				json.put("fullName", rs.getString("user_full_name"));
				json.put("mobile", rs.getString("user_mobile"));
				json.put("location", rs.getString("user_location"));
				json.put("auth", rs.getString("user_auth"));
				
				message = json.toString();
				System.out.println(message);
				LOGGER.fine(message);
				check = rs.getString("user_id");
			}
			
			if(check != null ) {
				Code = FLS_SUCCESS;
				Id = check;
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
	
	private void getPrevious() {
		check = null;
		LOGGER.fine("Inside GetPrevious method");
		String sql = "SELECT * FROM users WHERE user_id < ? ORDER BY user_id DESC LIMIT 1";
		
		getConnection();
		try {
			LOGGER.fine("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			LOGGER.fine("Statement created. Executing getPrevious query...");
			stmt.setString(1, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("userId", rs.getString("user_id"));
				json.put("fullName", rs.getString("user_full_name"));
				json.put("mobile", rs.getString("user_mobile"));
				json.put("location", rs.getString("user_location"));
				json.put("auth", rs.getString("user_auth"));
				
				message = json.toString();
				System.out.println(message);
				check = rs.getString("user_id");
			}
			
			if(check != null ) {
				Code = FLS_SUCCESS;
				Id = check;
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
	
	private void getUserInfo() {
		check = null;
		auth = um.getAuth();
		LOGGER.fine("Inside GetPrevious method");
		String sql = "SELECT * FROM users WHERE user_id = ? AND user_auth = ?";
		
		getConnection();
		try {
			LOGGER.fine("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			LOGGER.fine("Statement created. Executing getPrevious query...");
			stmt.setString(1, token);
			stmt.setString(2, auth);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("userId", rs.getString("user_id"));
				json.put("fullName", rs.getString("user_full_name"));
				json.put("mobile", rs.getString("user_mobile"));
				json.put("location", rs.getString("user_location"));
				
				message = json.toString();
				System.out.println(message);
				check = rs.getString("user_id");
			}
			
			if(check != null ) {
				Code = FLS_SUCCESS;
				Id = check;
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
