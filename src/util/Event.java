package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import connect.Connect;
import pojos.GetNotificationsListResObj;
import pojos.GetNotificationsResObj;
import pojos.GetUnreadEventsCountResObj;

public class Event extends Connect{
	
	FlsLogger LOGGER = new FlsLogger(Event.class.getName());
	
	String URL = FlsConfig.prefixUrl;
	
	public enum Notification_Type {
		FLS_MAIL_REGISTER,
		FLS_MAIL_SIGNUP_VALIDATION,
		FLS_MAIL_POST_ITEM,
		FLS_MAIL_MATCH_WISHLIST_ITEM,
		FLS_MAIL_MATCH_POST_ITEM,
		FLS_MAIL_DELETE_ITEM,
		FLS_MAIL_MAKE_REQUEST_FROM,
		FLS_MAIL_MAKE_REQUEST_TO,
		FLS_MAIL_GRANT_REQUEST_FROM,
		FLS_MAIL_GRANT_REQUEST_TO,
		FLS_MAIL_REJECT_REQUEST_FROM,
		FLS_MAIL_REJECT_REQUEST_TO,
		FLS_MAIL_DELETE_REQUEST_FROM,
		FLS_MAIL_DELETE_REQUEST_TO,
		FLS_MAIL_ADD_FRIEND_FROM,
		FLS_MAIL_ADD_FRIEND_TO,
		FLS_MAIL_DELETE_FRIEND_FROM,
		FLS_MAIL_DELETE_FRIEND_TO,
		FLS_MAIL_GRANT_LEASE_FROM,
		FLS_MAIL_GRANT_LEASE_TO,
		FLS_MAIL_REJECT_LEASE_FROM,
		FLS_MAIL_REJECT_LEASE_TO,
		FLS_MAIL_FORGOT_PASSWORD,
		FLS_MAIL_GRACE_PERIOD_OWNER,
		FLS_MAIL_GRACE_PERIOD_REQUESTOR,
		FLS_MAIL_RENEW_LEASE_OWNER,
		FLS_MAIL_RENEW_LEASE_REQUESTOR,
		FLS_NOMAIL_ADD_WISH_ITEM,
		FLS_SMS_SIGNUP_VALIDATION
	}
	
	public enum Event_Type {
		FLS_EVENT_NOT_NOTIFICATION,
		FLS_EVENT_NOTIFICATION,
		FLS_EVENT_CHAT
	}
	
	public enum Read_Status {
		FLS_READ,
		FLS_UNREAD
	}
	
	public enum Delivery_Status {
		FLS_DELIVERED,
		FLS_UNDELIVERED
	}
	
	public enum Archived {
		FLS_ACTIVE,
		FLS_ARCHIVED
	}
	
	public enum Icon_Type {
		FLS_DEFAULT,
		FLS_ITEM,
		FLS_USER,
		FLS_TIME,
		FLS_POSITIVE,
		FLS_NEGATIVE
	}
	
	public enum User_Notification {
		EMAIL,
		SMS,
		BOTH,
		NONE
	}
	
