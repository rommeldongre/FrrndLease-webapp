package tableOps;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.json.JSONException;
import org.json.JSONObject;

import connect.Connect;
import pojos.LeasesModel;
import adminOps.Response;

public class Leases extends Connect {
	
	private String check=null, Id=null,token,reqUserId, itemId,userId,operation,message;
	private int Code;
	private LeasesModel lm;
	private Response res = new Response();
	
	public Response selectOp(String Operation, LeasesModel lsm, JSONObject obj) {
		operation = Operation.toLowerCase();
		lm = lsm;
		
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
			
		case "getnextactive" :
			System.out.println("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getNextActive();
			} catch (JSONException e) {
				res.setData(202, String.valueOf(token), "JSON Data not parsed/found(JSON Exception)");
				e.printStackTrace();
			}
			break;
			
		case "getpreviousactive" :
			System.out.println("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getPreviousActive();
			} catch (JSONException e) {
				res.setData(202, String.valueOf(token), "JSON Data not parsed/found(JSON Exception)");
				e.printStackTrace();
			}
			break;
			
		case "renewlease" :
			RenewLease();
			break;
			
		default:
			res.setData(202, "0", "Invalid Operation!!");;
			break;
		}
		
		return res;
	}
	
	private void Add() {
		reqUserId = lm.getReqUserId();
		itemId = lm.getItemId();
		userId = lm.getUserId();
		int days;
		
		Items item = new Items();
		String term = item.GetLeaseTerm(Integer.parseInt(itemId));
		
		LeaseTerms Term = new LeaseTerms();
		
		days = Term.getDuration(term);
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, days);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(cal.getTime());
		
		String sql = "insert into leases (lease_requser_id,lease_item_id,lease_user_id,lease_expiry_date) values (?,?,?,?)";		 //
		getConnection();
		
		try {
			System.out.println("Creating statement.....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing query.....");
			stmt.setString(1, reqUserId);
			stmt.setString(2, itemId);
			stmt.setString(3, userId);
			stmt.setString(4, date);
			stmt.executeUpdate();
			System.out.println("Entry added into leases table");
			
			message = "Entry added into leases table";
			Code = 15;
			Id = reqUserId;
			
			res.setData(Code,Id,message);
		} catch (SQLException e) {
			System.out.println("Couldn't create statement");
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
			e.printStackTrace();
		}
	}
	
	private void Delete() {
		reqUserId = lm.getReqUserId();
		itemId = lm.getItemId();
		check = null;
		System.out.println("Inside delete method....");
		
		getConnection();
		String sql = "DELETE FROM leases WHERE lease_requser_id=?,lease_item_id=?";			//
		String sql2 = "SELECT * FROM leases WHERE lease_requser_id=?,lease_item_id=?";			//
		
		try {
			System.out.println("Creating statement...");
			
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, reqUserId);
			stmt2.setString(2, itemId);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getString("lease_requser_id");
			}
			
			if(check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing delete query on ..." + check);
				stmt.setString(1, reqUserId);
				stmt.setString(2, itemId);
				stmt.executeUpdate();
				message = "operation successfull deleted lease Req User id : "+reqUserId;
				Code = 16;
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
		reqUserId = lm.getReqUserId();
		itemId = lm.getItemId();
		userId = lm.getUserId();
		check = null;
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(cal.getTime());
		
		System.out.println("inside edit method");
		getConnection();
		String sql = "UPDATE leases SET lease_user_id=?,lease_expiry_date=? WHERE lease_requser_id=? AND lease_item_id=?";			//
		String sql2 = "SELECT * FROM leases WHERE lease_requser_id=? AND lease_item_id=?";								//
		
		try {
			System.out.println("Creating Statement....");
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, reqUserId);
			stmt2.setString(2, itemId);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getString("lease_requser_id");
			}
			
			if(check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing edit query on ..." + check);
				stmt.setString(1, userId);
				stmt.setString(2, date);
				stmt.setString(3, reqUserId);
				stmt.setString(4, itemId);
				stmt.executeUpdate();
				message = "operation successfull edited lease Req User id : "+reqUserId;
				Code = 17;
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
		String sql = "SELECT * FROM leases WHERE lease_requser_id > ? ORDER BY lease_requser_id LIMIT 1";		//
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getNext query...");
			stmt.setString(1, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("reqUserId", rs.getString("lease_requser_id"));
				json.put("itemId", rs.getString("lease_item_id"));
				json.put("userId", rs.getString("lease_user_id"));
				json.put("expiry", rs.getString("lease_expiry_date"));
				
				message = json.toString();
				System.out.println(message);
				check = rs.getString("lease_requser_id");
			}
			
			if(check != null ) {
				Code = 18;
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
			res.setData(204,"0", "JSON Exception");
			e.printStackTrace();
		}	
	}
	
	private void getPrevious() {
		check = null;
		System.out.println("Inside GetPrevious method");
		String sql = "SELECT * FROM leases WHERE lease_requser_id < ? ORDER BY lease_requser_id DESC LIMIT 1";			//
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getPrevious query...");
			stmt.setString(1, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("reqUserId", rs.getString("lease_requser_id"));
				json.put("itemId", rs.getString("lease_item_id"));
				json.put("userId", rs.getString("lease_user_id"));
				json.put("expiry", rs.getString("lease_expiry_date"));
				
				message = json.toString();
				System.out.println(message);
				check = rs.getString("lease_requser_id");
			}
			
			if(check != null ) {
				Code = 19;
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
			res.setData(204,"0", "JSON Exception");
			e.printStackTrace();
		}	
	}
	
	private void getNextActive () {
		int t = Integer.parseInt(token);
		check = null;
		System.out.println("Inside GetNext Active method");
		String sql = "SELECT * FROM leases WHERE lease_id > ? AND lease_status=? ORDER BY lease_id LIMIT 1";		//
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getNext query...");
			stmt.setInt(1, t);
			stmt.setString(2, "Active");
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("reqUserId", rs.getString("lease_requser_id"));
				json.put("itemId", rs.getString("lease_item_id"));
				json.put("userId", rs.getString("lease_user_id"));
				json.put("expiry", rs.getString("lease_expiry_date"));
				
				message = json.toString();
				System.out.println(message);
				check = String.valueOf(rs.getInt("lease_id"));
			}
			
			if(check != null ) {
				Code = 18;
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
			res.setData(204,"0", "JSON Exception");
			e.printStackTrace();
		}	
	}
	
	private void getPreviousActive() {
		check = null;
		int t = Integer.parseInt(token);
		System.out.println("Inside GetPrevious method");
		String sql = "SELECT * FROM leases WHERE lease_id < ? AND lease_status=? ORDER BY lease_id DESC LIMIT 1";			//
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getPrevious query...");
			stmt.setInt(1, t);
			stmt.setString(2, "Active");
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				JSONObject json = new JSONObject();
				json.put("reqUserId", rs.getString("lease_requser_id"));
				json.put("itemId", rs.getString("lease_item_id"));
				json.put("userId", rs.getString("lease_user_id"));
				json.put("expiry", rs.getString("lease_expiry_date"));
				
				message = json.toString();
				System.out.println(message);
				check = String.valueOf(rs.getInt("lease_id"));
			}
			
			if(check != null ) {
				Code = 19;
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
			res.setData(204,"0", "JSON Exception");
			e.printStackTrace();
		}	
	}
	
	private void RenewLease() {
		reqUserId = lm.getReqUserId();
		itemId = lm.getItemId();
		check = null;
		String date1 = null;
		
		Calendar cal = new GregorianCalendar();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//String date = sdf.format(cal.getTime());
		
		System.out.println("inside edit method");
		getConnection();
		
		String sql1 = "SELECT lease_expiry_date,lease_id FROM leases WHERE lease_requser_id=? AND lease_item_id=?";
		
		try {
			PreparedStatement stmt1 = connection.prepareStatement(sql1);
			stmt1.setString(1, reqUserId);
			stmt1.setString(2, itemId);
			
			ResultSet rs = stmt1.executeQuery();
			while(rs.next()) {
				date1 = rs.getString("lease_expiry_date");
				check = String.valueOf(rs.getInt("lease_id"));
				System.out.println(date1);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		Items item = new Items();
		String term = item.GetLeaseTerm(Integer.parseInt(itemId));
		
		LeaseTerms Term = new LeaseTerms();
		int days = Term.getDuration(term);
		
		try {
			cal.setTime(sdf.parse(date1));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		cal.add(Calendar.DATE, days);
		String date = sdf.format(cal.getTime());
		System.out.println(date);
		
		String sql = "UPDATE leases SET lease_expiry_date=? WHERE lease_requser_id=? AND lease_item_id=?";								//
		
		try {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing renew query ...");
				stmt.setString(1, date);
				stmt.setString(2, reqUserId);
				stmt.setString(3, itemId);
				stmt.executeUpdate();
				message = "operation successfull edited lease Req User id : "+reqUserId;
				Code = 17;
				Id = check;
				res.setData(Code, Id, message);
			
		} catch (SQLException e) {
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
			e.printStackTrace();
		}
	}
}
