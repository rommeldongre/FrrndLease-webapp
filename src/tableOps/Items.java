package tableOps;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONException;
import org.json.JSONObject;

import connect.Connect;
import adminOps.Response;
import pojos.ItemsModel;

public class Items extends Connect {
	
	private String operation, status, title, description, message,Id=null;
	private int quantity,id,token,Code;
	private ItemsModel im;
	private Response res = new Response();
	private int check = 0;
	
	public Response selectOp(String Operation, ItemsModel itm, JSONObject obj) {
		operation = Operation.toLowerCase();
		im = itm;
		
		switch(operation) {
		
		case "add" :
			System.out.println("Add op is selected..");
			Add();
			//status = "Performing addition operation..";
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
				token = obj.getInt("token");
				System.out.println(token);
				GetNext();
			} catch (JSONException e) {
				res.setData(202, String.valueOf(token), "JSON Data not parsed/found");
				e.printStackTrace();
			}
			
			break;
			
		case "getprevious" :
			System.out.println("Get Previous Operation is selected.");
			try {
				token = obj.getInt("token");
				GetPrevious();
			} catch (JSONException e) {
				res.setData(202, String.valueOf(token), "JSON Data not parsed/found");
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
		title = im.getTitle();
		description = im.getDescription();
		quantity = im.getQuantity();
		
		System.out.println("Inside add method...");
		String sql = "insert into items (title, description, quantity) values (?,?,?)";
		
		getConnection();
		try {
			System.out.println("Creating statement.....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing query.....");
			stmt.setString(1, title);
			stmt.setString(2, description);
			stmt.setInt(3, quantity);
			stmt.executeUpdate();
			System.out.println("Item added into table");
			
			status = "operation successfull!!!";
			message = "Item added into table";
			Code = 000;
			
			//returning the new id
			sql = "SELECT MAX(itemId) FROM items";
			Statement stmt1 = connection.createStatement();
			ResultSet rs = stmt1.executeQuery(sql);
			while(rs.next()) {
				id = rs.getInt(1);
				System.out.println(Id);
			}
			Id = String.valueOf(id);
			res.setData(Code, Id, message);
			
		} catch (SQLException e) {
			System.out.println("Couldn't create statement");
			e.printStackTrace();
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
		}
	}
	
	private void Delete() {
		id = im.getId();
		check = 0;
		System.out.println("Inside delete method....");
		String sql = "DELETE FROM items WHERE itemId = ?";
		
		getConnection();
		try {
			System.out.println("Creating statement...");
			
			//checking whether the input id is present in table
			String sql2 = "SELECT * FROM items WHERE itemId=?";
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setInt(1, id);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getInt("itemId");
			}
			
			if(check != 0){
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing delete query..." + check);
				stmt.setInt(1, id);
				stmt.executeUpdate();
				status = "operation successfull deleted item id :" + id;
				Id = String.valueOf(id);
				message = status;
				Code = 001;
				res.setData(Code,Id,message);
			}
			else {
				System.out.println("Entry not found in database!!");
				res.setData(201, "0", "Entry not found in database!!");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
		}
	}
	
	private void Edit() {
		check = 0;
		title = im.getTitle();
		id = im.getId();
		description = im.getDescription();
		quantity = im.getQuantity();
		
		System.out.println("Inside edit method...");
		String sql = "UPDATE items SET title=?, description=?, quantity=? WHERE itemId=?";
		
		getConnection();
		try {
			System.out.println("Creating statement...");
			
			String sql2 = "SELECT * FROM items WHERE itemId=?";
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setInt(1, id);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getInt("itemId");
			}
			
			if(check != 0) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing edit query...");
				stmt.setString(1, title);
				stmt.setString(2, description);
				stmt.setInt(3, quantity);
				stmt.setInt(4, id);
				stmt.executeUpdate();
				status = "operation successfull edited item id : " +id;
				Id = String.valueOf(id);
				message = status;
				Code = 002;
				res.setData(Code,Id,message);
			}
			else {
				System.out.println("Entry not found in database!!");
				res.setData(201, "0", "Entry not found in database!!");
			}
			
		} catch (SQLException e) {
			System.out.println("Couldnt create a statement");
			e.printStackTrace();
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
		}
	}
	
	private void GetNext() {
		check = 0;
		System.out.println("Inside GetNext Method..");
		String sql = "SELECT * FROM items WHERE itemId > ? ORDER BY itemId LIMIT 1";
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getNext query...");
			stmt.setInt(1, token);
			
			ResultSet rs = stmt.executeQuery();
			
			System.out.println("itemId\tName\tDescription\tQuantity");
			while(rs.next()) {
				System.out.println(rs.getInt("itemId")+"\t"+rs.getString("title")+"\t"+rs.getString("description"));
				message = rs.getInt("itemId")+"\t"+rs.getString("title")+"\t"+rs.getString("description");
				check = rs.getInt("itemId");
				//System.out.println(id);
			}
			if(check != 0 ) {
				Code = 003;
				Id = String.valueOf(check);
			}
			else {
				Id = "0";
				message = "End of Database!!!";
				Code = 199;
			}
			
			res.setData(Code, Id, message);
			
			status = String.valueOf(id);
		} catch (SQLException e) {
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
			e.printStackTrace();
		}
	}
	
	private void GetPrevious() {
		check = 0;
		System.out.println("Inside getPrevious method");
		String sql = "SELECT * FROM items WHERE itemId < ? ORDER BY itemId DESC LIMIT 1";
		getConnection();
		
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getprevious query...");
			stmt.setInt(1, token);
			
			ResultSet rs = stmt.executeQuery();
			
			System.out.println("itemId\tName\tDescription\tQuantity");
			while(rs.next()) {
				System.out.println(rs.getInt("itemId")+"\t"+rs.getString("title")+"\t"+rs.getString("description"));
				message = rs.getInt("itemId")+"\t"+rs.getString("title")+"\t"+rs.getString("description");
				check = rs.getInt("itemId");
			}
			if(check != 0 ) { //checks if result Set is empty
				Id = String.valueOf(check);
				Code = 004;
			}
			else{
				message = "End of Database!!";
				Code = 199;
			}
			
			res.setData(Code, Id, message);
			
			//status = String.valueOf(Id);
		} catch (SQLException e) {
			System.out.println("Couldnt create a statement");
			e.printStackTrace();
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
		}
	}
	
	/*private void GetMax(){
		//id = im.getId();
		getConnection();
		try {
			Statement stmt1 = connection.createStatement();
			String sql = "SELECT MAX(itemId),title,description FROM items";
			ResultSet rs = stmt1.executeQuery(sql);
			while(rs.next()) {
				System.out.println(rs.getInt("itemId")+"\t"+rs.getString("title")+"\t"+rs.getString("description"));
				id = rs.getInt("itemId");
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*private void updateCounter() {
		try {
			Statement stm = connection.createStatement();
			String sql1 = "SET @num := 0;";
			String sql2 = "UPDATE items SET counter = @num := (@num+1);";
			String sql3 = "ALTER TABLE items AUTO_INCREMENT = 1;";
			
			stm.addBatch(sql1);
			stm.addBatch(sql2);
			stm.addBatch(sql3);
			
			System.out.println("Executing Query...");
			stm.executeBatch();
			
		} catch (SQLException e) {
			System.out.println("Couldn't update counter column..");
			e.printStackTrace();
		}
	}
	
	private void Search(int id) {
		
		String sql = "SELECT * FROM items WHERE id=?";
		getConnection();
		try {
			System.out.println("Searching for item in table...");
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
		} catch (SQLException e) {
			System.out.println("Couldnt create a statement");
			e.printStackTrace();
		}
	}*/

}
