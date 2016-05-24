package tableOps;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import adminOps.Response;
import connect.Connect;
import pojos.RequestsModel;
import pojos.ItemsModel;
import util.FlsSendMail;
import util.AwsSESEmail;
import util.FlsLogger;

public class Requests extends Connect {

	private FlsLogger LOGGER = new FlsLogger(Requests.class.getName());

	private String check = null, Id = null, token, userId, itemId, operation, message;
	private int Code;
	private RequestsModel rm;
	private Response res = new Response();

	public Response selectOp(String Operation, RequestsModel rtm, JSONObject obj) {
		operation = Operation.toLowerCase();
		rm = rtm;

		switch (operation) {

		case "add":
			LOGGER.info("Add op is selected..");
			Add();
			break;

		case "delete":
			LOGGER.info("Delete operation is selected");
			Delete();
			break;

		/*
		 * case "deleteone" : System.out.println("DeleteOne op is selected");
		 * DeleteOne(); break;
		 */

		case "edits":
			LOGGER.info("Edit s operation is selected");
			EditS();
			break;

		case "editone":
			LOGGER.info("Edit one operation is selected");
			EditOne();
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

		case "getnextr":
			LOGGER.info("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getNextR();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;

		case "getpreviousr":
			LOGGER.info("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getPreviousR();
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
		userId = rm.getUserId();
		itemId = rm.getItemId();
		check = null;

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(cal.getTime());

		String sql1 = "SELECT * FROM requests WHERE request_item_id=? AND request_requser_id=? AND request_status = ? ";

		String sql = "insert into requests (request_requser_id,request_item_id,request_date) values (?,?,?)"; //
		getConnection();

		try {

			LOGGER.info("Creating statement.....");
			PreparedStatement stmt1 = connection.prepareStatement(sql1);
			stmt1.setString(1, itemId);
			stmt1.setString(2, userId);
			stmt1.setString(3, "Active");

			ResultSet rs = stmt1.executeQuery();

			while (rs.next()) {
				check = rs.getString("request_requser_id");
			}

			if (check == null) {

				// code to check whether item has been already leased out not
				String checklease = null;
				LOGGER.info("Creating statement to check if lease exists.....");
				String sql3 = "SELECT * FROM leases WHERE lease_item_id=? AND lease_requser_id=? AND lease_status =?";
				PreparedStatement stmt3 = connection.prepareStatement(sql3);
				stmt3.setString(1, itemId);
				stmt3.setString(2, userId);
				stmt3.setString(3, "Active");

				ResultSet rslease = stmt3.executeQuery();

				while (rslease.next()) {
					checklease = rslease.getString("lease_id");
				}
				// code to check whether item has been already leased out not
				// ends here
				if (checklease == null) {

					// code for populating item pojo for sending owner email
					String ownerUserId;
					ItemsModel im = new ItemsModel();
					String sql2 = "SELECT * FROM items WHERE item_id=?";
					LOGGER.info("Creating a statement .....");
					PreparedStatement stmt2 = connection.prepareStatement(sql2);

					LOGGER.info("Statement created. Executing select row query...");
					stmt2.setString(1, itemId);

					ResultSet dbResponse = stmt2.executeQuery();
					LOGGER.info("Query to request pojos fired into requests table");
					if (dbResponse.next()) {

						if (dbResponse.getString("item_id") != null) {
							LOGGER.info("Inside Nested check1 statement");

							// Populate the response
							try {
								JSONObject obj1 = new JSONObject();
								obj1.put("title", dbResponse.getString("item_name"));
								obj1.put("description", dbResponse.getString("item_desc"));
								obj1.put("category", dbResponse.getString("item_category"));
								obj1.put("userId", dbResponse.getString("item_user_id"));
								obj1.put("leaseTerm", dbResponse.getString("item_lease_term"));
								obj1.put("id", dbResponse.getString("item_id"));
								obj1.put("leaseValue", dbResponse.getString("item_lease_value"));
								obj1.put("status", "InStore");
								obj1.put("image", " ");

								im.getData(obj1);
								LOGGER.warning("Json parsed for FLS_MAIL_MAKE_REQUEST_TO");
							} catch (JSONException e) {
								LOGGER.warning("Couldn't parse/retrieve JSON for FLS_MAIL_MAKE_REQUEST_TO");
								e.printStackTrace();
							}

						}
					}
					// code for populating item pojo for sending owner email
					// ends here
					
					// add credit to user reuesting item
					String sqlAddCredit = "UPDATE users SET user_credit=user_credit+1 WHERE user_id=?";
					PreparedStatement s1 = connection.prepareStatement(sqlAddCredit);
					s1.setString(1, userId);
					s1.executeUpdate();
					
					PreparedStatement stmt = connection.prepareStatement(sql);
	
					LOGGER.info("Statement created. Executing query.....");
					stmt.setString(1, userId);
					stmt.setString(2, itemId);
					stmt.setString(3, date);
					stmt.executeUpdate();
					LOGGER.warning("Entry added into requests table");
	
					message = FLS_SUCCESS_M;
					Code = FLS_SUCCESS;
					Id = itemId;
	
					try {
						AwsSESEmail newE = new AwsSESEmail();
						ownerUserId = im.getUserId();
						newE.send(userId, FlsSendMail.Fls_Enum.FLS_MAIL_MAKE_REQUEST_FROM, rm);
						LOGGER.info("Statement FLS_MAIL_MAKE_REQUEST_FROM fired......");
						newE.send(ownerUserId, FlsSendMail.Fls_Enum.FLS_MAIL_MAKE_REQUEST_TO, im);
						LOGGER.info("Statement FLS_MAIL_MAKE_REQUEST_TO fired......");
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					message = FLS_DUPLICATE_ENTRY_L;
					Code = FLS_DUPLICATE_ENTRY;
					Id = "0";
				}
			} else {
				message = FLS_DUPLICATE_ENTRY_M;
				Code = FLS_DUPLICATE_ENTRY;
				Id = "0";
			}
			res.setData(Code, Id, message);
		}catch(

	SQLException e)
	{
		LOGGER.warning("Couldn't create statement");
		res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
		e.printStackTrace();
	}
	}

	private void Delete() {
		itemId = rm.getItemId();
		check = null;
		LOGGER.info("Inside delete method....");

		getConnection();
		String sql = "DELETE FROM requests WHERE request_item_id=?"; //
		String sql2 = "SELECT * FROM requests WHERE request_item_id=?"; //

		try {
			LOGGER.info("Creating statement...");

			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, itemId);
			ResultSet rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getString("request_item_id");
			}

			if (check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);

				LOGGER.info("Statement created. Executing delete query on ..." + check);
				stmt.setString(1, itemId);
				stmt.executeUpdate();
				message = "operation successfull deleted request item id : " + itemId;
				LOGGER.warning(message);
				Code = 26;
				Id = check;
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

	/*
	 * private void DeleteOne() { itemId = rm.getItemId(); userId =
	 * rm.getUserId(); check = null; System.out.println(
	 * "Inside delete method....");
	 * 
	 * getConnection(); String sql =
	 * "DELETE FROM requests WHERE request_item_id=? AND request_requser_id=?";
	 * // String sql2 =
	 * "SELECT * FROM requests WHERE request_item_id=? AND request_requser_id=?"
	 * ; //
	 * 
	 * try { System.out.println("Creating statement...");
	 * 
	 * PreparedStatement stmt2 = connection.prepareStatement(sql2);
	 * stmt2.setString(1, itemId); stmt2.setString(2, userId); ResultSet rs =
	 * stmt2.executeQuery(); while(rs.next()) { check =
	 * rs.getString("request_item_id"); }
	 * 
	 * if(check != null) { PreparedStatement stmt =
	 * connection.prepareStatement(sql);
	 * 
	 * System.out.println("Statement created. Executing delete query on ..." +
	 * check); stmt.setString(1, itemId); stmt.setString(2, userId);
	 * stmt.executeUpdate(); message =
	 * "operation successfull deleted request item id : "+itemId; Code = 56; Id
	 * = check; res.setData(Code, Id, message); } else{ System.out.println(
	 * "Entry not found in database!!"); res.setData(201, "0",
	 * "Entry not found in database!!"); } } catch (SQLException e) {
	 * res.setData(200, "0",
	 * "Couldn't create statement, or couldn't execute a query(SQL Exception)");
	 * e.printStackTrace(); } }
	 */

	private void EditS() {
		itemId = rm.getItemId();
		userId = rm.getUserId();
		String status = "Archived";
		check = null;

		LOGGER.info("inside edit method");
		getConnection();
		String sql = "UPDATE requests SET request_status=? WHERE request_item_id=?"; //
		String sql2 = "SELECT * FROM requests WHERE request_item_id=?"; //

		try {
			LOGGER.info("Creating Statement....");
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, itemId);
			ResultSet rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getString("request_item_id");
			}

			if (check != null) {
				PreparedStatement stmt = connection.prepareStatement(sql);

				LOGGER.info("Statement created. Executing edit query on ..." + check);
				stmt.setString(1, status);
				stmt.setString(2, itemId);
				stmt.executeUpdate();
				message = "operation successfull edited item id : " + itemId;
				LOGGER.warning(message);
				Code = 56;
				Id = check;
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

	private void EditOne() {
		itemId = rm.getItemId();
		userId = rm.getUserId();
		String status = "Archived";
		check = null;

		LOGGER.info("inside edit method");
		getConnection();
		String sql = "UPDATE requests SET request_status=? WHERE request_item_id=? AND request_requser_id=?"; //
		String sql2 = "SELECT * FROM requests WHERE request_item_id=? AND request_requser_id=? AND request_status=?"; //

		try {
			LOGGER.info("Creating Statement....");
			PreparedStatement stmt2 = connection.prepareStatement(sql2);
			stmt2.setString(1, itemId);
			stmt2.setString(2, userId);
			stmt2.setString(3, "Active");
			ResultSet rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getString("request_id");
			}

			if (check != null) {

				// code for populating item pojo for sending requester email
				RequestsModel rm1 = new RequestsModel();
				ItemsModel im = new ItemsModel();
				String sql1 = "SELECT * FROM items WHERE item_id=?";
				LOGGER.info("Creating a statement .....");
				PreparedStatement stmt1 = connection.prepareStatement(sql1);

				LOGGER.info("Statement created. Executing select row query of FLS_MAIL_REJECT_REQUEST_TO...");
				stmt1.setString(1, itemId);

				ResultSet dbResponse = stmt1.executeQuery();
				LOGGER.info("Query to request pojos fired into requests table");
				if (dbResponse.next()) {

					if (dbResponse.getString("item_id") != null) {
						LOGGER.info("Inside Nested check1 statement of FLS_MAIL_REJECT_REQUEST_TO");

						// Populate the response
						try {
							JSONObject obj1 = new JSONObject();
							obj1.put("title", dbResponse.getString("item_name"));
							obj1.put("description", dbResponse.getString("item_desc"));
							obj1.put("category", dbResponse.getString("item_category"));
							obj1.put("userId", dbResponse.getString("item_user_id"));
							obj1.put("leaseTerm", dbResponse.getString("item_lease_term"));
							obj1.put("id", dbResponse.getString("item_id"));
							obj1.put("leaseValue", dbResponse.getString("item_lease_value"));
							obj1.put("status", "InStore");
							obj1.put("image", " ");

							im.getData(obj1);
							LOGGER.info("Json parsed for FLS_MAIL_REJECT_REQUEST_TO");
						} catch (JSONException e) {
							LOGGER.warning("Couldn't parse/retrieve JSON for FLS_MAIL_REJECT_REQUEST_TO");
							e.printStackTrace();
						}

					}
				}
				// code for populating item pojo for sending requester email
				// ends here

				PreparedStatement stmt = connection.prepareStatement(sql);

				LOGGER.info("Statement created. Executing edit query on ..." + check);
				stmt.setString(1, status);
				stmt.setString(2, itemId);
				stmt.setString(3, userId);
				stmt.executeUpdate();
				message = "operation successfull edited item id : " + itemId;
				LOGGER.warning(message);
				Code = 56;
				Id = check;
				res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);

				try {
					AwsSESEmail newE = new AwsSESEmail();
					// ownerId= im.getUserId();
					newE.send(userId, FlsSendMail.Fls_Enum.FLS_MAIL_REJECT_REQUEST_TO, rm);
				} catch (Exception e) {
					e.printStackTrace();
				}
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
		check = null;
		LOGGER.info("Inside GetNext method");
		String sql = "SELECT * FROM requests WHERE request_item_id > ? ORDER BY request_item_id LIMIT 1"; //

		getConnection();
		try {
			LOGGER.info("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getNext query...");
			stmt.setString(1, token);

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getString("request_item_id"));
				json.put("userId", rs.getString("request_requser_id"));
				json.put("date", rs.getString("request_date"));

				message = json.toString();
				LOGGER.info(message);
				check = rs.getString("request_item_id");
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
		}
	}

	private void getPrevious() {
		check = null;
		LOGGER.info("Inside GetPrevious method");
		String sql = "SELECT * FROM requests WHERE request_item_id < ? ORDER BY request_item_id DESC LIMIT 1"; //

		getConnection();
		try {
			LOGGER.info("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getPrevious query...");
			stmt.setString(1, token);

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getString("request_item_id"));
				json.put("userId", rs.getString("request_requser_id"));
				json.put("date", rs.getString("request_date"));

				message = json.toString();
				LOGGER.info(message);
				check = rs.getString("request_item_id");
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
		}
	}

	private void getNextR() {
		check = null;
		int t = Integer.parseInt(token);
		LOGGER.info("Inside GetNextR method");
		String sql = "SELECT tb1.*, tb2.user_full_name FROM requests tb1 INNER JOIN users tb2 ON tb1.request_requser_id = tb2.user_id WHERE request_id > ? AND request_status=? ORDER BY request_id LIMIT 1"; //

		getConnection();
		try {
			LOGGER.info("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getNext query...");
			stmt.setInt(1, t);
			stmt.setString(2, "Active");

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getString("request_item_id"));
				json.put("userId", rs.getString("request_requser_id"));
				json.put("date", rs.getString("request_date"));
				json.put("requser_name", rs.getString("user_full_name"));

				message = json.toString();
				LOGGER.info(message);
				check = String.valueOf(rs.getInt("request_id"));
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
		}
	}

	private void getPreviousR() {
		check = null;
		int t = Integer.parseInt(token);
		LOGGER.info("Inside GetPrevious method");
		String sql = "SELECT * FROM requests WHERE request_id < ? AND request_status=? ORDER BY request_id DESC LIMIT 1"; //

		getConnection();
		try {
			LOGGER.info("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getPrevious query...");
			stmt.setInt(1, t);
			stmt.setString(2, "Active");

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getString("request_item_id"));
				json.put("userId", rs.getString("request_requser_id"));
				json.put("date", rs.getString("request_date"));

				message = json.toString();
				LOGGER.info(message);
				check = String.valueOf(rs.getInt("request_id"));
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
		}
	}

}
