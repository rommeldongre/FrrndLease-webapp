package tableOps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import pojos.FriendsModel;
import adminOps.Response;
import connect.Connect;
import util.Event;
import util.Event.Event_Type;
import util.Event.Notification_Type;
import util.FlsConfig;
import util.FlsLogger;
import util.LogCredit;

public class Friends extends Connect {

	private FlsLogger LOGGER = new FlsLogger(Friends.class.getName());

	private String URL = FlsConfig.prefixUrl;
	
	private String check = null, Id = null, token, friendId, fullName, mobile, userId, operation, message;
	private int Code;
	private FriendsModel fm;
	private Response res = new Response();

	public Response selectOp(String Operation, FriendsModel fdm, JSONObject obj) {
		operation = Operation.toLowerCase();
		fm = fdm;

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
			
		case "reinvite":
			LOGGER.info("Reinvite op is selected..");
			Reinvite();
			break;

		default:
			res.setData(FLS_INVALID_OPERATION, "0", FLS_INVALID_OPERATION_M);
			break;
		}

		return res;
	}

	private void Add() {
		friendId = fm.getFriendId();
		fullName = fm.getFullName();
		mobile = fm.getMobile();
		userId = fm.getUserId();

		PreparedStatement stmt1 = null, stmt2 = null, stmt3 = null, stmt4 = null;
		ResultSet rs1 = null, rs2 = null;
		Connection hcp = getConnectionFromPool();

		String source = "@api";
		
		String status = "pending";
		
		try {
			LOGGER.info("Checking if friend is signed up");
			String sqlCheckUser = "SELECT user_id,user_full_name FROM users WHERE user_id=?";
			stmt1 = hcp.prepareStatement(sqlCheckUser);
			stmt1.setString(1, friendId);
			rs1 = stmt1.executeQuery();
			
			if(rs1.next()){
				status = "signedup";
				fullName = rs1.getString("user_full_name");
			}
			
			LOGGER.info("Creating Select statement from friends table");
			String sqlSelectFriends = "SELECT * FROM friends WHERE friend_user_id=? AND (friend_id=? OR friend_fb_id=?)";
			stmt2 = hcp.prepareStatement(sqlSelectFriends);
			stmt2.setString(1, userId);
			stmt2.setString(2, friendId);
			stmt2.setString(3, friendId);
			rs2 = stmt2.executeQuery();
			
			while (rs2.next()) {
				check = rs2.getString("friend_id");
				LOGGER.info("Printing check val: "+check+" "+rs2.getString("friend_full_name"));
			}
			
			if (check == null  & !userId.equals(friendId)) {
				LOGGER.info("Creating statement.....");
				String sqlAddFriends = "insert into friends (friend_id,friend_full_name,friend_mobile,friend_user_id,friend_status) values (?,?,?,?,?)";
				stmt3 = hcp.prepareStatement(sqlAddFriends);

				LOGGER.info("Statement created. Executing query.....");
				stmt3.setString(1, friendId);
				stmt3.setString(2, fullName);
				stmt3.setString(3, mobile);
				stmt3.setString(4, userId);
				stmt3.setString(5, status);
				stmt3.executeUpdate();

				Event event = new Event();
				event.createEvent(friendId, userId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_ADD_FRIEND_FROM, 0, "You have added <a href=\"" + URL + "/myapp.html#/myfriendslist\">" + friendId + "</a> to your Friend List. You can now lease items to each other.");
				event.createEvent(userId, friendId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_ADD_FRIEND_TO, 0, "You are now in <a href=\"" + URL + "/myapp.html#/myfriendslist\">" + userId + "</a>\'s Friend List. You can now lease items to each other");
				
				message = "Entry added into friends table";
				LOGGER.warning(message);
				Code = 10;
				Id = friendId;
				res.setData(FLS_SUCCESS, Id, message);
				
				// to add credit in user_credit
				String sqlAddCredit = "UPDATE users SET user_credit=user_credit+1 WHERE user_id=?";
				stmt4 = hcp.prepareStatement(sqlAddCredit);
				stmt4.setString(1, userId);
				stmt4.executeUpdate();
				
				LogCredit lc = new LogCredit();
				lc.addLogCredit(userId,1,"Friend Added","");
				
				if (friendId.contains("@fb") || friendId.contains("@google")){
//					Event event = new Event();
//					event.createEvent(friendId, userId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_ADD_FRIEND_FROM, 0, "You have added <a href=\"myapp.html#/myfriendslist\">" + friendId + "</a> to your Friend List. You can now lease items to each other.");
				}

			} else {
				LOGGER.warning("Friend Already exists.....");
				res.setData(FLS_SUCCESS, check, FLS_SUCCESS_M);
			}
		} catch (SQLException e) {
			LOGGER.warning("Couldn't create statement");
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs1 != null)rs1.close();
				if(rs2 != null)rs2.close();
				if(stmt4 != null)stmt4.close();
				if(stmt3 != null)stmt3.close();
				if(stmt2 != null)stmt2.close();
				if(stmt1 != null)stmt1.close();
				if(hcp != null)hcp.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void Delete() {
		friendId = fm.getFriendId();
		userId = fm.getUserId();
		check = null;
		LOGGER.info("Inside delete method....");

		PreparedStatement stmt2 = null, stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		String sql = "DELETE FROM friends WHERE friend_id=? AND friend_user_id=?"; //
		String sql2 = "SELECT * FROM friends WHERE friend_id=? AND friend_user_id=?"; //

		try {
			LOGGER.info("Creating statement...");

			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setString(1, userId);
			stmt2.setString(2, friendId);
			rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getString("friend_id");
			}

			if (check != null) {
				stmt = hcp.prepareStatement(sql);

				LOGGER.info("Statement created. Executing delete query on ..." + check);
				stmt.setString(1, userId);
				stmt.setString(2, friendId);
				stmt.executeUpdate();
				message = "operation successfull deleted friend id : " + friendId;
				LOGGER.warning(message);
				Code = 11;
				Id = check;
				res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);

				try {
					Event event = new Event();
					event.createEvent(userId, friendId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_DELETE_FRIEND_FROM, 0, "You have now removed <a href=\"" + URL + "/myapp.html#/myfriendslist\">" + userId + "</a> from your Friend List. You can no longer lease items to each other. Tell us what went wrong!");
					event.createEvent(friendId, userId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_DELETE_FRIEND_TO, 0, "You have been removed from the Friend List of your Friend <a href=\"" + URL + "/myapp.html#/myfriendslist\">" + friendId + "</a>. You can no longer lease items to each other. Tell us what went wrong!");
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
				if(rs!=null) rs.close();
				if(stmt!=null) stmt.close();
				if(stmt2!=null) stmt2.close();
				if(hcp!=null) hcp.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void Edit() {
		friendId = fm.getFriendId();
		fullName = fm.getFullName();
		mobile = fm.getMobile();
		userId = fm.getUserId();
		check = null;

		PreparedStatement stmt2 = null, stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		LOGGER.info("inside edit method");
		String sql = "UPDATE friends SET friend_full_name=?, friend_mobile=? WHERE friend_id=? AND friend_user_id=?"; //
		String sql2 = "SELECT * FROM friends WHERE friend_id=? AND friend_user_id=?"; //

		try {
			LOGGER.info("Creating Statement....");
			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setString(1, userId);
			stmt2.setString(2, friendId);
			rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getString("friend_id");
			}

			if (check != null) {
				stmt = hcp.prepareStatement(sql);

				LOGGER.info("Statement created. Executing edit query on ..." + check);
				stmt.setString(1, fullName);
				stmt.setString(2, mobile);
				stmt.setString(3, userId);
				stmt.setString(4, friendId);
				stmt.executeUpdate();
				message = "operation successfull edited friends id : " + friendId;
				LOGGER.warning(message);
				Code = 12;
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
				if(rs!=null) rs.close();
				if(stmt!=null) stmt.close();
				if(stmt2!=null) stmt2.close();
				if(hcp!=null) hcp.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getNext() {
		check = null;
		Id = fm.getFriendId();
		LOGGER.info("Inside GetNext method");
		String sql = "SELECT * FROM friends WHERE friend_user_id = ? AND friend_id>? ORDER BY friend_id LIMIT 1"; //
		String getPicturesql = "SELECT user_profile_picture FROM users WHERE user_id = ?"; //
		
		PreparedStatement stmt = null,stmt1=null;
		ResultSet rs = null,rs1=null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating a statement .....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getNext query...");
			stmt.setString(1, Id);
			stmt.setString(2, token);

			rs = stmt.executeQuery();
			while (rs.next()) {
				LOGGER.info("Creating 2nd Select statement to fetch profile picture .....");
				stmt1 = hcp.prepareStatement(getPicturesql);

				LOGGER.info("Statement created. Executing getNext query...");
				stmt1.setString(1, rs.getString("friend_id"));
				rs1 = stmt1.executeQuery();
				
				JSONObject json = new JSONObject();
				json.put("friendId", rs.getString("friend_id"));
				json.put("fullName", rs.getString("friend_full_name"));
				json.put("mobile", rs.getString("friend_mobile"));
				json.put("userId", rs.getString("friend_user_id"));
				json.put("status", rs.getString("friend_status"));
				while (rs1.next()) {
				json.put("friendPicture", rs1.getString("user_profile_picture"));
				}
				message = json.toString();
				LOGGER.info(message);
				check = rs.getString("friend_id");
				
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
				if(rs!=null) rs.close();
				if(stmt!=null) stmt.close();
				if(hcp!=null) hcp.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getPrevious() {
		check = null;
		Id = fm.getFriendId();
		LOGGER.info("Inside GetPrevious method");
		String sql = "SELECT * FROM friends WHERE friend_id = ? AND friend_user_id<? ORDER BY friend_user_id DESC LIMIT 1"; //

		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating a statement .....");
			stmt = hcp.prepareStatement(sql);
			stmt.setString(1, Id);
			stmt.setString(2, token);

			rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("friendId", rs.getString("friend_id"));
				json.put("fullName", rs.getString("friend_full_name"));
				json.put("mobile", rs.getString("friend_mobile"));
				json.put("userId", rs.getString("friend_user_id"));

				message = json.toString();
				LOGGER.info(message);
				check = rs.getString("friend_id");
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
				if(rs!=null) rs.close();
				if(stmt!=null) stmt.close();
				if(hcp!=null) hcp.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void Reinvite() {
		
		friendId = fm.getFriendId();
		fullName = fm.getFullName();
		mobile = fm.getMobile();
		userId = fm.getUserId();
		
		try {
			
			Event event = new Event();
			event.createEvent(friendId, userId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_ADD_FRIEND_FROM, 0, "You have added <a href=\"" + URL + "/myapp.html#/myfriendslist\">" + friendId + "</a> to your Friend List. You can now lease items to each other.");
			event.createEvent(userId, friendId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_ADD_FRIEND_TO, 0, "You are now in <a href=\"" + URL + "/myapp.html#/myfriendslist\">" + userId + "</a>\'s Friend List. You can now lease items to each other");
			
			message = "Reinvite to friend Sent";
			LOGGER.warning(message);
			Id = friendId;
			res.setData(FLS_SUCCESS, Id, message);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		
	}
}