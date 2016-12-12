package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import connect.Connect;
import pojos.GetNotificationsListResObj;
import pojos.GetNotificationsResObj;
import pojos.GetUnreadEventsCountResObj;

public class Event extends Connect{
	
	FlsLogger LOGGER = new FlsLogger(Event.class.getName());
	
	String ENV_CONFIG = FlsConfig.env;
	String URL = FlsConfig.prefixUrl;
	
	private final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
	
	public enum Notification_Type {
		FLS_MAIL_FORGOT_PASSWORD,
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
		FLS_MAIL_GRANT_LEASE_FROM_SELF,
		FLS_MAIL_GRANT_LEASE_TO_SELF,
		FLS_MAIL_GRANT_LEASE_FROM_PRIME,
		FLS_MAIL_GRANT_LEASE_TO_PRIME,
		FLS_MAIL_FROM_LEASE_STARTED,
		FLS_MAIL_TO_LEASE_STARTED,
		FLS_MAIL_OPS_PICKUP_READY,
		FLS_MAIL_CLOSE_LEASE_FROM_SELF,
		FLS_MAIL_CLOSE_LEASE_TO_SELF,
		FLS_MAIL_OPS_PICKUP_CLOSE,
		FLS_MAIL_ITEM_INSTORE_FROM,
		FLS_MAIL_ITEM_INSTORE_TO,
		FLS_MAIL_GRACE_PERIOD_OWNER,
		FLS_MAIL_GRACE_PERIOD_REQUESTOR,
		FLS_MAIL_RENEW_LEASE_OWNER,
		FLS_MAIL_RENEW_LEASE_REQUESTOR,
		FLS_NOMAIL_ADD_WISH_ITEM,
		FLS_SMS_FORGOT_PASSWORD,
		FLS_SMS_SIGNUP_VALIDATION,
		FLS_SMS_REGISTER,
		FLS_EMAIL_VERIFICATION,
		FLS_MOBILE_VERIFICATION,
		FLS_MAIL_MESSAGE_FRIEND_FROM,
		FLS_MAIL_MESSAGE_FRIEND_TO,
		FLS_MAIL_MESSAGE_ITEM_FROM,
		FLS_MAIL_MESSAGE_ITEM_TO,
		FLS_MAIL_OLD_ITEM_WARN,
		FLS_MAIL_OLD_REQUEST_WARN,
		FLS_MAIL_OLD_LEASE_WARN,
		FLS_MAIL_REMIND_PHOTO_ID,
		FLS_MAIL_OPS_ADD_LEAD,
		FLS_MAIL_ADD_LEAD,
		FLS_MAIL_ITEM_ON_HOLD,
		FLS_MAIL_ITEM_INSTORE,
		FLS_MAIL_LEASE_ENDED_OWNER,
		FLS_MAIL_LEASE_ENDED_REQUESTOR,
		FLS_MAIL_LEASE_READY_OWNER,
		FLS_MAIL_LEASE_READY_REQUESTOR
	}
	
	public enum Event_Type {
		FLS_EVENT_NOT_NOTIFICATION, // for no In App notifications but emails and sms go out
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
		FLS_NEGATIVE,
		FLS_MESSAGE_FROM,
		FLS_MESSAGE_TO,
	}
	
	public enum User_Notification {
		EMAIL,
		SMS,
		BOTH,
		NONE
	}
	
