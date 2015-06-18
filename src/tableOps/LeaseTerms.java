package tableOps;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import connect.Connect;
import pojos.LeaseTermsModel;
import adminOps.Response;

public class LeaseTerms extends Connect {
	private String check=null, Id=null,token,name,description,operation,message;
	private int Code,duration;
	private LeaseTermsModel ltm;
	private Response res = new Response();
	
	public Response selectOp(String Operation, LeaseTermsModel ltml, JSONObject obj) {
		operation = Operation.toLowerCase();
		ltm = ltml;
		
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
		name = ltm.getName();
		description = ltm.getDescription();
		duration = ltm.getDuration();
		
		String sql = "insert into leaseterms (term_name,term_desc,term_duration) values (?,?,?)";		 //
		getConnection();
		
		try {
			System.out.println("Creating statement.....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing query.....");
			stmt.setString(1, name);
			stmt.setString(2, description);
			stmt.setInt(3, duration);
			stmt.executeUpdate();
			System.out.println("Entry added into leasetrems table");
			
			message = "Entry added into leaseTerms table";
			Code = 20;
			Id = name;
			
			res.setData(Code,Id,message);
		} catch (SQLException e) {
			System.out.println("Couldn't create statement");
			res.setData(200, "0", "Couldn't create statement, or couldn't execute a query(SQL Exception)");
			e.printStackTrace();
		}
	}
	
	private void Delete() {
		name = ltm.getName();
		check = null;
		System.out.println("Inside delete method....");
		
		getConnection();
		String sql = "DELETE FROM leaseterms WHERE term_name=?";			//
		String sql2 = "SELECT * FROM leaseterms WHERE term_name=?";			//
		
		try {
			System.out.println("Creating statement...");
			
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, name);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getString("term_name");
			}
			
			if(check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing delete query on ..." + check);
				stmt.setString(1, name);
				stmt.executeUpdate();
				message = "operation successfull deleted leaseTerm Req User id : "+name;
				Code = 21;
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
		name = ltm.getName();
		description = ltm.getDescription();
		duration = ltm.getDuration();
		check = null;
		
		System.out.println("inside edit method");
		getConnection();
		String sql = "UPDATE leaseterms SET term_desc=?,term_duration=? WHERE term_name=?";			//
		String sql2 = "SELECT * FROM leaseterms WHERE term_name=?";								//
		
		try {
			System.out.println("Creating Statement....");
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, name);
			ResultSet rs = stmt2.executeQuery();
			while(rs.next()) {
				check = rs.getString("term_name");
			}
			
			if(check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);
				
				System.out.println("Statement created. Executing edit query on ..." + check);
				stmt.setString(1, description);
				stmt.setInt(2, duration);
				stmt.setString(3,name);
				stmt.executeUpdate();
				message = "operation successfull edited leaseterm Req User id : "+name;
				Code = 22;
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
		String sql = "SELECT * FROM leaseterms WHERE term_name > ? ORDER BY term_name LIMIT 1";		//
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getNext query...");
			stmt.setString(1, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				message = "term Name: "+rs.getString("term_name")+"; term Description : "+rs.getString("term_desc")+"; term Duration : "+rs.getInt("term_duration");
				System.out.println(message);
				check = rs.getString("term_name");
			}
			
			if(check != null ) {
				Code = 23;
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
		System.out.println("Inside GetPrevious method");
		String sql = "SELECT * FROM leaseterms WHERE term_name < ? ORDER BY term_name DESC LIMIT 1";			//
		
		getConnection();
		try {
			System.out.println("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			System.out.println("Statement created. Executing getPrevious query...");
			stmt.setString(1, token);
			
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				message = "term Name: "+rs.getString("term_name")+"; term Description : "+rs.getString("term_desc")+"; term Duration : "+rs.getInt("term_duration");
				System.out.println(message);
				check = rs.getString("term_name");
			}
			
			if(check != null ) {
				Code = 24;
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
