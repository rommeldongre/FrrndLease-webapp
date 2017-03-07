package tableOps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import connect.Connect;
import pojos.LeaseTermsModel;
import util.FlsLogger;
import adminOps.Response;

public class LeaseTerms extends Connect {

	private FlsLogger LOGGER = new FlsLogger(LeaseTerms.class.getName());

	private String check = null, Id = null, token, name, description, operation, message;
	private int Code, duration;
	private LeaseTermsModel ltm;
	private Response res = new Response();

	public Response selectOp(String Operation, LeaseTermsModel ltml, JSONObject obj) {
		operation = Operation.toLowerCase();
		ltm = ltml;

		switch (operation) {

		case "add":
			LOGGER.info("Add op is selected..");
			Add();
			break;

		case "delete":
			LOGGER.info("Delete operation is selected");
			Delete();
			break;

		case "edit":
			LOGGER.info("Edit operation is selected.");
			Edit();
			break;

		case "getnext":
			LOGGER.info("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getNext();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;

		case "getprevious":
			LOGGER.info("Get Next operation is selected.");
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
		name = ltm.getName();
		description = ltm.getDescription();
		duration = ltm.getDuration();

		String sql = "insert into leaseterms (term_name,term_desc,term_duration) values (?,?,?)"; //
		PreparedStatement stmt = null;
		Connection hcp = getConnectionFromPool();

		try {
			LOGGER.info("Creating statement.....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing query.....");
			stmt.setString(1, name);
			stmt.setString(2, description);
			stmt.setInt(3, duration);
			stmt.executeUpdate();
			LOGGER.info("Entry added into leasetrems table");

			message = "Entry added into leaseTerms table";
			LOGGER.warning(message);
			Code = 20;
			Id = name;

			res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
		} catch (SQLException e) {
			LOGGER.warning("Couldn't create statement");
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				stmt.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void Delete() {
		name = ltm.getName();
		check = null;
		LOGGER.info("Inside delete method....");

		PreparedStatement stmt = null,stmt2 = null ;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		String sql = "DELETE FROM leaseterms WHERE term_name=?"; //
		String sql2 = "SELECT * FROM leaseterms WHERE term_name=?"; //

		try {
			LOGGER.info("Creating statement...");

			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setString(1, name);
			rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getString("term_name");
			}

			if (check != null) {
				stmt = hcp.prepareStatement(sql);

				LOGGER.info("Statement created. Executing delete query on ..." + check);
				stmt.setString(1, name);
				stmt.executeUpdate();
				message = "operation successfull deleted leaseTerm Req User id : " + name;
				LOGGER.warning(message);
				Code = 21;
				Id = check;
				res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
			} else {
				LOGGER.warning("Entry not found in database!!");
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				stmt2.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void Edit() {
		name = ltm.getName();
		description = ltm.getDescription();
		duration = ltm.getDuration();
		check = null;

		LOGGER.info("inside edit method");
		PreparedStatement stmt = null,stmt2 = null ;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		String sql = "UPDATE leaseterms SET term_desc=?,term_duration=? WHERE term_name=?"; //
		String sql2 = "SELECT * FROM leaseterms WHERE term_name=?"; //

		try {
			LOGGER.info("Creating Statement....");
			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setString(1, name);
			rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getString("term_name");
			}

			if (check != null) {
				stmt = hcp.prepareStatement(sql);

				LOGGER.info("Statement created. Executing edit query on ..." + check);
				stmt.setString(1, description);
				stmt.setInt(2, duration);
				stmt.setString(3, name);
				stmt.executeUpdate();
				message = "operation successfull edited leaseterm Req User id : " + name;
				LOGGER.warning(message);
				Code = 22;
				Id = check;
				res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
			} else {
				LOGGER.warning("Entry not found in database!!");
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				stmt2.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getNext() {
		check = null;
		LOGGER.info("Inside GetNext method");
		String sql = "SELECT * FROM leaseterms WHERE term_duration > ? ORDER BY term_duration LIMIT 1"; //

		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating a statement .....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getNext query...");
			stmt.setInt(1, Integer.parseInt(token));

			rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("termName", rs.getString("term_name"));
				json.put("termDesc", rs.getString("term_desc"));
				json.put("termDuration", rs.getInt("term_duration"));

				message = json.toString();
				LOGGER.warning(message);
				check = String.valueOf(rs.getInt("term_duration"));
			}

			if (check != null) {
				Code = FLS_SUCCESS;
				Id = check;
			}

			else {
				Id = "0";
				message = FLS_END_OF_DB_M;
				Code = FLS_END_OF_DB;
			}

			res.setData(Code, Id, message);
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (JSONException e) {
			res.setData(FLS_JSON_EXCEPTION, "0", FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getPrevious() {
		check = null;
		LOGGER.info("Inside GetPrevious method");
		String sql = "SELECT * FROM leaseterms WHERE term_name < ? ORDER BY term_name DESC LIMIT 1"; //

		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating a statement .....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getPrevious query...");
			stmt.setString(1, token);

			rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("termName", rs.getString("term_name"));
				json.put("termDesc", rs.getString("term_desc"));
				json.put("termDuration", rs.getInt("term_duration"));

				message = json.toString();
				LOGGER.info(message);
				check = rs.getString("term_name");
			}

			if (check != null) {
				Code = FLS_SUCCESS;
				Id = check;
			}

			else {
				Id = "0";
				message = FLS_END_OF_DB_M;
				Code = FLS_END_OF_DB;
			}

			res.setData(Code, Id, message);
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (JSONException e) {
			res.setData(FLS_JSON_EXCEPTION, "0", FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public int getDuration(String term) {
		int days = 0;
		LOGGER.info("Inside getDuration");
		String sql = "SELECT term_duration FROM leaseterms WHERE term_name=?";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();

		try {
			LOGGER.info("executing getDuration query...");
			stmt = hcp.prepareStatement(sql);
			stmt.setString(1, term);
			rs = stmt.executeQuery();
			while (rs.next()) {
				days = rs.getInt("term_duration");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return days;
	}

}
