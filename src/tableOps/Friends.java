package tableOps;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import pojos.FriendsModel;
import adminOps.Response;
import connect.Connect;

public class Friends extends Connect{
	
	private String check=null, Id=null,token,friendId, fullName, mobile, userId,operation,message;
	private int Code;
	private FriendsModel fm;
	private Response res = new Response();
	
	public Response selectOp(String Operation, FriendsModel fdm, JSONObject obj) {
		operation = Operation.toLowerCase();
		fm = fdm;
		
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
		friendId = fm.getFriendId();
		fullName = fm.getFullName();
		mobile = fm.getMobile();
		userId = fm.getUserId();
		
		String sql = "insert into friends (friend_id,friend_full_name,friend_mobile,friend_user_id) values (?,?,?,?)";		 //
		getConnection();
		
		try {
			System.out.println("Creating statement.....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing query.....");
			stmt.setString(1, friendId);
			stmt.setString(2, fullName);
			stmt.setString(3,mobile);
			stmt.setString(4,userId);
			stmt.executeUpdate();
			System.out.println("Entry added into friends table");
			
			message = "Entry added into friends table";
			Code = 010;
			Id = friendId;
			
			res.setData(Code,Id,message);
		} catch (SQLException e) {
			System.out.println("Couldn't create statement");
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
			e.printStackTrace();
		}
	}
	
	private void Delete() {
		friendId = fm.getFriendId();
		check = null;
		System.out.println("Inside delete method....");
		
		getConnection();
		String sql = "DELETE FROM friends WHERE friend_id=?";			//
		String sql2 = "SELECT * FROM friends WHERE friend_id=?";			//
		
		try {
			System.out.println("Creating statement...");
			
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, friendId);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getString("friend_id");
			}
			
			if(check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing delete query on ..." + check);
				stmt.setString(1, friendId);
				stmt.executeUpdate();
				message = "operation successfull deleted friend id : "+friendId;
				Code = 0011;
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
		friendId = fm.getFriendId();
		fullName = fm.getFullName();
		mobile = fm.getMobile();
		check = null;
		
		System.out.println("inside edit method");
		getConnection();
		String sql = "UPDATE friends SET friend_full_name=?, friend_mobile=? WHERE friend_id=?";			//
		String sql2 = "SELECT * FROM friends WHERE friend_id=?";								//
		
		try {
			System.out.println("Creating Statement....");
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, friendId);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getString("friend_id");
			}
			
			if(check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing edit query on ..." + check);
				stmt.setString(1, fullName);
				stmt.setString(2,mobile);
				stmt.setString(3,friendId);
				stmt.executeUpdate();
				message = "operation successfull edited friends id : "+friendId;
				Code = 0012;
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
		String sql = "SELECT * FROM friends WHERE friend_id > ? ORDER BY friend_id LIMIT 1";		//
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getNext query...");
			stmt.setString(1, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("friendId",rs.getString("friend_id"));
				json.put("fullName",rs.getString("friend_full_name"));
				json.put("mobile",rs.getString("friend_mobile"));
				json.put("userId",rs.getString("friend_user_id"));
				
				message = json.toString();
				System.out.println(message);
				check = rs.getString("friend_id");
			}
			
			if(check != null ) {
				Code = 13;
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
			res.setData(204,"0","JSON Exception");
			e.printStackTrace();
		}	
	}
	
	private void getPrevious() {
		check = null;
		System.out.println("Inside GetPrevious method");
		String sql = "SELECT * FROM friends WHERE friend_id < ? ORDER BY friend_id DESC LIMIT 1";			//
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getPrevious query...");
			stmt.setString(1, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("friendId",rs.getString("friend_id"));
				json.put("fullName",rs.getString("friend_full_name"));
				json.put("mobile",rs.getString("friend_mobile"));
				json.put("userId",rs.getString("friend_user_id"));
				
				message = json.toString();
				System.out.println(message);
				check = rs.getString("friend_id");
			}
			
			if(check != null ) {
				Code = 14;
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
			res.setData(204,"0","JSON Exception");
			e.printStackTrace();
		}	
	}
}