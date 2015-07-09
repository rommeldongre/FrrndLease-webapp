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
			System.out.println("Add op is selected..");
			Add();
			break;
			
		case "delete" : 
			System.out.println("Delete operation is selected");
			Delete();
			break;
			
		case "getnext" :
			System.out.println("Get Next operation is selected.");
			try {
				token = obj.getInt("token");
				getNext();
			} catch (JSONException e) {
				res.setData(202, String.valueOf(token), "JSON Data not parsed/found(JSON Exception)");
				e.printStackTrace();
			}
			break;
			
		case "getprevious" :
			System.out.println("Get Next operation is selected.");
			try {
				token = obj.getInt("token");
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
		itemId = wm.getItemId();
		
		String sql = "insert into wishlist (wishlist_item_id) values (?)";		 //
		getConnection();
		
		try {
			System.out.println("Creating statement.....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing query.....");
			stmt.setInt(1, itemId);
			stmt.executeUpdate();
			System.out.println("Entry added into wishlist table");
			
			message = "Entry added into wishlist table";
			Code = 33;
			Id = String.valueOf(itemId);
			
			res.setData(Code,Id,message);
		} catch (SQLException e) {
			System.out.println("Couldn't create statement");
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
			e.printStackTrace();
		}
	}
	
	private void Delete() {
		itemId = wm.getItemId();
		check = 0;
		System.out.println("Inside delete method....");
		
		getConnection();
		String sql = "DELETE FROM wishlist WHERE wishlist_item_id=?";			//
		String sql2 = "SELECT * FROM wishlist WHERE wishlist_item_id=?";			//
		
		try {
			System.out.println("Creating statement...");
			
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setInt(1, itemId);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getInt("wishlist_item_id");
			}
			
			if(check != 0) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing delete query on ..." + check);
				stmt.setInt(1, itemId);
				stmt.executeUpdate();
				message = "operation successfully deleted wishlist item id : "+itemId;
				Code = 34;
				Id = String.valueOf(check);
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
		check = 0;
		System.out.println("Inside GetNext method");
		String sql = "SELECT * FROM wishlist WHERE wishlist_item_id > ? ORDER BY wishlist_item_id LIMIT 1";		//
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getNext query...");
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
				Code = 35;
				Id = String.valueOf(check);
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
		check = 0;
		System.out.println("Inside GetPrevious method");
		String sql = "SELECT * FROM wishlist WHERE wishlist_item_id < ? ORDER BY wishlist_item_id DESC LIMIT 1";		//
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getPrevious query...");
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
				Code = 36;
				Id = String.valueOf(check);
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
	
	public void DeleteW(int id){
		check = 0;
		System.out.println("Inside delete method....");
		
		getConnection();
		String sql = "DELETE FROM wishlist WHERE wishlist_item_id=?";			//
		String sql2 = "SELECT * FROM wishlist WHERE wishlist_item_id=?";			//
		
		try {
			System.out.println("Creating statement...");
			
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setInt(1, id);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getInt("wishlist_item_id");
			}
			
			if(check != 0) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing delete query on ..." + check);
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
