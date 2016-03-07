package tableOps;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import pojos.CategoryModel;
import adminOps.Response;
import connect.Connect;

public class Category extends Connect {
	
	private String name,description,parent,child,message,operation,Id=null,check=null,token;
	private int Code;
	private CategoryModel cm;
	private Response res = new Response();
	
	public Response selectOp(String Operation, CategoryModel ctm, JSONObject obj) {
		operation = Operation.toLowerCase();
		cm = ctm;
		
		switch(operation) {
		
		case "add" :
			//System.out.println("Add op is selected..");
			LOGGER.fine("Add op is selected..");
			Add();
			break;
			
		case "delete" : 
			//System.out.println("Delete operation is selected");
			LOGGER.fine("Delete operation is selected");
			Delete();
			break;
			
		case "edit" :
			//System.out.println("Edit operation is selected.");
			LOGGER.fine("Edit operation is selected.");
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
			//System.out.println("Get Next operation is selected.");
			LOGGER.fine("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getPrevious();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;
			
		default:
			res.setData(FLS_INVALID_OPERATION, "0", FLS_INVALID_OPERATION_M);;
			break;
		}
		
		return res;
	}
	
	private void Add() {
		name = cm.getName();
		description = cm.getDescription();
		parent = cm.getParent();
		child = cm.getChild();
		
		String sql = "insert into category (cat_name,cat_desc,cat_parent,cat_child) values (?,?,?,?)";
		getConnection();
		
		try {
			//System.out.println("Creating statement.....");
			LOGGER.fine("Creating statement.....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			//System.out.println("Statement created. Executing query.....");
			LOGGER.fine("Statement created. Executing query.....");
			stmt.setString(1, name);
			stmt.setString(2, description);
			stmt.setString(3,parent);
			stmt.setString(4,child);
			stmt.executeUpdate();
			//System.out.println("Entry added into category table");
			LOGGER.fine("Entry added into category table");
			
			message = "Entry added into category table";
			Code = 005;
			Id = name;
			
			res.setData(FLS_SUCCESS,Id,FLS_CATEGORY_ADD);
		} catch (SQLException e) {
			System.out.println("Couldn't create statement");
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}
	}
	
	private void Delete() {
		name = cm.getName();
		check = null;
		//System.out.println("Inside delete method....");
		LOGGER.fine("Inside delete method....");
		
		getConnection();
		String sql = "DELETE FROM category WHERE cat_name=?";
		String sql2 = "SELECT * FROM category WHERE cat_name=?";
		
		try {
			//System.out.println("Creating statement...");
			LOGGER.fine("Creating statement...");
			
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, name);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getString("cat_name");
			}
			
			if(check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				//System.out.println("Statement created. Executing delete query on ..." + check);
				LOGGER.fine("Statement created. Executing delete query on ..." + check);
				stmt.setString(1, name);
				stmt.executeUpdate();
				message = "operation successfull deleted category id : "+name;
				Code = 006;
				Id = check;
				res.setData(FLS_SUCCESS, Id, FLS_CATEGORY_DELETE);
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
		name = cm.getName();
		description = cm.getDescription();
		parent = cm.getParent();
		child = cm.getChild();
		check = null;
		
		//System.out.println("inside edit method");
		LOGGER.fine("inside edit method");
		getConnection();
		String sql = "UPDATE category SET cat_desc=?, cat_parent=?, cat_child=? WHERE cat_name=?";
		String sql2 = "SELECT * FROM category WHERE cat_name=?";
		
		try {
			//System.out.println("Creating Statement....");
			LOGGER.fine("Creating Statement....");
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, name);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getString("cat_name");
			}
			
			if(check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				//System.out.println("Statement created. Executing edit query on ..." + check);
				LOGGER.fine("Statement created. Executing edit query on ..." + check);
				stmt.setString(1, description);
				stmt.setString(2,parent);
				stmt.setString(3,child);
				stmt.setString(4, name);
				stmt.executeUpdate();
				message = "operation successfull edited category id : "+name;
				Code = 007;
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
		//System.out.println("Inside GetNext method");
		LOGGER.fine("Inside GetNext method");
		String sql = "SELECT * FROM category WHERE cat_name > ? ORDER BY cat_name LIMIT 1";
		
		getConnection();
		try {
			//System.out.println("Creating a statement .....");
			LOGGER.fine("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			//System.out.println("Statement created. Executing getNext query...");
			LOGGER.fine("Statement created. Executing getNext query...");
			stmt.setString(1, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("catName",rs.getString("cat_name"));
				json.put("catDesc",rs.getString("cat_desc"));
				json.put("catParent",rs.getString("cat_parent"));
				
				//message = "; catDesc: "+rs.getString("cat_desc")+"; catParent: "+rs.getString("cat_parent");
				message = json.toString();
				//System.out.println(message);
				LOGGER.fine(message);
				check = rs.getString("cat_name");
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
		//System.out.println("Inside GetPrevious method");
		LOGGER.fine("Inside GetPrevious method");
		String sql = "SELECT * FROM category WHERE cat_name < ? ORDER BY cat_name DESC LIMIT 1";
		
		getConnection();
		try {
			//System.out.println("Creating a statement .....");
			LOGGER.fine("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			//System.out.println("Statement created. Executing getPrevious query...");
			LOGGER.fine("Statement created. Executing getPrevious query...");
			stmt.setString(1, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("catName",rs.getString("cat_name"));
				json.put("catDesc",rs.getString("cat_desc"));
				json.put("catParent",rs.getString("cat_parent"));
				
				//message = "catName: "+rs.getString("cat_name")+"; catDesc: "+rs.getString("cat_desc")+"; catParent: "+rs.getString("cat_parent");
				message = json.toString();
				//System.out.println(message);
				LOGGER.fine(message);
				check = rs.getString("cat_name");
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