	public void createEvent(String fromUserId, String toUserId, Event_Type eventType, Notification_Type notificationType, int itemId, String message){
		
		PreparedStatement ps = null;
		Connection hcp = getConnectionFromPool();
		
		try{
			
			String sqlCreateEvent = "INSERT INTO events (from_user_id,to_user_id,event_type,notification_type,item_id,message) VALUES (?,?,?,?,?,?)";
			ps = hcp.prepareStatement(sqlCreateEvent);
			ps.setString(1, fromUserId);
			ps.setString(2, toUserId);
			ps.setString(3, eventType.name());
			ps.setString(4, notificationType.name());
			ps.setInt(5, itemId);
			ps.setString(6, message);
			ps.executeUpdate();
			
		}catch(SQLException e){
			LOGGER.warning(e.getMessage());
			e.printStackTrace();
		}catch(NullPointerException e){
			LOGGER.warning(e.getMessage());
			e.printStackTrace();
		}finally{
			try {
				if(ps != null)ps.close();
				if(hcp != null)hcp.close();
				
				// Grab the Scheduler instance from the Factory
				Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
				JobKey jobKey = JobKey.jobKey("FlsEmailJob", "FlsEmailGroup");
				scheduler.triggerJob(jobKey);
				
			} catch(SchedulerException e){
				LOGGER.warning("not able to get scheduler");
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public int changeReadStatus(int eventId, Read_Status readStatus){
		
		PreparedStatement ps = null;
		Connection hcp = getConnectionFromPool();
		
		try{
			
			hcp.setAutoCommit(false);
			
			String sqlChangeReadStatus = "UPDATE events SET read_status=? WHERE event_id=?";
			ps = hcp.prepareStatement(sqlChangeReadStatus);
			ps.setString(1, readStatus.name());
			ps.setInt(2, eventId);
			
			int count = ps.executeUpdate();
			
			if(count == 1)
				hcp.commit();
			else
				hcp.rollback();
			
			return count;
			
		}catch(SQLException e){
			LOGGER.warning(e.getMessage());
			e.printStackTrace();
		}catch(NullPointerException e){
			LOGGER.warning(e.getMessage());
			e.printStackTrace();
		}finally{
			try {
				if(ps != null)ps.close();
				if(hcp != null)hcp.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return 0;
		
	}
	
public int changeDeliveryStatus(int eventId, Delivery_Status deliveryStatus){
		
		PreparedStatement ps = null;
		Connection hcp = getConnectionFromPool();
		
		try{
			
			hcp.setAutoCommit(false);
			
			String sqlChangeReadStatus = "UPDATE events SET delivery_status=? WHERE event_id=?";
			ps = hcp.prepareStatement(sqlChangeReadStatus);
			ps.setString(1, deliveryStatus.name());
			ps.setInt(2, eventId);
			
			int count = ps.executeUpdate();
			
			if(count == 1)
				hcp.commit();
			else
				hcp.rollback();
			
			return count;
			
		}catch(SQLException e){
			LOGGER.warning(e.getMessage());
			e.printStackTrace();
		}catch(NullPointerException e){
			LOGGER.warning(e.getMessage());
			e.printStackTrace();
		}finally{
			try {
				if(ps != null)ps.close();
				if(hcp != null)hcp.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return 0;
		
	}

public GetNotificationsListResObj getNotifications(String userId, int limit, int offset) {
	
	PreparedStatement ps = null;
	ResultSet rs = null;
	Connection hcp = getConnectionFromPool();
	
	GetNotificationsListResObj response = new GetNotificationsListResObj();
	
	try{
		String sqlGetNotifications = "SELECT tb1.*, tb2.user_profile_picture, tb2.user_full_name, tb3.item_uid FROM events tb1 LEFT JOIN users tb2 ON tb1.from_user_id=tb2.user_id LEFT JOIN items tb3 ON tb1.item_id=tb3.item_id WHERE to_user_id=? AND event_type IN (?,?) ORDER BY event_id DESC LIMIT ?,?";
		ps = hcp.prepareStatement(sqlGetNotifications);
		ps.setString(1, userId);
		ps.setString(2, Event_Type.FLS_EVENT_NOTIFICATION.name());
		ps.setString(3, Event_Type.FLS_EVENT_CHAT.name());
		ps.setInt(4, offset);
		ps.setInt(5, limit);
		
		rs = ps.executeQuery();
		
		if(rs.isBeforeFirst()){
			while(rs.next()){
				GetNotificationsResObj res = new GetNotificationsResObj();
				res.setEventId(rs.getInt("event_id"));
				res.setDatetime(rs.getString("datetime"));
				res.setFromUserId(rs.getString("from_user_id"));
				res.setToUserId(rs.getString("to_user_id"));
				res.setProfilePic(rs.getString("user_profile_picture"));
				res.setUid(rs.getString("item_uid"));
				res.setFullName(rs.getString("user_full_name"));
				res.setReadStatus(rs.getString("read_status"));
				res.setItemId(rs.getInt("item_id"));
				res.setNotificationMsg(rs.getString("message"));
				res.setNotificationType(rs.getString("notification_type"));
				response.addResList(res);
				offset = offset + 1;
			}
			response.setOffset(offset);
			response.setCode(FLS_SUCCESS);
			response.setMessage(FLS_SUCCESS_M);
		} else {
			response.setCode(FLS_END_OF_DB);
			response.setMessage(FLS_END_OF_DB_M);
			LOGGER.warning(FLS_END_OF_DB_M);
		}
		
	}catch(SQLException e){
		response.setCode(FLS_SQL_EXCEPTION);
		response.setMessage(FLS_SQL_EXCEPTION_M);
		LOGGER.warning(e.getMessage());
		e.printStackTrace();
	}catch(NullPointerException e){
		response.setCode(FLS_NULL_POINT);
		response.setMessage(FLS_NULL_POINT_M);
		LOGGER.warning(e.getMessage());
		e.printStackTrace();
	}finally{
		try {
			if(rs != null)rs.close();
			if(ps != null)ps.close();
			if(hcp != null)hcp.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	return response;
}

public GetUnreadEventsCountResObj getUnreadEventsCount(String userId) {
	
	GetUnreadEventsCountResObj response = new GetUnreadEventsCountResObj();
	
	PreparedStatement ps = null;
	ResultSet rs = null;
	Connection hcp = getConnectionFromPool();
	
	try{
		String sqlGetUnreadEventsCount = "SELECT count(*) FROM events WHERE to_user_id=? AND read_status=? AND event_type IN (?,?)";
		ps = hcp.prepareStatement(sqlGetUnreadEventsCount);
		ps.setString(1, userId);
		ps.setString(2, Read_Status.FLS_UNREAD.name());
		ps.setString(3, Event_Type.FLS_EVENT_NOTIFICATION.name());
		ps.setString(4, Event_Type.FLS_EVENT_CHAT.name());
		
		rs = ps.executeQuery();
		
		if(rs.next()){
			response.setUnreadCount((int)rs.getLong(1));
			response.setCode(FLS_SUCCESS);
			response.setMessage(FLS_SUCCESS_M);
		}else{
			response.setCode(FLS_END_OF_DB);
			response.setMessage(FLS_END_OF_DB_M);
		}
	}catch(SQLException e){
		response.setCode(FLS_SQL_EXCEPTION);
		response.setMessage(FLS_SQL_EXCEPTION_M);
		e.printStackTrace();
	}catch(NullPointerException e){
		response.setCode(FLS_NULL_POINT);
		response.setMessage(FLS_NULL_POINT_M);
		e.printStackTrace();
	}finally{
		try {
			if(rs != null)rs.close();
			if(ps != null)ps.close();
			if(hcp != null)hcp.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	return response;
}

public int getNextUndeliveredEvent(){
	
	PreparedStatement ps = null;
	ResultSet rs = null;
	Connection hcp = getConnectionFromPool();
	
	try{
		String sqlGetUndeliveredEvent = "SELECT event_id FROM events WHERE delivery_status=? ORDER BY event_id ASC LIMIT 1";
		ps = hcp.prepareStatement(sqlGetUndeliveredEvent);
		ps.setString(1,Delivery_Status.FLS_UNDELIVERED.name());
		
		rs = ps.executeQuery();
		
		if(rs.next()){
			return rs.getInt("event_id");
		}else{
			return -1;
		}
	}catch(SQLException e){
		LOGGER.warning(FLS_SQL_EXCEPTION_M);
		e.printStackTrace();
	}finally{
		try {
			if(rs != null)rs.close();
			if(ps != null)ps.close();
			if(hcp != null)hcp.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	return -1;
	
}

public boolean SendNotifications(int eventId){
	
	PreparedStatement ps = null;
	ResultSet rs = null;
	Connection hcp = getConnectionFromPool();
	
	try{
		String sqlCheckUserNotificationType = "SELECT user_notification FROM events INNER JOIN users ON events.to_user_id = users.user_id WHERE event_id=?";
		ps = hcp.prepareStatement(sqlCheckUserNotificationType);
		ps.setInt(1, eventId);
		
		rs = ps.executeQuery();
		
		if(rs.next()){
			switch(rs.getString("user_notification")){
				case "EMAIL":
					return sendEmail(eventId);
				case "SMS":
					return sendSms(eventId);
				case "BOTH":
					return sendEmail(eventId) || sendSms(eventId);
				case "NONE":
					return true;
			}
		}else{
			return false;
		}
	}catch(SQLException e){
		LOGGER.warning(FLS_SQL_EXCEPTION_M);
		e.printStackTrace();
	}finally{
		try {
			if(rs != null)rs.close();
			if(ps != null)ps.close();
			if(hcp != null)hcp.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	return false;
}

private boolean sendSms(int eventId){
	LOGGER.info("Sms not sent");
	return false;
}

private boolean sendEmail(int eventId){
	
	PreparedStatement ps = null;
	ResultSet rs = null;
	Connection hcp = getConnectionFromPool();

	JSONObject obj = new JSONObject();
	
	try{
		
		String sqlGetAllData = "SELECT tb1.event_id, tb1.datetime, tb1.notification_type, tb1.message, tb2.item_id, tb2.item_name, tb2.item_category, tb2.item_desc, tb2.item_user_id, tb2.item_lease_value, tb2.item_lease_term, tb2.item_image, tb2.item_uid, tb2.item_status, tb3.user_id, tb3.user_full_name, tb3.user_profile_picture, tb3.user_activation, tb4.user_id AS senders_user_id, tb4.user_full_name AS senders_full_name, tb4.user_profile_picture AS senders_profile_pic, tb4.user_referral_code AS senders_refferal_code FROM events tb1 LEFT JOIN items tb2 ON tb1.item_id=tb2.item_id LEFT JOIN users tb3 ON tb1.to_user_id=tb3.user_id LEFT JOIN users tb4 ON tb1.from_user_id=tb4.user_id WHERE event_id=?";
		ps = hcp.prepareStatement(sqlGetAllData);
		ps.setInt(1, eventId);
		
		rs = ps.executeQuery();
		
		if(rs.next()){
			// Senders Data
			obj.put("fromUserId", rs.getString("senders_user_id"));
			obj.put("fromFullName", rs.getString("senders_full_name"));
			obj.put("fromProfilePic", rs.getString("senders_profile_pic"));
			obj.put("fromUserRefferalCode", rs.getString("senders_refferal_code"));
			
			// Receivers Data
			obj.put("toUserId", rs.getString("user_id"));
			obj.put("toUserName", rs.getString("user_full_name"));
			obj.put("toProfilePic", rs.getString("user_profile_picture"));
			obj.put("toUserActivation", rs.getString("user_activation"));
			
			// Items Data
			obj.put("itemId", rs.getInt("item_id"));
			obj.put("title", rs.getString("item_name"));
			obj.put("category", rs.getString("item_category"));
			if(rs.getString("item_desc") == null || rs.getString("item_desc").equals(""))
				obj.put("description", "");
			else
				obj.put("description", rs.getString("item_desc"));
			obj.put("itemUserId", rs.getString("item_user_id"));
			obj.put("leaseValue", rs.getInt("item_lease_value"));
			obj.put("leaseTerm", rs.getString("item_lease_term"));
			if(rs.getString("item_image") == null || rs.getString("item_image").equals(""))
				obj.put("image", "");
			else
				obj.put("image", rs.getString("item_image"));
			obj.put("uid", rs.getString("item_uid"));
			obj.put("itemStatus", rs.getString("item_status"));
			
			// Events Data
			obj.put("eventId", rs.getInt("event_id"));
			obj.put("datetime", rs.getString("datetime"));
			obj.put("notificationType", rs.getString("notification_type"));
			obj.put("message", rs.getString("message"));
			
			
			if(Notification_Type.valueOf(obj.getString("notificationType")).equals(Notification_Type.FLS_NOMAIL_ADD_WISH_ITEM)){
				
				List<JSONObject> listItems = new ArrayList<>();
				
				String itemLinks = "";
				
				MatchItems matchItems = new MatchItems();
				listItems = matchItems.checkPostedItems(obj.getInt("itemId"));
				
				if(!listItems.isEmpty()){
					try {
						for(JSONObject p : listItems){
							itemLinks = itemLinks + " <u><a href=\"" + URL + "/ItemDetails?uid=" + p.getString("uid") + "\">" + p.getString("title") + "</a></u>";
						}
						
						Event event = new Event();
						event.createEvent(obj.getString("toUserId"), obj.getString("toUserId"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_MATCH_POST_ITEM, obj.getInt("itemId"), "Some items in the store" + itemLinks + " match your wished item <strong>'" + obj.getString("title") + "'</strong>");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				return true;
			}else{
				if(Notification_Type.valueOf(obj.getString("notificationType")).equals(Notification_Type.FLS_MAIL_POST_ITEM)){
					try{
						// checking the wish list if this posted item matches someone's requirements
						MatchItems matchItems = new MatchItems();
						matchItems.checkWishlist(obj.getString("title"), obj.getString("itemUserId"), obj.getString("uid"), obj.getInt("itemId"));
					}catch(Exception e){
						LOGGER.warning(e.getMessage());
					}
				}
				FlsEmail email = new FlsEmail();
				return email.sendEmail(obj, Notification_Type.valueOf(obj.getString("notificationType")));
			}
			
			
		}else{
			return false;
		}
		
	}catch(SQLException e){
		LOGGER.warning(FLS_SQL_EXCEPTION_M);
		e.printStackTrace();
	} catch (JSONException e) {
		LOGGER.warning(FLS_JSON_EXCEPTION_M);
		e.printStackTrace();
	}finally{
		try {
			if(rs != null)rs.close();
			if(ps != null)ps.close();
			if(hcp != null)hcp.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	return false;
}

}