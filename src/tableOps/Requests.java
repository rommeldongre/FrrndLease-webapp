package tableOps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import adminOps.Response;
import connect.Connect;
import pojos.ItemsModel;
import pojos.RequestsModel;
import util.Event;
import util.Event.Event_Type;
import util.Event.Notification_Type;
import util.FlsCredit.Credit;
import util.FlsBadges;
import util.FlsConfig;
import util.FlsCredit;
import util.FlsLogger;

public class Requests extends Connect {

	private FlsLogger LOGGER = new FlsLogger(Requests.class.getName());

	private String URL = FlsConfig.prefixUrl;
	
	private String check = null, Id = null, token, userId, itemId, operation, message, msg;
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
		msg = rm.getMessage();

		PreparedStatement  ps1 = null, ps2 = null, ps3 = null, ps4 = null, ps5 = null, ps6 = null, ps7 = null;
		ResultSet rs1 = null, rs2 = null, rs3 = null, rs6 = null, rs7 = null;
		int rs4;
		boolean user_verified=false;
		Connection hcp = getConnectionFromPool();

		try {
			
			LOGGER.info("Checking if the owner is Uber Or Not...");
			String sqlCheckingUberOwner = "SELECT (CASE WHEN tb1.user_fee_expiry IS NULL THEN true WHEN tb1.user_fee_expiry < NOW() THEN true ELSE false END) AS expired, tb1.user_id FROM users tb1 INNER JOIN items tb2 ON tb1.user_id=tb2.item_user_id WHERE tb2.item_id=?";
			ps6 = hcp.prepareStatement(sqlCheckingUberOwner);
			ps6.setInt(1, Integer.parseInt(itemId));
			
			rs6 = ps6.executeQuery();
			
			if(rs6.next()){
				if(rs6.getBoolean("expired")){
					String sqlCountOwnerRequests = "SELECT COUNT(*) AS requests FROM `requests` tb1 INNER JOIN `items` tb2 ON tb1.request_item_id=tb2.item_id WHERE tb2.item_user_id=?  AND tb1.request_status=?";
					ps7 = hcp.prepareStatement(sqlCountOwnerRequests);
					ps7.setString(1, rs6.getString("user_id"));
					ps7.setString(2, "Active");
					
					rs7 = ps7.executeQuery();
					
					if(rs7.next()){
						if(rs7.getInt("requests") >= 10){
							LOGGER.warning("Number of requests to the owner : " + rs7.getInt("requests"));
							res.setData(FLS_OWNER_REQUESTS_LIMIT, "0", FLS_OWNER_REQUESTS_LIMIT_M);
							try {
								Event event = new Event();
								event.createEvent(rs6.getString("user_id"), rs6.getString("user_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_OWNER_REQUEST_LIMIT, Integer.parseInt(itemId), "People are not able to request since you have reached the limit for receiving incoming requests. To get unlimited number of requests please switch to Uber Plan.");
							} catch (Exception e) {
								e.printStackTrace();
								res.setData(FLS_DUPLICATE_ENTRY, "0", "Something is wrong with us we'll get back to you ASAP!!");
							}
							return;
						}
					}else{
						LOGGER.warning("Not able to get number of requests to the owner!!");
						res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
						return;
					}
				}
			}else{
				LOGGER.warning("Not able to get owners data!!");
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
				return;
			}

			LOGGER.info("Checking if this request already exists...");
			String sqlCheckingRequest = "SELECT tb1.*,(CASE WHEN tb1.request_item_id=? THEN true ELSE false END) AS itemCheck FROM requests tb1 WHERE tb1.request_requser_id=? AND tb1.request_status=?";
			ps1 = hcp.prepareStatement(sqlCheckingRequest);
			ps1.setString(1, itemId);
			ps1.setString(2, userId);
			ps1.setString(3, "Active");
			
			rs1 = ps1.executeQuery();
			
			int i = 0;
			while(rs1.next()){
				if(rs1.getBoolean("itemCheck")){
					res.setData(FLS_DUPLICATE_ENTRY, "0", FLS_DUPLICATE_ENTRY_M);
					return;
				}else{
					i++;
					if(i >= 3){
						res.setData(FLS_REQUEST_LIMIT, "0", FLS_REQUEST_LIMIT_M);
						return;
					}
				}
			}
			
			LOGGER.info("Getting Item and Item owner data...");
			String sqlSelectItemData = "SELECT tb1.*, tb2.* FROM items tb1 INNER JOIN users tb2 ON tb1.item_user_id=tb2.user_id WHERE item_id=?";
			ps2 = hcp.prepareStatement(sqlSelectItemData);
			ps2.setInt(1, Integer.parseInt(itemId));
			
			rs2 = ps2.executeQuery();
			
			if(!(rs2.next())){
				LOGGER.warning("Not able to get items data from items table!!");
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
				return;
			}

			if(!(rs2.getString("item_status").equals("InStore"))){
				LOGGER.info("Item is not InStore..");
				res.setData(FLS_ITEM_ON_HOLD, "0", FLS_ITEM_ON_HOLD_M);
				return;
			}else{
				LOGGER.info("Getting requestor's data...");
				String sqlRequestorData = "SELECT * FROM users WHERE user_id=?";
				ps3 = hcp.prepareStatement(sqlRequestorData);
				ps3.setString(1, userId);
					
				rs3 = ps3.executeQuery();
			
				if(!(rs3.next())){
					LOGGER.warning("Not able to get requestors data from users table!!");
					res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
					return;
				}
				
				if(!(rs2.getString("user_locality").equals(rs3.getString("user_locality")))){
					LOGGER.warning("Users are not in the same locality");
					res.setData(FLS_UNLIKE_LOCATION, "0", FLS_UNLIKE_LOCATION_M);
					return;
				}
								
				String sqlAddRequest = "INSERT INTO requests (request_requser_id,request_item_id,request_message) values (?,?,?)";
				ps4 = hcp.prepareStatement(sqlAddRequest);
				ps4.setString(1, userId);
				ps4.setString(2, itemId);
				ps4.setString(3, msg);
							
				rs4 = ps4.executeUpdate();
				
				if(rs4 == 1){
					LOGGER.info("Entry added into requests table");
					res.setData(FLS_SUCCESS, "0", "Your request has been sent to the owner!!");
						
					String sqlAddCredit = "UPDATE users SET user_credit=user_credit+1 WHERE user_id=?";
					ps5 = hcp.prepareStatement(sqlAddCredit);
					ps5.setString(1, userId);
									
					ps5.executeUpdate();
					
					FlsCredit credits = new FlsCredit();
					credits.logCredit(userId, 1, "Item Requested", "", Credit.ADD);
									
					try {
						String ownerUserId;
						ownerUserId = rs2.getString("item_user_id");
						Event event = new Event();
						event.createEvent(ownerUserId, userId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_MAKE_REQUEST_FROM, Integer.parseInt(itemId), "You have sucessfully Requested the item <a href=\"" + URL + "/ItemDetails?uid=" + rs2.getString("item_uid") + "\">" + rs2.getString("item_name") + "</a> on Friend Lease. The Owner of the item will respond within a week!");
						if(msg.equals(null)){
							event.createEvent(userId, ownerUserId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_MAKE_REQUEST_TO, Integer.parseInt(itemId), "Your Item <a href=\"" + URL + "/ItemDetails?uid=" + rs2.getString("item_uid") + "\">" + rs2.getString("item_name") + "</a> has been requested on Friend Lease. Check out <a href=\"" + URL + "/myapp.html#/myincomingrequests\">Your Incoming Requests</a>");
						}else{
							event.createEvent(userId, ownerUserId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_MAKE_REQUEST_TO, Integer.parseInt(itemId), "Your Item <a href=\"" + URL + "/ItemDetails?uid=" + rs2.getString("item_uid") + "\">" + rs2.getString("item_name") + "</a> has been requested on Friend Lease with a message <i>"+msg+"</i>.Check out <a href=\"" + URL + "/myapp.html#/myincomingrequests\">Your Incoming Requests</a>");
						}
						
					} catch (Exception e) {
						e.printStackTrace();
						res.setData(FLS_DUPLICATE_ENTRY, "0", "Something is wrong with us we'll get back to you ASAP!!");
					}
									
				}else{
					LOGGER.info("Entry not added into requests table");
					res.setData(FLS_DUPLICATE_ENTRY, "0", "Something is wrong with us we'll get back to you ASAP!!");
				}
				
			}
			
		}catch(Exception e){
			LOGGER.warning("Couldn't create statement");
			res.setData(FLS_SQL_EXCEPTION, "0", "Something is wrong with us we'll get back to you ASAP!!");
			e.printStackTrace();
	    }finally{
			try {
				if(rs3 != null)	rs3.close();
				if(rs2 != null) rs2.close();
				if(rs1 != null) rs1.close();
				if(ps5 != null)	ps5.close();
				if(ps4 != null)	ps4.close();
				if(ps3 != null)	ps3.close();
				if(ps2 != null)	ps2.close();
				if(ps1 != null)	ps1.close();
				if(hcp != null) hcp.close();
			} catch (SQLException e) {
				e.printStackTrace();
				res.setData(FLS_SQL_EXCEPTION, "0", "Something wrong with us we'll get back to you ASAP!!");
			}
		}
	}

	private void Delete() {
		itemId = rm.getItemId();
		check = null;
		LOGGER.info("Inside delete method....");

		PreparedStatement stmt = null,stmt2 = null ;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		String sql = "DELETE FROM requests WHERE request_item_id=?"; //
		String sql2 = "SELECT * FROM requests WHERE request_item_id=?"; //

		try {
			LOGGER.info("Creating statement...");

			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setString(1, itemId);
			rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getString("request_item_id");
			}

			if (check != null) {
				stmt = hcp.prepareStatement(sql);

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
		PreparedStatement stmt = null,stmt2 = null ;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		String sql = "UPDATE requests SET request_status=? WHERE request_item_id=?"; //
		String sql2 = "SELECT * FROM requests WHERE request_item_id=?"; //

		try {
			LOGGER.info("Creating Statement....");
			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setString(1, itemId);
			rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getString("request_item_id");
			}

			if (check != null) {
				stmt = hcp.prepareStatement(sql);

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

	private void EditOne() {
		itemId = rm.getItemId();
		userId = rm.getUserId();
		String status = "Archived";
		check = null;

		LOGGER.info("inside edit method");
		PreparedStatement stmt = null,stmt2 = null, stmt1 = null ;
		ResultSet rs = null, dbResponse = null;
		Connection hcp = getConnectionFromPool();
		String sql = "UPDATE requests SET request_status=? WHERE request_item_id=? AND request_requser_id=?"; //
		String sql2 = "SELECT * FROM requests WHERE request_item_id=? AND request_requser_id=? AND request_status=?"; //

		try {
			LOGGER.info("Creating Statement....");
			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setString(1, itemId);
			stmt2.setString(2, userId);
			stmt2.setString(3, "Active");
			rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getString("request_id");
			}

			if (check != null) {

				// code for populating item pojo for sending requester email
				ItemsModel im = new ItemsModel();
				String sql1 = "SELECT * FROM items WHERE item_id=?";
				LOGGER.info("Creating a statement .....");
				stmt1 = hcp.prepareStatement(sql1);

				LOGGER.info("Statement created. Executing select row query of FLS_MAIL_REJECT_REQUEST_TO...");
				stmt1.setString(1, itemId);

				dbResponse = stmt1.executeQuery();
				LOGGER.info("Query to request pojos fired into requests table");
				if (dbResponse.next()) {

					if (dbResponse.getString("item_id") != null) {
						LOGGER.info("Inside Nested check1 statement of FLS_MAIL_REJECT_REQUEST_TO");

						// Populate the response
						try {

							// Updating users response time
							FlsBadges badges = new FlsBadges(dbResponse.getString("item_user_id"));
							badges.updateRequestResponseTime(rs.getString("request_lastmodified"));
							
							JSONObject obj1 = new JSONObject();
							obj1.put("title", dbResponse.getString("item_name"));
							obj1.put("description", dbResponse.getString("item_desc"));
							obj1.put("category", dbResponse.getString("item_category"));
							obj1.put("userId", dbResponse.getString("item_user_id"));
							obj1.put("uid", dbResponse.getString("item_uid"));
							obj1.put("leaseTerm", dbResponse.getString("item_lease_term"));
							obj1.put("id", dbResponse.getString("item_id"));
							obj1.put("leaseValue", dbResponse.getString("item_lease_value"));
							obj1.put("status", "InStore");
							if(dbResponse.getString("item_primary_image_link") == null || dbResponse.getString("item_primary_image_link").equals("null"))
								obj1.put("primaryImageLink", "");
							else
								obj1.put("primaryImageLink", dbResponse.getString("item_primary_image_link"));

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

				stmt = hcp.prepareStatement(sql);

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
					Event event = new Event();
					event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_REJECT_REQUEST_TO, Integer.parseInt(itemId), "Request of item <a href=\"" + URL + "/ItemDetails?uid=" + im.getUid() + "\">" + im.getTitle() + "</a> has been removed by the owner as a lease might be granted.");
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
		}finally{
			try {
				rs.close();
				dbResponse.close();
				stmt.close();
				stmt1.close();
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
		String sql = "SELECT * FROM requests WHERE request_item_id > ? ORDER BY request_item_id LIMIT 1"; //

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
				json.put("itemId", rs.getString("request_item_id"));
				json.put("userId", rs.getString("request_requser_id"));
				json.put("date", rs.getString("request_lastmodified"));

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
		String sql = "SELECT * FROM requests WHERE request_item_id < ? ORDER BY request_item_id DESC LIMIT 1"; //

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
				json.put("itemId", rs.getString("request_item_id"));
				json.put("userId", rs.getString("request_requser_id"));
				json.put("date", rs.getString("request_lastmodified"));

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

	private void getNextR() {
		check = null;
		int t = Integer.parseInt(token);
		LOGGER.info("Inside GetNextR method");
		String sql = "SELECT tb1.*, tb2.user_full_name FROM requests tb1 INNER JOIN users tb2 ON tb1.request_requser_id = tb2.user_id WHERE request_id > ? AND request_status=? ORDER BY request_id LIMIT 1"; //

		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating a statement .....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getNext query...");
			stmt.setInt(1, t);
			stmt.setString(2, "Active");

			rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getString("request_item_id"));
				json.put("userId", rs.getString("request_requser_id"));
				json.put("date", rs.getString("request_lastmodified"));
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

	private void getPreviousR() {
		check = null;
		int t = Integer.parseInt(token);
		LOGGER.info("Inside GetPrevious method");
		String sql = "SELECT * FROM requests WHERE request_id < ? AND request_status=? ORDER BY request_id DESC LIMIT 1"; //

		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating a statement .....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getPrevious query...");
			stmt.setInt(1, t);
			stmt.setString(2, "Active");

			rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getString("request_item_id"));
				json.put("userId", rs.getString("request_requser_id"));
				json.put("date", rs.getString("request_lastmodified"));

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
