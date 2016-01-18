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
import util.FlsSendMail;

public class Leases extends Connect {
	
	private String check=null, Id=null,token,reqUserId, itemId,userId,operation,message,status;
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
			
		case "editstat" :
			System.out.println("Edit operation is selected.");
			EditStat();
			break;
			
		case "getnext" :
			System.out.println("Get Next operation is selected.");
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
			
		case "getnextactive" :
			System.out.println("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getNextActive();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;
			
		case "getpreviousactive" :
			System.out.println("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getPreviousActive();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;
			
		case "renewlease" :
			RenewLease();
			break;
			
		default:
			res.setData(FLS_INVALID_OPERATION, "0", FLS_INVALID_OPERATION_M);
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
			
			try{
				FlsSendMail newE = new FlsSendMail();
				newE.send(userId,FlsSendMail.Fls_Enum.FLS_MAIL_GRANT_LEASE_FROM,lm);
				newE.send(reqUserId,FlsSendMail.Fls_Enum.FLS_MAIL_GRANT_LEASE_TO,lm);
				}catch(Exception e){
				  e.printStackTrace();
				}
			
			res.setData(FLS_SUCCESS,Id,FLS_SUCCESS_M);
		} catch (SQLException e) {
			System.out.println("Couldn't create statement");
			 res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
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
	
	private void EditStat() {
		reqUserId = lm.getReqUserId();
		itemId = lm.getItemId();
		status = lm.getStatus();
		check = null;
		LeasesModel lm1 = new LeasesModel();
		
		System.out.println("inside edit method");
		getConnection();
		String sql = "UPDATE leases SET lease_status = ? WHERE lease_requser_id=? AND lease_item_id=? AND lease_status=?";							//
		
		//code for populating lease pojo for sending owner email...
		String sqlrf ="SELECT * FROM leases WHERE lease_item_id=?";
		   try {
			System.out.println("Creating Statement....");
			PreparedStatement stmtrf = connection.prepareStatement(sqlrf);
			stmtrf.setString(1, itemId);
		
			System.out.println("Statement created. Executing select query on ..." + check);
			ResultSet dbResponse = stmtrf.executeQuery();
			
			if(dbResponse.next()) {
				check = dbResponse.getString("lease_item_id");
			
			
			if (check!= null) {
				System.out.println("Inside Nested check statement for FLS_MAIL_REJECT_LEASE_FROM");
				System.out.println(check);
				
				//Populate the response
				try {
					JSONObject obj1 = new JSONObject();
					obj1.put("reqUserId", dbResponse.getString("lease_requser_id"));
					obj1.put("itemId", dbResponse.getString("lease_item_id"));
					obj1.put("userId", dbResponse.getString("lease_user_id"));
					obj1.put("status", dbResponse.getString("lease_status"));
					
					lm1.getData(obj1);
					System.out.println("Json parsed for FLS_MAIL_REJECT_LEASE_FROM");
				} catch (JSONException e) {
					System.out.println("Couldn't parse/retrieve JSON for FLS_MAIL_REJECT_LEASE_FROM");
					e.printStackTrace();
				}
			}
	      }
		   } catch (SQLException e) {
				 res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
					e.printStackTrace();
				}
		//code for populating lease pojo for sending owner email ends here...
		
		try {
			System.out.println("Creating Statement....");
			PreparedStatement stmt = connection.prepareStatement(sql);
				
			System.out.println("Statement created. Executing edit query on ..." + check);
			stmt.setString(1, status);
			stmt.setString(2, reqUserId);
			stmt.setString(3, itemId);
			stmt.setString(4, "Active");
			stmt.executeUpdate();
			message = "operation successfull edited lease Req User id : "+reqUserId;
			Code = 17;
			Id = check;
			res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
			
			try{
				FlsSendMail newE = new FlsSendMail();
				userId = lm1.getUserId();
				newE.send(userId,FlsSendMail.Fls_Enum.FLS_MAIL_REJECT_LEASE_FROM,lm1);
				newE.send(reqUserId,FlsSendMail.Fls_Enum.FLS_MAIL_REJECT_LEASE_TO,lm);
				}catch(Exception e){
				  e.printStackTrace();
				}
			
		} catch (SQLException e) {
			 res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
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
				res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
			
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}
	}
}
