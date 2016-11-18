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
	
	public enum Subject{
		FRIEND,
		ITEM
	}
	
	public Message(){}
	
	public Message(int ItemId){
		this.itemId = ItemId;
	}
	
	public int sendMessage(String from, String to, Subject subject, String message){
		LOGGER.info("Inside sendMessage Method");
		
		String fromName = getUserName(from);
		String toName = getUserName(to);
		
		try{
			Event event = new Event();
			switch(subject){
				case ITEM:
					String uid = "", title = "";
					
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
					
					LOGGER.info("Sending message to item's owner : " + to);
					if(itemId != 0){
						event.createEvent(to, from, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_MESSAGE_ITEM_FROM, itemId, "From: <i>You</i>. To: <i>" + toName + "</i>. Regarding: <a href=\"" + URL + "/ItemDetails?uid=" + uid + "\">" + title + "</a>. <br> <i>" + message + "'</i>");
						event.createEvent(from, to, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_MESSAGE_ITEM_TO, itemId, "To: <i>You</i>. From: <i>" + fromName + "</i>. Regarding: <a href=\"" + URL + "/ItemDetails?uid=" + uid + "\">" + title + "</a>. <br> <i>'" + message + "'</i>");
					}else{
						return 0;
					}
				return 1;
				case FRIEND:
					LOGGER.info("Sending message to friend : " + to);
					event.createEvent(to, from, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_MESSAGE_FRIEND_FROM, 0, "From: <i>You</i>. To: <a href=\"" + URL + "/myapp.html#/myfriendslist\">" + toName + "</a>. <br> <i>'" + message + "'</i>");
					event.createEvent(from, to, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_MESSAGE_FRIEND_TO, 0, "From: <a href=\"" + URL + "/myapp.html#/myfriendslist\">" + fromName + "</a>. To: You. <br> <i>'" + message + "'</i>");
					return 1;
				default:
					return 0;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return 0;
	}
	
	private String getUserName(String userId){
		
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
				return rs1.getString("user_full_name");
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
		return null;
	}
	
}
