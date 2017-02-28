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
import util.FlsCredit.Credit;
import util.FlsConfig;
import util.FlsCredit;
import util.FlsLogger;

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

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null, ps3 = null;
		ResultSet rs1 = null, rs2 = null;
		int rs3;
		
		String status = "pending";
		
		try {
			LOGGER.info("Checking if this friend user has signed up");
			String sqlCheckUser = "SELECT user_id,user_full_name FROM users WHERE user_id=?";
			ps1 = hcp.prepareStatement(sqlCheckUser);
			ps1.setString(1, friendId);
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				status = "signedup";
				LOGGER.info("This user has already signed up");
				fullName = rs1.getString("user_full_name");
			}else{
				LOGGER.info("This user has not signed up");
			}
			
			LOGGER.info("Checking this friendship...");
			String sqlSelectFriends = "SELECT * FROM friends WHERE friend_user_id=? AND (friend_id=? OR friend_fb_id=?)";
			ps2 = hcp.prepareStatement(sqlSelectFriends);
			ps2.setString(1, userId);
			ps2.setString(2, friendId);
			ps2.setString(3, friendId);
			rs2 = ps2.executeQuery();
			
			if(!rs2.next()){
				LOGGER.info("This is a new friendship.");
				String sqlAddFriends = "insert into friends (friend_id,friend_full_name,friend_mobile,friend_user_id,friend_status,friend_fb_id) values (?,?,?,?,?,?)";
				ps3 = hcp.prepareStatement(sqlAddFriends);

				LOGGER.info("Statement created. Executing query.....");
				ps3.setString(1, friendId);
				ps3.setString(2, fullName);
				ps3.setString(3, mobile);
				ps3.setString(4, userId);
				ps3.setString(5, status);
				if(friendId.contains("@fb")){
					ps3.setString(6, friendId);
				}else{
					ps3.setString(6, null);
				}
				
				rs3 = ps3.executeUpdate();
				
				if(rs3 == 1){
					res.setData(FLS_SUCCESS, Id, FLS_ADD_FRIEND);
					LOGGER.info("Congratulations new friendship created!!");
					Event event = new Event();
					
					if(fullName.contains("-")){
						event.createEvent(friendId, userId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_ADD_FRIEND_FROM, 0, "You have added <a href=\"" + URL + "/myapp.html#/myfriendslist\">" + friendId + "</a> to your Friend List. You can now lease items to each other.");
					}else{
						if(friendId.contains("@fb")){
							event.createEvent(getUserId(friendId), userId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_ADD_FRIEND_FROM_NAME, 0, "Great start! You have added '<a href=\"" + URL + "/myapp.html#/myfriendslist\">" + fullName + "</a> ' to your Friend List. Once he/she responds, you will be able to lease items to each other with discounted credits!");
						}else{
							event.createEvent(friendId, userId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_ADD_FRIEND_FROM_NAME, 0, "Great start! You have added '<a href=\"" + URL + "/myapp.html#/myfriendslist\">" + fullName + "</a> ' to your Friend List. Once he/she responds, you will be able to lease items to each other with discounted credits!");
						}
					}
					event.createEvent(userId, friendId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_ADD_FRIEND_TO, 0, "You are now in <a href=\"" + URL + "/myapp.html#/myfriendslist\">" + userId + "</a>\'s Friend List. You can now lease items to each other");
				
					FlsCredit credits = new FlsCredit();
					credits.logCredit(userId, 1, "Friend Added", "", Credit.ADD);
				}else{
					res.setData(FLS_INVALID_OPERATION, Id, FLS_FRIEND_NOT_ADDED);
					LOGGER.info("Not able to create a new friendship.");
				}

				
			} else if(userId.equals(friendId)) {
				res.setData(FLS_DUPLICATE_ENTRY, Id, FLS_FRIEND_YOURSELF);
				LOGGER.info("Trying to make yourself friend.");
			} else {
				res.setData(FLS_DUPLICATE_ENTRY, Id, FLS_DUPLICATE_FRIEND_ENTRY);
				LOGGER.info("This friendship already exists.");
			}

		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			res.setData(FLS_INVALID_OPERATION, "0", FLS_INVALID_OPERATION_M);
		} finally {
			try {
				if(rs2 != null) rs2.close();
				if(rs1 != null) rs1.close();
				if(ps3 != null) ps3.close();
				if(ps2 != null) ps2.close();
				if(ps1 != null) ps1.close();
				if(hcp != null)hcp.close();
			} catch (Exception e) {
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
	
	private String getUserId(String fbId){
		String userId=null;
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		try {
			
			LOGGER.info("Select statement for fetching userId based on FB ID .....");
			String sqlUserId = "SELECT user_id FROM `users` WHERE user_fb_id =?";
			ps1 = hcp.prepareStatement(sqlUserId);
			ps1.setString(1, fbId);
			rs1 = ps1.executeQuery();
			
			
			if(rs1.next()){
				userId = rs1.getString("user_id");
			}
			
		} catch(Exception e){
			LOGGER.warning("not able to get userId based on FB ID");
			e.printStackTrace();
		}finally {
			try {
				if(rs1 != null)rs1.close();
				if(ps1 != null)ps1.close();
				if(hcp != null)hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return userId;
	}
}