package tableOps;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import adminOps.Response;
import connect.Connect;
import pojos.UsersModel;

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
			System.out.println("Add op is selected..");
			Add();
			break;
			
		case "delete" : 
			System.out.println("Delete operation is selected");
			Delete();
			break;
			
		case "edit" :
			System.out.println("Edit operation is selected.");
			Edit();
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
			
		default:
			res.setData(202, "0", "Invalid Operation!!");;
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
		
		String sql = "insert into users (userId,fullName,mobile,location,auth) values (?,?,?,?,?)";
		getConnection();
		
		try {
			System.out.println("Creating statement.....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing query.....");
			stmt.setString(1, userId);
			stmt.setString(2, fullName);
			stmt.setString(3, mobile);
			stmt.setString(4, location);
			stmt.setString(5, auth);
			stmt.executeUpdate();
			System.out.println("Entry added into users table");
			
			message = "Entry added into users table";
			Code = 37;
			Id = userId;
			
			res.setData(Code,Id,message);
		} catch (SQLException e) {
			System.out.println("Couldn't create statement");
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
			e.printStackTrace();
		}
	}
	
	private void Delete() {
		userId = um.getUserId();
		check = null;
		System.out.println("Inside delete method....");
		
		getConnection();
		String sql = "DELETE FROM users WHERE userId=?";
		String sql2 = "SELECT * FROM users WHERE userId=?";
		
		try {
			System.out.println("Creating statement...");
			
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, userId);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getString("userId");
			}
			
			if(check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing delete query on ..." + check);
				stmt.setString(1, userId);
				stmt.executeUpdate();
				message = "operation successfull deleted user id : "+userId;
				Code = 38;
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
	
	private void Edit() {
		userId = um.getUserId();
		fullName = um.getFullName();
		mobile = um.getMobile();
		location = um.getLocation();
		auth = um.getAuth();
		check = null;
		
		System.out.println("inside edit method");
		getConnection();
		String sql = "UPDATE users SET fullName=?, mobile=?, location=?, auth=?  WHERE userId=?";
		String sql2 = "SELECT * FROM users WHERE userId=?";
		
		try {
			System.out.println("Creating Statement....");
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, userId);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getString("userId");
			}
			
			if(check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing edit query on ..." + check);
				stmt.setString(1, fullName);
				stmt.setString(2, mobile);
				stmt.setString(3, location);
				stmt.setString(4, auth);
				stmt.setString(5, userId);
				stmt.executeUpdate();
				message = "operation successfull edited user id : "+userId;
				Code = 39;
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
		String sql = "SELECT * FROM users WHERE userId > ? ORDER BY userId LIMIT 1";
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getNext query...");
			stmt.setString(1, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				message = "user ID: "+rs.getString("userId")+"; full name: "+rs.getString("fullName")+"; mobile: "+rs.getString("mobile")+"; location : "+rs.getString("location")+"; auth : "+rs.getString("auth");
				System.out.println(message);
				check = rs.getString("userId");
			}
			
			if(check != null ) {
				Code = 40;
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
		}	
	}
	
	private void getPrevious() {
		check = null;
		System.out.println("Inside GetPrevious method");
		String sql = "SELECT * FROM users WHERE userId < ? ORDER BY userId DESC LIMIT 1";
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getPrevious query...");
			stmt.setString(1, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				message = "user ID: "+rs.getString("userId")+"; full name: "+rs.getString("fullName")+"; mobile: "+rs.getString("mobile")+"; location : "+rs.getString("location")+"; auth : "+rs.getString("auth");
				System.out.println(message);
				check = rs.getString("userId");
			}
			
			if(check != null ) {
				Code = 41;
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
		}	
	}
}
