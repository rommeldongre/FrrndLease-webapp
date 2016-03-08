package tableOps;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import adminOps.Response;
import connect.Connect;
import pojos.WishlistModel;

public class Wishlist extends Connect {
	private String Id=null,operation,message;
	private int Code,itemId,check=0,token=0;
	private WishlistModel wm;
	private Response res = new Response();
	
	public Response selectOp(String Operation, WishlistModel wtm, JSONObject obj) {
		operation = Operation.toLowerCase();
		wm = wtm;
		
		switch(operation) {
		
		case "add" :
			LOGGER.fine("Add op is selected..");
			Add();
			break;
			
		case "delete" : 
			LOGGER.fine("Delete operation is selected");
			Delete();
			break;
			
		case "getnext" :
			LOGGER.fine("Get Next operation is selected.");
			try {
				token = obj.getInt("token");
				getNext();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;
			
		case "getprevious" :
			LOGGER.fine("Get Next operation is selected.");
			try {
				token = obj.getInt("token");
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
		itemId = wm.getItemId();
		
		String sql = "insert into wishlist (wishlist_item_id) values (?)";		 //
		getConnection();
		
		try {
			LOGGER.fine("Creating statement.....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			LOGGER.fine("Statement created. Executing query.....");
			stmt.setInt(1, itemId);
			stmt.executeUpdate();
			LOGGER.fine("Entry added into wishlist table");
			
			message = "Entry added into wishlist table";
			Code = 33;
			Id = String.valueOf(itemId);
			
			res.setData(FLS_SUCCESS,Id,FLS_SUCCESS_M);
		} catch (SQLException e) {
			System.out.println("Couldn't create statement");
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}
	}
	
	private void Delete() {
		itemId = wm.getItemId();
		check = 0;
		LOGGER.fine("Inside delete method....");
		
		getConnection();
		String sql = "DELETE FROM wishlist WHERE wishlist_item_id=?";			//
		String sql2 = "SELECT * FROM wishlist WHERE wishlist_item_id=?";			//
		
		try {
			LOGGER.fine("Creating statement...");
			
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setInt(1, itemId);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getInt("wishlist_item_id");
			}
			
			if(check != 0) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				LOGGER.fine("Statement created. Executing delete query on ..." + check);
				stmt.setInt(1, itemId);
				stmt.executeUpdate();
				message = "operation successfully deleted wishlist item id : "+itemId;
				Code = 34;
				Id = String.valueOf(check);
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
		check = 0;
		LOGGER.fine("Inside GetNext method");
		String sql = "SELECT * FROM wishlist WHERE wishlist_item_id > ? ORDER BY wishlist_item_id LIMIT 1";		//
		
		getConnection();
		try {
			LOGGER.fine("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			LOGGER.fine("Statement created. Executing getNext query...");
			stmt.setInt(1, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getInt("wishlist_item_id"));
				
				message = json.toString();
				System.out.println(message);
				check = rs.getInt("wishlist_item_id");
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
	
	private void getPrevious() {
		check = 0;
		LOGGER.fine("Inside GetPrevious method");
		String sql = "SELECT * FROM wishlist WHERE wishlist_item_id < ? ORDER BY wishlist_item_id DESC LIMIT 1";		//
		
		getConnection();
		try {
			LOGGER.fine("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			LOGGER.fine("Statement created. Executing getPrevious query...");
			stmt.setInt(1, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getInt("wishlist_item_id"));
				
				message = json.toString();
				LOGGER.fine(message);
				check = rs.getInt("wishlist_item_id");
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
	
	public void DeleteW(int id){
		check = 0;
		LOGGER.fine("Inside delete method....");
		
		getConnection();
		String sql = "DELETE FROM wishlist WHERE wishlist_item_id=?";			//
		String sql2 = "SELECT * FROM wishlist WHERE wishlist_item_id=?";			//
		
		try {
			LOGGER.fine("Creating statement...");
			
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setInt(1, id);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getInt("wishlist_item_id");
			}
			
			if(check != 0) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				LOGGER.fine("Statement created. Executing delete query on ..." + check);
				stmt.setInt(1, id);
				stmt.executeUpdate();
				message = "operation successfully deleted wishlist item id : "+id;
			}
			else{
				System.out.println("Entry not found in database!!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
