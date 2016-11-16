package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import connect.Connect;
import util.Event.Event_Type;
import util.Event.Notification_Type;

public class Message extends Connect {

	private FlsLogger LOGGER = new FlsLogger(Message.class.getName());

	private String URL = FlsConfig.prefixUrl;
	
	private int itemId;
	
	private String uid = "", title = "", fromName = "", toName = "";
	
	public enum Subject{
		FRIEND,
		ITEM
	}
	
	public enum User{
		FROM,
		TO
	}
	
	public Message(){}
	
	public Message(int ItemId){
		this.itemId = ItemId;
	}
	
	public int sendMessage(String from, String to, Subject subject, String message){
		LOGGER.info("Inside sendMessage Method");
		
		getUserName(from, User.FROM);
		getUserName(to, User.TO);
		
		try{
			Event event = new Event();
			switch(subject){
				case FRIEND:
					LOGGER.info("Sending message to friend : " + to);
					event.createEvent(to, from, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_MESSAGE_FRIEND_FROM, 0, "Your friend <a href=\"" + URL + "/myapp.html#/myfriendslist\">" + toName + "</a> has been sent a message. The message is:- <br> <i>'" + message + "'</i>");
					event.createEvent(from, to, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_MESSAGE_FRIEND_TO, 0, "Your friend <a href=\"" + URL + "/myapp.html#/myfriendslist\">" + fromName + "</a> sent you a message. The message is:- <br> <i>'" + message + "'</i>");
					return 1;
				case ITEM:
					getItemDetails();
					LOGGER.info("Sending message to item's owner : " + to);
					if(itemId != 0){
						event.createEvent(to, from, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_MESSAGE_ITEM_FROM, itemId, "You have sent a message to user <i>" + toName + "</i> regarding an item <a href=\"" + URL + "/ItemDetails?uid=" + uid + "\">" + title + "</a>. The message is:- <br> <i>'" + message + "'</i>");
						event.createEvent(from, to, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_MESSAGE_ITEM_TO, itemId, " You have recieved a message from user <i>" + fromName + "</i> regarding an item <a href=\"" + URL + "/ItemDetails?uid=" + uid + "\">" + title + "</a>. The message is:- <br> <i>'" + message + "'</i>");
					}else{
						return 0;
					}
					return 1;
				default:
					return 0;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return 0;
	}
	
	private void getUserName(String userId, User user){
		
		LOGGER.info("Inside getUserName Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		try{
			String sqlGetUserName = "SELECT user_full_name FROM users WHERE user_id=?";
			ps1 = hcp.prepareStatement(sqlGetUserName);
			ps1.setString(1, userId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				String name = rs1.getString("user_full_name");
				if(user.name().equals(User.FROM.name()))
					fromName = name;
				else if(user.name().equals(User.TO.name()))
					toName = name;
			}else{
				LOGGER.warning("Not Able to find userId : " + userId);
			}
			
		}catch(Exception e){
			LOGGER.warning("Error occured while getting user name");
			e.printStackTrace();
		}finally{
			try{
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
	private void getItemDetails(){
		
		LOGGER.info("Inside getItemDetails Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		try{
			String sqlGetUserName = "SELECT item_name, item_uid FROM items WHERE item_id=?";
			ps1 = hcp.prepareStatement(sqlGetUserName);
			ps1.setInt(1, itemId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				title = rs1.getString("item_name");
				uid = rs1.getString("item_uid");
			}else{
				LOGGER.warning("Not Able to find itemId : " + itemId);
			}
			
		}catch(Exception e){
			LOGGER.warning("Error occured while getting item title and uid");
			e.printStackTrace();
		}finally{
			try{
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
}
