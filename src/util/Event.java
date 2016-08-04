package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import connect.Connect;
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

}
