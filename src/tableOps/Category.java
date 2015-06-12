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
		name = cm.getName();
		description = cm.getDescription();
		parent = cm.getParent();
		child = cm.getChild();
		
		String sql = "insert into category (catName,catDesc,catParent,catChild) values (?,?,?,?)";
		getConnection();
		
		try {
			System.out.println("Creating statement.....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing query.....");
			stmt.setString(1, name);
			stmt.setString(2, description);
			stmt.setString(3,parent);
			stmt.setString(4,child);
			stmt.executeUpdate();
			System.out.println("Entry added into category table");
			
			message = "Entry added into category table";
			Code = 005;
			Id = name;
			
			res.setData(Code,Id,message);
		} catch (SQLException e) {
			System.out.println("Couldn't create statement");
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
			e.printStackTrace();
		}
	}
	
	private void Delete() {
		name = cm.getName();
		check = null;
		System.out.println("Inside delete method....");
		
		getConnection();
		String sql = "DELETE FROM category WHERE catName=?";
		String sql2 = "SELECT * FROM category WHERE catName=?";
		
		try {
			System.out.println("Creating statement...");
			
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, name);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getString("catName");
			}
			
			if(check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing delete query on ..." + check);
				stmt.setString(1, name);
				stmt.executeUpdate();
				message = "operation successfull deleted category id : "+name;
				Code = 006;
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
		name = cm.getName();
		description = cm.getDescription();
		parent = cm.getParent();
		child = cm.getChild();
		check = null;
		
		System.out.println("inside edit method");
		getConnection();
		String sql = "UPDATE category SET catDesc=?, catParent=?, catChild=? WHERE catName=?";
		String sql2 = "SELECT * FROM category WHERE catName=?";
		
		try {
			System.out.println("Creating Statement....");
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, name);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getString("catName");
			}
			
			if(check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing edit query on ..." + check);
				stmt.setString(1, description);
				stmt.setString(2,parent);
				stmt.setString(3,child);
				stmt.setString(4, name);
				stmt.executeUpdate();
				message = "operation successfull edited category id : "+name;
				Code = 007;
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
		String sql = "SELECT * FROM category WHERE catName > ? ORDER BY catName LIMIT 1";
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getNext query...");
			stmt.setString(1, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				message = "catName: "+rs.getString("catName")+"; catDesc: "+rs.getString("catDesc")+"; catParent: "+rs.getString("catParent");
				System.out.println(message);
				check = rs.getString("catName");
			}
			
			if(check != null ) {
				Code = 8;
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
		System.out.println("Inside GetNext method");
		String sql = "SELECT * FROM category WHERE catName < ? ORDER BY catName DESC LIMIT 1";
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getPrevious query...");
			stmt.setString(1, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				message = "catName: "+rs.getString("catName")+"; catDesc: "+rs.getString("catDesc")+"; catParent: "+rs.getString("catParent");
				System.out.println(message);
				check = rs.getString("catName");
			}
			
			if(check != null ) {
				Code = 9;
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
