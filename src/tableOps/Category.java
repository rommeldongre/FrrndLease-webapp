package tableOps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import pojos.CategoryModel;
import util.FlsLogger;
import adminOps.Response;
import connect.Connect;

public class Category extends Connect {

	private FlsLogger LOGGER = new FlsLogger(Category.class.getName());

	private String name, description, parent, child, message, operation, Id = null, check = null, token;
	private int Code;
	private CategoryModel cm;
	private Response res = new Response();

	public Response selectOp(String Operation, CategoryModel ctm, JSONObject obj) {
		operation = Operation.toLowerCase();
		cm = ctm;

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
			;
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
		
		PreparedStatement stmt = null;
		Connection hcp = getConnectionFromPool();
		

		try {
			LOGGER.info("Creating statement.....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing query.....");
			stmt.setString(1, name);
			stmt.setString(2, description);
			stmt.setString(3, parent);
			stmt.setString(4, child);
			stmt.executeUpdate();
			message = "Entry added into category table";
			LOGGER.warning(message);
			Code = 005;
			Id = name;

			res.setData(FLS_SUCCESS, Id, FLS_CATEGORY_ADD);
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
		name = cm.getName();
		check = null;
		LOGGER.info("Inside delete method....");

		PreparedStatement stmt = null, stmt2 = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		
		String sql = "DELETE FROM category WHERE cat_name=?";
		String sql2 = "SELECT * FROM category WHERE cat_name=?";

		try {
			LOGGER.info("Creating statement...");

			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setString(1, name);
			rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getString("cat_name");
			}

			if (check != null) {
				stmt = hcp.prepareStatement(sql);

				LOGGER.info("Statement created. Executing delete query on ..." + check);
				stmt.setString(1, name);
				stmt.executeUpdate();
				message = "operation successfull deleted category id : " + name;
				LOGGER.warning(message);
				Code = 006;
				Id = check;
				res.setData(FLS_SUCCESS, Id, FLS_CATEGORY_DELETE);
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
		name = cm.getName();
		description = cm.getDescription();
		parent = cm.getParent();
		child = cm.getChild();
		check = null;

		LOGGER.info("inside edit method");
		
		PreparedStatement stmt = null, stmt2 = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		
		String sql = "UPDATE category SET cat_desc=?, cat_parent=?, cat_child=? WHERE cat_name=?";
		String sql2 = "SELECT * FROM category WHERE cat_name=?";

		try {
			LOGGER.info("Creating Statement....");
			stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, name);
			rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getString("cat_name");
			}

			if (check != null) {
				stmt = connection.prepareStatement(sql);

				LOGGER.info("Statement created. Executing edit query on ..." + check);
				stmt.setString(1, description);
				stmt.setString(2, parent);
				stmt.setString(3, child);
				stmt.setString(4, name);
				stmt.executeUpdate();
				message = "operation successfull edited category id : " + name;
				LOGGER.warning(message);
				Code = 007;
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
		String sql = "SELECT * FROM category WHERE cat_name > ? ORDER BY cat_name LIMIT 1";

		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating a statement .....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getNext query...");
			stmt.setString(1, token);

			rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("catName", rs.getString("cat_name"));
				json.put("catDesc", rs.getString("cat_desc"));
				json.put("catParent", rs.getString("cat_parent"));

				// message = "; catDesc: "+rs.getString("cat_desc")+";
				// catParent: "+rs.getString("cat_parent");
				message = json.toString();
				LOGGER.info(message);
				check = rs.getString("cat_name");
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
		String sql = "SELECT * FROM category WHERE cat_name < ? ORDER BY cat_name DESC LIMIT 1";

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
				json.put("catName", rs.getString("cat_name"));
				json.put("catDesc", rs.getString("cat_desc"));
				json.put("catParent", rs.getString("cat_parent"));

				// message = "catName: "+rs.getString("cat_name")+"; catDesc:
				// "+rs.getString("cat_desc")+"; catParent:
				// "+rs.getString("cat_parent");
				message = json.toString();
				LOGGER.info(message);
				check = rs.getString("cat_name");
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
}
