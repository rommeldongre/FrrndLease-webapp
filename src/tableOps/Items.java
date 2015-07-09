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
	
	private String operation,category,leaseTerm,userId, status, title, description, message,Id=null;
	private int leaseValue,id,token,Code;
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
			
		case "editstat" :
			System.out.println("Edit status is selected");
			EditStat();
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
			
		case "browsen" :
			System.out.println("Browse Next Operation is selected.");
			try {
				token = obj.getInt("token");
				BrowseN();
			} catch (JSONException e) {
				res.setData(202, String.valueOf(token), "JSON Data not parsed/found");
				e.printStackTrace();
			}
			break;
			
		case "browsep" :
			System.out.println("Browse Previous Operation is selected.");
			try {
				token = obj.getInt("token");
				BrowseP();
			} catch (JSONException e) {
				res.setData(202, String.valueOf(token), "JSON Data not parsed/found");
				e.printStackTrace();
			}
			break;
			
		case "deletepost" :
			System.out.println("Delete Posting operation is selected");
			DeletePosting();
			break;
			
		case "deletewish" :
			System.out.println("Delete Wishlist operation is selected");
			DeleteWishlist();
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
		category = im.getCategory();
		userId = im.getUserId();
		leaseTerm = im.getLeaseTerm();
		leaseValue = im.getLeaseValue();
		status = im.getStatus();
		
		System.out.println("Inside add method...");
		String sql = "insert into items (item_name, item_category, item_desc, item_user_id, item_lease_value, item_lease_term, item_status) values (?,?,?,?,?,?,?)";
		
		getConnection();
		try {
			System.out.println("Creating statement.....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing query.....");
			stmt.setString(1, title);
			stmt.setString(2, category);
			stmt.setString(3, description);
			stmt.setString(4, userId);
			stmt.setInt(5, leaseValue);
			stmt.setString(6, leaseTerm);
			stmt.setString(7, status);
			stmt.executeUpdate();
			System.out.println("Item added into table");
			
			status = "operation successfull!!!";
			message = "Item added into table";
			Code = 000;
			
			//returning the new id
			sql = "SELECT MAX(item_id) FROM items";
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
		String sql = "DELETE FROM items WHERE item_id = ?";
		
		getConnection();
		try {
			System.out.println("Creating statement...");
			
			//checking whether the input id is present in table
			String sql2 = "SELECT * FROM items WHERE item_id=?";
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setInt(1, id);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getInt("item_id");
			}
			
			if(check != 0){
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing delete query..." + check);
				stmt.setInt(1, id);
				stmt.executeUpdate();
				status = "operation successfull deleted item id :" + id;
				Id = String.valueOf(check);
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
		id = im.getId();
		title = im.getTitle();
		description = im.getDescription();
		category = im.getCategory();
		userId = im.getUserId();
		leaseTerm = im.getLeaseTerm();
		leaseValue = im.getLeaseValue();
		status = im.getStatus();
		
		System.out.println("Inside edit method...");
		String sql = "UPDATE items SET item_name=?, item_category=?, item_desc=?, item_lease_value=?, item_lease_term=? WHERE item_id=? AND item_user_id=? AND item_status=?";
		
		getConnection();
		try {
			System.out.println("Creating statement...");
			
			String sql2 = "SELECT * FROM items WHERE item_id=? AND item_user_id=? AND item_status=?";
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setInt(1, id);
			stmt2.setString(2, userId);
			stmt2.setString(3, status);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getInt("item_id");
			}
			
			if(check != 0) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing edit query...");
				stmt.setString(1, title);
				stmt.setString(2, category);
				stmt.setString(3, description);
				stmt.setInt(4, leaseValue);
				stmt.setString(5, leaseTerm);
				stmt.setInt(6, id);
				stmt.setString(7, userId);
				stmt.setString(8, status);
				
				stmt.executeUpdate();
				message = "operation successfull edited item id : " +id;
				Id = String.valueOf(check);
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
	
	private void EditStat() {
		check = 0;
		id = im.getId();
		status = im.getStatus();
		
		System.out.println("Inside edit stat method...");
		String sql = "UPDATE items SET item_status=? WHERE item_id=?";
		
		getConnection();
		try {
			System.out.println("Creating statement...");
			
			String sql2 = "SELECT * FROM items WHERE item_id=?";
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setInt(1, id);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getInt("item_id");
			}
			
			if(check != 0) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing edit stat query...");
				stmt.setString(1, status);
				stmt.setInt(2, id);
				stmt.executeUpdate();
				message = "operation successfull edited item id : " +id;
				Id = String.valueOf(check);
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
		String sql = "SELECT * FROM items WHERE item_id > ? ORDER BY item_id LIMIT 1";
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getNext query...");
			stmt.setInt(1, token);
			
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getInt("item_id"));
				json.put("title", rs.getString("item_name"));
				json.put("category", rs.getString("item_category"));
				json.put("description", rs.getString("item_desc"));
				json.put("userId", rs.getString("item_user_id"));
				json.put("leaseValue", rs.getInt("item_lease_value"));
				json.put("leaseTerm", rs.getString("item_lease_term"));
				json.put("status", rs.getString("item_status"));
				
				message = json.toString();
				System.out.println(message);
				check = rs.getInt("item_id");
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
		} catch (JSONException e) {
			res.setData(204,"0", "JSON Exception");
			e.printStackTrace();
		}
	}
	
	private void GetPrevious() {
		check = 0;
		System.out.println("Inside getPrevious method");
		String sql = "SELECT * FROM items WHERE item_id < ? ORDER BY item_id DESC LIMIT 1";
		getConnection();
		
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getprevious query...");
			stmt.setInt(1, token);
			
			ResultSet rs = stmt.executeQuery();
			
			System.out.println("itemId\tName\tDescription\tQuantity");
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getInt("item_id"));
				json.put("title", rs.getString("item_name"));
				json.put("category", rs.getString("item_category"));
				json.put("description", rs.getString("item_desc"));
				json.put("userId", rs.getString("item_user_id"));
				json.put("leaseValue", rs.getInt("item_lease_value"));
				json.put("leaseTerm", rs.getString("item_lease_term"));
				json.put("status", rs.getString("item_status"));
				
				message = json.toString();
				System.out.println(message);
				check = rs.getInt("item_id");
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
		} catch (JSONException e) {
			res.setData(204,"0", "JSON Exception");
			e.printStackTrace();
		}
	}
	
	private void BrowseN () {
		check = 0;
		status = im.getStatus();
		
		System.out.println("Inside Browse N method");
		String sql = "SELECT * FROM items WHERE item_id > ? AND item_status= ? ORDER BY item_id LIMIT 1";
		getConnection();
		
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing Browse P query...");
			stmt.setInt(1, token);
			stmt.setString(2,status);
			
			ResultSet rs = stmt.executeQuery();
			
			System.out.println("itemId\tName\tDescription\tQuantity");
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getInt("item_id"));
				json.put("title", rs.getString("item_name"));
				json.put("category", rs.getString("item_category"));
				json.put("description", rs.getString("item_desc"));
				json.put("userId", rs.getString("item_user_id"));
				json.put("leaseValue", rs.getInt("item_lease_value"));
				json.put("leaseTerm", rs.getString("item_lease_term"));
				json.put("status", rs.getString("item_status"));
				
				message = json.toString();
				System.out.println(message);
				check = rs.getInt("item_id");
			}
			if(check != 0 ) { //checks if result Set is empty
				Id = String.valueOf(check);
				Code = 53;
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
		} catch (JSONException e) {
			res.setData(204,"0", "JSON Exception");
			e.printStackTrace();
		}
	}
	
	private void BrowseP() {
		check = 0;
		status = im.getStatus();
		
		System.out.println("Inside Browse P method");
		String sql = "SELECT * FROM items WHERE item_id < ? AND item_status= ? ORDER BY item_id DESC LIMIT 1";
		getConnection();
		
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing BrowseP query...");
			stmt.setInt(1, token);
			stmt.setString(2,status);
			
			ResultSet rs = stmt.executeQuery();
			
			System.out.println("itemId\tName\tDescription\tQuantity");
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getInt("item_id"));
				json.put("title", rs.getString("item_name"));
				json.put("category", rs.getString("item_category"));
				json.put("description", rs.getString("item_desc"));
				json.put("userId", rs.getString("item_user_id"));
				json.put("leaseValue", rs.getInt("item_lease_value"));
				json.put("leaseTerm", rs.getString("item_lease_term"));
				json.put("status", rs.getString("item_status"));
				
				message = json.toString();
				System.out.println(message);
				check = rs.getInt("item_id");
			}
			if(check != 0 ) { //checks if result Set is empty
				Id = String.valueOf(check);
				Code = 54;
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
		} catch (JSONException e) {
			res.setData(204,"0", "JSON Exception");
			e.printStackTrace();
		}
	}
	
	public String GetLeaseTerm(int itemId) {
		String term=null;
		System.out.println("Inside getItemLeaseTerm");
		String sql = "SELECT item_lease_term FROM items WHERE item_id=?";
		getConnection();
		
		try {
			System.out.println("executing getItemLesae Term query");
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setInt(1, itemId);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				term = rs.getString("item_lease_term");
				System.out.println(term);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return term;
	}
	
	private void DeletePosting() {
		id = im.getId();
		userId = im.getUserId();
		String check2 = null;
		System.out.println("Inside delete posting method....");
		
		
		getConnection();
		try {
			System.out.println("Creating statement...");
			
			//checking whether the input id is present in table
			String sql2 = "SELECT * FROM items WHERE item_id=? AND item_user_id=?";
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setInt(1, id);
			stmt2.setString(2, userId);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getInt("item_id");
				check2 = rs.getString("item_status");
				System.out.println(check2);
			}
			
			if(check != 0){
				
				switch(check2) {
				
				case "InStore" :
					Store st = new Store();
					st.DeleteP(id);//deletes entry from store table
					
					String sql = "DELETE FROM items WHERE item_id = ? AND item_user_id = ?";
					PreparedStatement stmt = connection.prepareStatement(sql);
					
					//deletes entry from items table
					
					System.out.println("Statement created. Executing delete posting query..." + check);
					stmt.setInt(1, id);
					stmt.setString(2, userId);
					stmt.executeUpdate();
					status = "Posting Deleted!!";
					Id = String.valueOf(check);
					message = status;
					Code = 001;
					res.setData(Code,Id,message);
					break;
					
				case "Leased" :
					status = "Item is leased, close the lease first!!!";
					Id = String.valueOf(check);
					message = status;
					Code = 215;
					res.setData(Code,Id,message);
					break;
					
				default :
					status = "Item is niether posted nor leased!!!";
					Id = String.valueOf(check);
					message = status;
					Code = 216;
					res.setData(Code,Id,message);
					break;
				}
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
	
	private void DeleteWishlist() {
		id = im.getId();
		userId = im.getUserId();
		String check2 = null;
		System.out.println("Inside delete wishlist method....");
		
		
		getConnection();
		try {
			System.out.println("Creating statement...");
			
			//checking whether the input id is present in table
			String sql2 = "SELECT * FROM items WHERE item_id=? AND item_user_id=? AND item_status=?";
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setInt(1, id);
			stmt2.setString(2, userId);
			stmt2.setString(3, "Wished");
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getInt("item_id");
				check2 = rs.getString("item_status");
				System.out.println(check2);
			}
			
			if(check != 0){
					Wishlist wish = new Wishlist();
					wish.DeleteW(id);
					
					String sql = "DELETE FROM items WHERE item_id = ? AND item_user_id = ?";
					PreparedStatement stmt = connection.prepareStatement(sql);
					
					//deletes entry from items table
					
					System.out.println("Statement created. Executing delete wishlist query..." + check);
					stmt.setInt(1, id);
					stmt.setString(2, userId);
					stmt.executeUpdate();
					status = "Wish Deleted!!";
					Id = String.valueOf(check);
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
