package tableOps;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import adminOps.Response;
import pojos.StoreModel;
import util.FlsLogger;
import connect.Connect;

public class Store extends Connect {

	private FlsLogger LOGGER = new FlsLogger(Store.class.getName());

	private String Id = null, operation, message;
	private int Code, itemId, check = 0, token = 0;
	private StoreModel sm;
	private Response res = new Response();

	public Response selectOp(String Operation, StoreModel sem, JSONObject obj) {
		operation = Operation.toLowerCase();
		sm = sem;

		switch (operation) {

		case "add":
			LOGGER.info("Add op is selected..");
			Add();
			break;

		case "delete":
			LOGGER.info("Delete operation is selected");
			Delete();
			break;

		case "getnext":
			LOGGER.info("Get Next operation is selected.");
			try {
				token = obj.getInt("token");
				getNext();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;

		case "getprevious":
			LOGGER.info("Get Next operation is selected.");
			try {
				token = obj.getInt("token");
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
		itemId = sm.getItemId();

		String sql = "insert into store (store_item_id) values (?)"; //
		getConnection();

		try {
			LOGGER.info("Creating statement.....");
			PreparedStatement stmt = connection.prepareStatement(sql);

			LOGGER.info("Statement created. Executing query.....");
			stmt.setInt(1, itemId);
			stmt.executeUpdate();
			message = "Entry added into store table";
			LOGGER.warning(message);
			Code = 29;
			Id = String.valueOf(itemId);

			res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
		} catch (SQLException e) {
			LOGGER.warning("Couldn't create statement");
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}
	}

	private void Delete() {
		itemId = sm.getItemId();
		check = 0;
		LOGGER.info("Inside delete method....");

		getConnection();
		String sql = "DELETE FROM store WHERE store_item_id=?"; //
		String sql2 = "SELECT * FROM store WHERE store_item_id=?"; //

		try {
			LOGGER.info("Creating statement...");

			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setInt(1, itemId);
			ResultSet rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getInt("store_item_id");
			}

			if (check != 0) {
				PreparedStatement stmt = connection.prepareStatement(sql);

				LOGGER.info("Statement created. Executing delete query on ..." + check);
				stmt.setInt(1, itemId);
				stmt.executeUpdate();
				message = "operation successfully deleted store item id : " + itemId;
				LOGGER.warning(message);
				Code = 30;
				Id = String.valueOf(check);
				res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
			} else {
				LOGGER.warning("Entry not found in database!!");
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}

	}

	private void getNext() {
		check = 0;
		LOGGER.info("Inside GetNext method");
		String sql = "SELECT * FROM store WHERE store_item_id > ? ORDER BY store_item_id LIMIT 1"; //

		getConnection();
		try {
			LOGGER.info("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getNext query...");
			stmt.setInt(1, token);

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getInt("store_item_id"));

				message = json.toString();
				LOGGER.warning(message);
				check = rs.getInt("store_item_id");
			}

			if (check != 0) {
				Code = FLS_SUCCESS;
				Id = String.valueOf(check);
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
		}
	}

	private void getPrevious() {
		check = 0;
		LOGGER.info("Inside GetPrevious method");
		String sql = "SELECT * FROM store WHERE store_item_id < ? ORDER BY store_item_id DESC LIMIT 1"; //

		getConnection();
		try {
			LOGGER.info("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getNext query...");
			stmt.setInt(1, token);

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getInt("store_item_id"));

				message = json.toString();
				LOGGER.warning(message);
				check = rs.getInt("store_item_id");
			}

			if (check != 0) {
				Code = FLS_SUCCESS;
				Id = String.valueOf(check);
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
		}
	}

	public void DeleteP(int id) { // used in delete posting
		check = 0;
		LOGGER.info("Inside deleteP method of store....");

		getConnection();
		String sql = "DELETE FROM store WHERE store_item_id=?"; //
		String sql2 = "SELECT * FROM store WHERE store_item_id=?"; //

		try {
			LOGGER.info("Creating statement...");
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setInt(1, id);
			ResultSet rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getInt("store_item_id");
			}

			if (check != 0) {
				PreparedStatement stmt = connection.prepareStatement(sql);

				LOGGER.info("Statement created. Executing delete query on ..." + check);
				stmt.setInt(1, id);
				stmt.executeUpdate();
				message = "operation successfully deleted store item id : " + id;
				LOGGER.warning(message);
			} else {
				LOGGER.warning("Entry not found in database!!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