	public void createEvent(String fromUserId, String toUserId, Event_Type eventType, Notification_Type notificationType, int itemId, String message){
		
		LOGGER.info("Inside createEvent Method");
		
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
		
		LOGGER.info("Inside changeReadStatus Method");
		
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
			
		LOGGER.info("Inside changeDeliveryStatus Method");
		
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
		
		LOGGER.info("Inside getNotifications Method");
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		
		GetNotificationsListResObj response = new GetNotificationsListResObj();
		
		try{
			String sqlGetNotifications = "SELECT tb1.*, tb2.user_profile_picture, tb2.user_full_name, tb3.item_uid,tb3.item_name FROM events tb1 LEFT JOIN users tb2 ON tb1.from_user_id=tb2.user_id LEFT JOIN items tb3 ON tb1.item_id=tb3.item_id WHERE to_user_id=? AND event_type IN (?,?) AND archived=? ORDER BY event_id DESC LIMIT ?,?";
			ps = hcp.prepareStatement(sqlGetNotifications);
			ps.setString(1, userId);
			ps.setString(2, Event_Type.FLS_EVENT_NOTIFICATION.name());
			ps.setString(3, Event_Type.FLS_EVENT_CHAT.name());
			ps.setString(4, Archived.FLS_ACTIVE.name());
			ps.setInt(5, offset);
			ps.setInt(6, limit);
			
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
					res.setTitle(rs.getString("item_name"));
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
		
		LOGGER.info("Inside getUnreadEventsCount Method");
		
		GetUnreadEventsCountResObj response = new GetUnreadEventsCountResObj();
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		
		try{
			String sqlGetUnreadEventsCount = "SELECT count(*) FROM events WHERE to_user_id=? AND read_status=? AND event_type IN (?,?) AND archived=?";
			ps = hcp.prepareStatement(sqlGetUnreadEventsCount);
			ps.setString(1, userId);
			ps.setString(2, Read_Status.FLS_UNREAD.name());
			ps.setString(3, Event_Type.FLS_EVENT_NOTIFICATION.name());
			ps.setString(4, Event_Type.FLS_EVENT_CHAT.name());
			ps.setString(5, Archived.FLS_ACTIVE.name());
			
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
	
	public int changeUserNotification(String userId, User_Notification userNotification){
		
		LOGGER.info("Inside changeUserNotification Method");
		
		PreparedStatement ps = null;
		Connection hcp = getConnectionFromPool();
		
		try{
			
			hcp.setAutoCommit(false);
			
			String sqlNotification = "UPDATE users SET user_notification=? WHERE user_id=?";
			ps = hcp.prepareStatement(sqlNotification);
			ps.setString(1, userNotification.name());
			ps.setString(2, userId);
			
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
	
	public User_Notification getUserNotification(String userId){
		
		LOGGER.info("Inside GetUserNotification Method");
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		
		try{
			
			String getUserNotification = "SELECT user_notification from users WHERE user_id=?";
			ps = hcp.prepareStatement(getUserNotification);
			ps.setString(1, userId);
			rs = ps.executeQuery();
			
			if(rs.next()){
				return User_Notification.valueOf(rs.getString("user_notification"));
			}else{
				return User_Notification.NONE;
			}
			
		}catch(SQLException e){
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
		
		return User_Notification.NONE;
		
	}
	
	public int getNextUndeliveredEvent(){
		
		LOGGER.info("Inside getNextUndeliveredEvent Method");
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		
		try{
			String sqlGetUndeliveredEvent = "SELECT event_id FROM events WHERE delivery_status=? ORDER BY event_id ASC LIMIT 1";
			ps = hcp.prepareStatement(sqlGetUndeliveredEvent);
			ps.setString(1,Delivery_Status.FLS_UNDELIVERED.name());
			
			rs = ps.executeQuery();
			
			if(rs.next()){
				LOGGER.info("Next Undelivered Event Id : " + rs.getInt("event_id"));
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
		
		LOGGER.info("Inside SendNotifications Method");
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		
		try{
			String sqlNotificationType = "SELECT from_user_id,to_user_id,notification_type FROM events WHERE event_id=?";
			ps = hcp.prepareStatement(sqlNotificationType);
			ps.setInt(1, eventId);
			
			rs = ps.executeQuery();
			
			if(rs.next()){
				switch(Notification_Type.valueOf(rs.getString("notification_type"))){
					case FLS_MAIL_FORGOT_PASSWORD:
					case FLS_MAIL_REGISTER:
					case FLS_MAIL_SIGNUP_VALIDATION:
					case FLS_EMAIL_VERIFICATION:
						return sendEmail(eventId);
					case FLS_SMS_FORGOT_PASSWORD:
					case FLS_SMS_SIGNUP_VALIDATION:
					case FLS_SMS_REGISTER:
					case FLS_MOBILE_VERIFICATION:
						return sendSms(eventId);
					case FLS_MAIL_ADD_FRIEND_TO:
						return sendEmailToFriend(rs.getString("from_user_id"), rs.getString("to_user_id"), eventId);
					case FLS_MAIL_ADD_LEAD:
						return sendEmailToLead(rs.getString("to_user_id"),rs.getString("from_user_id"), eventId);
					default:
						break;
				}
				switch(getUserNotification(rs.getString("to_user_id"))){
					case EMAIL:
						return sendEmail(eventId);
					case SMS:
						return sendSms(eventId);
					case BOTH:
						return sendEmail(eventId) && sendSms(eventId);
					case NONE:
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
		
		LOGGER.info("Sending msg for this event id : " + eventId);

		String ACCOUNT_SID = "ACd0d67cc952f254e5cce8e6912527750a";
		String AUTH_TOKEN = "69d7e761d7361e35fe43afd55acff5ff";
		
		if(ENV_CONFIG.equals("dev")){
			LOGGER.warning("Sms Sent in dev environment");
			return true;
		}
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		
		try {
			String sql = "SELECT user_mobile, user_sec_status, user_status, to_user_id, message, notification_type FROM users INNER JOIN events ON users.user_id=events.to_user_id WHERE event_id=?";
			ps = hcp.prepareStatement(sql);
			ps.setInt(1, eventId);
			
			rs = ps.executeQuery();
			
			if(rs.next()){
				String status = rs.getString("user_status");
				String phone = null;
				if(status.equals("mobile_pending") || status.equals("mobile_activated")){
					phone = rs.getString("to_user_id");
				}else if(rs.getInt("user_sec_status") == 1){
					phone = rs.getString("user_mobile");
				}else if(Notification_Type.valueOf(rs.getString("notification_type")).equals(Notification_Type.FLS_MOBILE_VERIFICATION)){
					phone = rs.getString("user_mobile");
				}else{
					phone = null;
				}
				
				if(phone != null){
					LOGGER.warning("Sending sms to phone number : " + phone);
				    TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);
					
				    List<NameValuePair> params = new ArrayList<NameValuePair>();
				    params.add(new BasicNameValuePair("Body", rs.getString("message")));
				    params.add(new BasicNameValuePair("To", "+91"+phone));
				    params.add(new BasicNameValuePair("From", "6507726500"));
			    
			    	MessageFactory messageFactory = client.getAccount().getMessageFactory();
			        Message message = messageFactory.create(params);
			        LOGGER.warning(message.getSid());
				}
				
			}else{
				LOGGER.warning("Event id doesnot exist!!");
			}
			
		} catch (Exception e) {
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
	
		return true;
	}
	
	private boolean sendEmail(int eventId){
		
		LOGGER.info("Sending email for this event id : " + eventId);
		
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null, rs2 = null;
		Connection hcp = getConnectionFromPool();
	
		JSONObject obj = new JSONObject();
		
		try{
			
			String sql = "SELECT user_email, user_sec_status, user_status, to_user_id, message, notification_type FROM users INNER JOIN events ON users.user_id=events.to_user_id WHERE event_id=?";
			ps1 = hcp.prepareStatement(sql);
			ps1.setInt(1, eventId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				String status = rs1.getString("user_status");
				String email = null;
				if(status.equals("email_pending") || status.equals("email_activated") || status.equals("facebook") || status.equals("google")){
					email = rs1.getString("to_user_id");
				}else if(rs1.getInt("user_sec_status") == 1){
					email = rs1.getString("user_email");
				}else if(Notification_Type.valueOf(rs1.getString("notification_type")).equals(Notification_Type.FLS_EMAIL_VERIFICATION)){
					email = rs1.getString("user_email");
				}else{
					email = null;
				}
				
				if(email != null){
					String sqlGetAllData = "SELECT tb1.*, tb2.*, tb3.*, tb4.user_id AS senders_user_id, tb4.user_full_name AS senders_full_name, tb4.user_profile_picture AS senders_profile_pic, tb4.user_activation AS senders_user_activation, tb4.user_referral_code AS senders_refferal_code FROM events tb1 LEFT JOIN items tb2 ON tb1.item_id=tb2.item_id LEFT JOIN users tb3 ON tb1.to_user_id=tb3.user_id LEFT JOIN users tb4 ON tb1.from_user_id=tb4.user_id WHERE event_id=?";
					ps2 = hcp.prepareStatement(sqlGetAllData);
					ps2.setInt(1, eventId);
					
					rs2 = ps2.executeQuery();
					
					if(rs2.next()){
						// Senders Data
						obj.put("fromUserId", rs2.getString("senders_user_id"));
						obj.put("fromUserName", rs2.getString("senders_full_name"));
						if(rs2.getString("senders_profile_pic") == null || rs2.getString("senders_profile_pic").equals("") || rs2.getString("senders_profile_pic").equals("null"))
							obj.put("fromProfilePic", "");
						else
							obj.put("fromProfilePic", rs2.getString("senders_profile_pic"));
						obj.put("fromUserActivation", rs2.getString("senders_user_activation"));
						obj.put("fromUserRefferalCode", rs2.getString("senders_refferal_code"));
						
						// Receivers Data
						obj.put("toUserId", rs2.getString("user_id"));
						obj.put("toUserName", rs2.getString("user_full_name"));
						if(rs2.getString("user_profile_picture") == null || rs2.getString("user_profile_picture").equals("") || rs2.getString("user_profile_picture").equals("null"))
							obj.put("toProfilePic", "");
						else
							obj.put("toProfilePic", rs2.getString("user_profile_picture"));
						obj.put("toUserActivation", rs2.getString("user_activation"));
						obj.put("toUserCredit", rs2.getInt("user_credit"));
						
						// Items Data
						obj.put("itemId", rs2.getInt("item_id"));
						if(rs2.getString("item_name") == null || rs2.getString("item_name").equals("") || rs2.getString("item_name").equals("null"))
							obj.put("title", "");
						else
							obj.put("title", rs2.getString("item_name"));
						if(rs2.getString("item_category") == null || rs2.getString("item_category").equals("") || rs2.getString("item_category").equals("null"))
							obj.put("category", "");
						else
							obj.put("category", rs2.getString("item_category"));
						if(rs2.getString("item_desc") == null || rs2.getString("item_desc").equals("") || rs2.getString("item_desc").equals("null"))
							obj.put("description", "");
						else
							obj.put("description", rs2.getString("item_desc"));
						obj.put("itemUserId", rs2.getString("item_user_id"));
						obj.put("leaseValue", rs2.getInt("item_lease_value"));
						obj.put("leaseTerm", rs2.getString("item_lease_term"));
						if(rs2.getString("item_primary_image_link") == null || rs2.getString("item_primary_image_link").equals("") || rs2.getString("item_primary_image_link").equals("null"))
							obj.put("imageLinks", "");
						else
							obj.put("imageLinks", rs2.getString("item_primary_image_link"));
						obj.put("uid", rs2.getString("item_uid"));
						obj.put("itemStatus", rs2.getString("item_status"));
						
						// Events Data
						obj.put("eventId", rs2.getInt("event_id"));
						obj.put("from", rs2.getString("from_user_id"));
						obj.put("to", email);
						obj.put("datetime", rs2.getString("datetime"));
						obj.put("notificationType", rs2.getString("notification_type"));
						obj.put("message", rs2.getString("message"));
						
						int leaseId = getLeaseId(obj.getInt("itemId"));
						if(leaseId != -1){
							obj.put("leaseId", leaseId);
						}
						
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
									if(obj.has("title") && obj.has("itemUserId") && obj.has("uid") && obj.has("itemId") && !obj.isNull("title") && !obj.isNull("itemUserId") && !obj.isNull("uid") && !obj.isNull("itemId"))
										matchItems.checkWishlist(obj.getString("title"), obj.getString("itemUserId"), obj.getString("uid"), obj.getInt("itemId"));
								}catch(Exception e){
									e.printStackTrace();
								}
							}
							FlsEmail mail = new FlsEmail();
							return mail.sendEmail(obj, Notification_Type.valueOf(obj.getString("notificationType")));
						}
					}
				}
			}
			
		}catch(SQLException e){
			LOGGER.warning(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (JSONException e) {
			LOGGER.warning(FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				if(rs1 != null)rs1.close();
				if(ps1 != null)ps1.close();
				if(rs2 != null)rs2.close();
				if(ps2 != null)ps2.close();
				if(hcp != null)hcp.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	private boolean sendEmailToFriend(String fromUserId, String toUserId, int eventId){
		
		LOGGER.info("Sending email to friend : " + toUserId);
		
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		Connection hcp = getConnectionFromPool();
	
		JSONObject obj = new JSONObject();
		
		try{
			
			String sqlSelectUserInfo = "SELECT user_full_name, user_referral_code FROM users WHERE user_id=?";
			ps1 = hcp.prepareStatement(sqlSelectUserInfo);
			ps1.setString(1, fromUserId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				
				if(validateEmail(toUserId)){
					obj.put("to", toUserId);
					obj.put("toUserCredit", 0);
					obj.put("from", rs1.getString("user_full_name"));
					obj.put("fromUserRefferalCode", rs1.getString("user_referral_code"));
					
					FlsEmail mail = new FlsEmail();
					return mail.sendEmail(obj, Notification_Type.FLS_MAIL_ADD_FRIEND_TO);
				}else{
					return sendSms(eventId);
				}
				
			}
			
		}catch(SQLException e){
			LOGGER.warning(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (JSONException e) {
			LOGGER.warning(FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				if(rs1 != null)rs1.close();
				if(ps1 != null)ps1.close();
				if(hcp != null)hcp.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return true;
		
	}

	public boolean validateEmail(String emailStr) {
		Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
		return matcher.find();
	}
	
	private boolean sendEmailToLead(String leadEmail,String senderEmail, int eventId){
		JSONObject obj = new JSONObject();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		Connection hcp = getConnectionFromPool();
		LOGGER.info("Sending email to Lead : " + leadEmail);
		try {
			String sqlSelectUserInfo = "SELECT user_full_name, user_credit, user_referral_code FROM users WHERE user_id=?";
			ps1 = hcp.prepareStatement(sqlSelectUserInfo);
			ps1.setString(1, leadEmail);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
			obj.put("to", leadEmail);
			obj.put("toUserCredit", rs1.getString("user_credit"));
			obj.put("from", rs1.getString("user_full_name"));
			obj.put("fromUserRefferalCode", rs1.getString("user_referral_code"));
		}else{
			obj.put("to", leadEmail);
			obj.put("toUserCredit", 0);
			obj.put("from", "");
			obj.put("fromUserRefferalCode", "");
		}
			FlsEmail mail = new FlsEmail();
			return mail.sendEmail(obj, Notification_Type.FLS_MAIL_ADD_LEAD);
		}catch(SQLException e){
			LOGGER.warning(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (JSONException e) {
			LOGGER.warning(FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
		}
		return true;
	}
	
	private int getLeaseId(int itemId){
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		int leaseId = -1;
		
		try{
			
			String sqlGetActiveLease = "SELECT lease_id FROM leases WHERE lease_item_id=?";
			ps1 = hcp.prepareStatement(sqlGetActiveLease);
			ps1.setInt(1, itemId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				leaseId = rs1.getInt("lease_id");
			}
			
		}catch(SQLException e){
			LOGGER.warning(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				if(rs1 != null)rs1.close();
				if(ps1 != null)ps1.close();
				if(hcp != null)hcp.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return leaseId;
		
	}

}