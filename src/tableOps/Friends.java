package tableOps;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import pojos.FriendsModel;
import adminOps.Response;
import connect.Connect;
import util.FlsSendMail;
import util.FlsLogger;

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
			//System.out.println("Get Next operation is selected.");
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
			System.out.println("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getPrevious();
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
		friendId = fm.getFriendId();
		fullName = fm.getFullName();
		mobile = fm.getMobile();
		userId = fm.getUserId();
		
		String sql = "insert into friends (friend_id,friend_full_name,friend_mobile,friend_user_id) values (?,?,?,?)";		 //
		String sql1 = "SELECT * FROM friends WHERE friend_user_id=? AND friend_id=?";
		getConnection();
		
		try {
			System.out.println("Creating Select statement.....");
			PreparedStatement stmt1 = connection.prepareStatement(sql1);
			stmt1.setString(1, userId);
			stmt1.setString(2, friendId);
			
			ResultSet rs = stmt1.executeQuery();
			
			while (rs.next()) {
				check = rs.getString("friend_id");
			}
			if (check == null) {
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
				Code = 10;
				Id = friendId;
				
				try{
						FlsSendMail newE = new FlsSendMail();
						String source = "@api";
						if (friendId.contains("@fb") || friendId.contains("@google")) {
							newE.send(userId,FlsSendMail.Fls_Enum.FLS_MAIL_ADD_FRIEND_FROM,fm,source);
						}else {
							source = "@email";
							newE.send(userId,FlsSendMail.Fls_Enum.FLS_MAIL_ADD_FRIEND_FROM,fm,source);
							newE.send(friendId,FlsSendMail.Fls_Enum.FLS_MAIL_ADD_FRIEND_TO,fm);
						}
					}catch(Exception e){
					  e.printStackTrace();
					}
			}else{
				System.out.println("Friend Already exists.....");
				res.setData(FLS_SUCCESS,Id,FLS_SUCCESS_M);
			}
			
			
			//res.setData(FLS_SUCCESS,Id,FLS_SUCCESS_M);
			res.setData(Code,Id,message);
		} catch (SQLException e) {
			System.out.println("Couldn't create statement");
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}
	}
	
	private void Delete() {
		friendId = fm.getFriendId();
		userId = fm.getUserId();
		check = null;
		System.out.println("Inside delete method....");
		
		getConnection();
		String sql = "DELETE FROM friends WHERE friend_id=? AND friend_user_id=?";			//
		String sql2 = "SELECT * FROM friends WHERE friend_id=? AND friend_user_id=?";			//
		
		try {
			System.out.println("Creating statement...");
			
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, friendId);
			stmt2.setString(2, userId);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getString("friend_id");
			}
			
			if(check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing delete query on ..." + check);
				stmt.setString(1, friendId);
				stmt.setString(2, userId);
				stmt.executeUpdate();
				message = "operation successfull deleted friend id : "+friendId;
				Code = 11;
				Id = check;
				res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
				
				try{
					FlsSendMail newE = new FlsSendMail();
					newE.send(friendId,FlsSendMail.Fls_Enum.FLS_MAIL_DELETE_FRIEND_FROM,fm);
					newE.send(userId,FlsSendMail.Fls_Enum.FLS_MAIL_DELETE_FRIEND_TO,fm);
					}catch(Exception e){
					  e.printStackTrace();
					}
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
		friendId = fm.getFriendId();
		fullName = fm.getFullName();
		mobile = fm.getMobile();
		userId = fm.getUserId();
		check = null;
		
		System.out.println("inside edit method");
		getConnection();
		String sql = "UPDATE friends SET friend_full_name=?, friend_mobile=? WHERE friend_id=? AND friend_user_id=?";			//
		String sql2 = "SELECT * FROM friends WHERE friend_id=? AND friend_user_id=?";								//
		
		try {
			System.out.println("Creating Statement....");
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, friendId);
			stmt2.setString(2, userId);
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
				stmt.setString(4, userId);
				stmt.executeUpdate();
				message = "operation successfull edited friends id : "+friendId;
				Code = 12;
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
		Id = fm.getFriendId();
		//System.out.println("Inside GetNext method");
		LOGGER.fine("Inside GetNext method");
		String sql = "SELECT * FROM friends WHERE friend_user_id = ? AND friend_id>? ORDER BY friend_id LIMIT 1";		//
		
		getConnection();
		try {
			//System.out.println("Creating a statement .....");
			LOGGER.fine("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			//System.out.println("Statement created. Executing getNext query...");
			LOGGER.fine("Statement created. Executing getNext query...");
			stmt.setString(1, Id);
			stmt.setString(2, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("friendId",rs.getString("friend_id"));
				json.put("fullName",rs.getString("friend_full_name"));
				json.put("mobile",rs.getString("friend_mobile"));
				json.put("userId",rs.getString("friend_user_id"));
				
				message = json.toString();
				//System.out.println(message);
				LOGGER.fine(message);
				check = rs.getString("friend_id");
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
		Id = fm.getFriendId();
		System.out.println("Inside GetPrevious method");
		String sql = "SELECT * FROM friends WHERE friend_id = ? AND friend_user_id<? ORDER BY friend_user_id DESC LIMIT 1";			//
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getPrevious query...");
			stmt.setString(1, Id);
			stmt.setString(2, token);
			
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