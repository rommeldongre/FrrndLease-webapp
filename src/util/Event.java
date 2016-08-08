package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.GetNotificationsListResObj;
import pojos.GetNotificationsResObj;
import pojos.GetUnreadEventsCountResObj;
import util.FlsEnums.Delivery_Status;
import util.FlsEnums.Event_Type;
import util.FlsEnums.Notification_Type;
import util.FlsEnums.Read_Status;

public class Event extends Connect{
	
	FlsLogger LOGGER = new FlsLogger(Event.class.getName());
	
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
			} catch (SQLException e) {
				// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return 0;
		
	}

public GetNotificationsListResObj getNotifications(String userId, Event_Type flsEventNotification, int limit, int offset) {
	
	PreparedStatement ps = null;
	ResultSet rs = null;
	Connection hcp = getConnectionFromPool();
	
	GetNotificationsListResObj response = new GetNotificationsListResObj();
	
	try{
		String sqlGetNotifications = "SELECT * FROM events WHERE to_user_id=? AND event_type=? ORDER BY event_id LIMIT ?,?";
		ps = hcp.prepareStatement(sqlGetNotifications);
		ps.setString(1, userId);
		ps.setString(2, flsEventNotification.name());
		ps.setInt(3, offset);
		ps.setInt(4, limit);
		
		rs = ps.executeQuery();
		
		if(rs.isBeforeFirst()){
			while(rs.next()){
				GetNotificationsResObj res = new GetNotificationsResObj();
				res.setEventId(rs.getInt("event_id"));
				res.setDatetime(rs.getString("datetime"));
				res.setFromUserId(rs.getString("from_user_id"));
				res.setToUserId(rs.getString("to_user_id"));
				res.setReadStatus(rs.getString("read_status"));
				res.setItemId(rs.getInt("item_id"));
				res.setNotificationMsg(rs.getString("message"));
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
			// TODO Auto-generated catch block
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
		String sqlGetUnreadEventsCount = "SELECT count(*) FROM events WHERE to_user_id=? AND read_status=?";
		ps = hcp.prepareStatement(sqlGetUnreadEventsCount);
		ps.setString(1, userId);
		ps.setString(2, Read_Status.FLS_UNREAD.name());
		
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	return response;
}

}
