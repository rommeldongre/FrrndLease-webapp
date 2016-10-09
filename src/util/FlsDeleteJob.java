package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import connect.Connect;
import util.Event.Event_Type;
import util.Event.Notification_Type;

public class FlsDeleteJob extends Connect implements org.quartz.Job {

	private FlsLogger LOGGER = new FlsLogger(FlsDeleteJob.class.getName());

	private String URL = FlsConfig.prefixUrl;
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		LOGGER.warning("Its 5AM, Starting FlsDeleteJob...");
		checkOldItems();
		checkOldRequests();
		checkOldLeases();
		
	}

	private void checkOldItems(){
		
		LOGGER.info("Checking old items");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null, rs2 = null;
		
		try{
			
			String sqlSelectItemsToWarn = "SELECT item_id FROM items WHERE item_status IN ('InStore') AND item_lastmodified BETWEEN (CURRENT_TIMESTAMP - INTERVAL 6 YEAR_MONTH + INTERVAL 144 DAY_HOUR) AND (CURRENT_TIMESTAMP - INTERVAL 6 YEAR_MONTH + INTERVAL 192 DAY_HOUR)";
			ps1 = hcp.prepareStatement(sqlSelectItemsToWarn);
			rs1 = ps1.executeQuery();
			
			while(rs1.next()){
				LOGGER.info(rs1.getInt("item_id") + "----");
			}
			
			String sqlSelectOldItems = "SELECT item_id FROM items WHERE item_status IN ('InStore') AND item_lastmodified <= (CURRENT_TIMESTAMP - INTERVAL 6 YEAR_MONTH)";
			ps2 = hcp.prepareStatement(sqlSelectOldItems);
			rs2 = ps2.executeQuery();
			
			while(rs2.next()){
				LOGGER.info(rs2.getInt("item_id") + "----");
			}
			
		}catch(SQLException e){
			LOGGER.warning("Error with the mysql operation");
			e.printStackTrace();
		}catch(Exception e){
			LOGGER.warning("Exception Occured");
			e.printStackTrace();
		}finally{
			try{
				if(rs2 != null) rs2.close();
				if(ps2 != null) ps2.close();
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
	private void checkOldRequests(){
		
		LOGGER.info("Checking old requests");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null,ps3 = null;
		ResultSet rs1 = null, rs2 = null;
		
		try{
			
			String sqlSelectRequestsToWarn = "SELECT tb1.*, tb2.* FROM requests tb1 INNER JOIN items tb2 ON tb1.request_item_id=tb2.item_id WHERE request_status='Active' AND request_lastmodified BETWEEN (CURRENT_TIMESTAMP - INTERVAL 168 DAY_HOUR + INTERVAL 24 DAY_HOUR) AND (CURRENT_TIMESTAMP - INTERVAL 168 DAY_HOUR + INTERVAL 72 DAY_HOUR)";
			ps1 = hcp.prepareStatement(sqlSelectRequestsToWarn);
			rs1 = ps1.executeQuery();
			
			while(rs1.next()){
				LOGGER.info("Sending a warning to the onwer about the request id --- " + rs1.getInt("request_id"));
				try {
					Event event = new Event();
					event.createEvent(rs1.getString("item_user_id"), rs1.getString("item_user_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_OLD_REQUEST_WARN, rs1.getInt("request_item_id"), "You have not responded to the request for the item " + rs1.getString("item_name") + ". It will be removed in 2 days.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			String sqlSelectOldRequests = "SELECT tb1.*, tb2.* FROM requests tb1 INNER JOIN items tb2 ON tb1.request_item_id=tb2.item_id WHERE tb1.request_status='Active' AND tb1.request_lastmodified <= (CURRENT_TIMESTAMP - INTERVAL 168 DAY_HOUR)";
			ps2 = hcp.prepareStatement(sqlSelectOldRequests);
			rs2 = ps2.executeQuery();
			
			while(rs2.next()){
				LOGGER.info("Archiving request id --- " + rs2.getInt("request_id"));
				
				String sqlArchiveRequest = "UPDATE requests SET request_status=? WHERE request_id=?";
				ps3 = hcp.prepareStatement(sqlArchiveRequest);
				ps3.setString(1, "Archived");
				ps3.setInt(2, rs2.getInt("request_id"));
				
				ps3.executeUpdate();
				
				try {
					Event event = new Event();
					event.createEvent(rs2.getString("item_user_id"), rs2.getString("request_requser_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_DELETE_REQUEST_FROM, rs2.getInt("request_item_id"), "Your Request for item having id <a href=\"" + URL + "/ItemDetails?uid=" + rs2.getString("item_uid") + "\">" + rs2.getString("item_name") + "</a> has been removed. ");
					event.createEvent(rs2.getString("request_requser_id"), rs2.getString("item_user_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_DELETE_REQUEST_TO, rs2.getInt("request_item_id"), "Request for item having id <a href=\"" + URL + "/ItemDetails?uid=" + rs2.getString("item_uid") + "\">" + rs2.getString("item_name") + "</a> has been removed by the Requestor. ");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}catch(SQLException e){
			LOGGER.warning("Error with the mysql operation");
			e.printStackTrace();
		}catch(Exception e){
			LOGGER.warning("Exception Occured");
			e.printStackTrace();
		}finally{
			try{
				if(ps3 != null) ps3.close();
				if(rs2 != null) rs2.close();
				if(ps2 != null) ps2.close();
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
	private void checkOldLeases(){
		
		LOGGER.info("Check old leases");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null, rs2 = null;
		
		try{
			
			String sqlSelectLeasesToWarn = "SELECT item_id FROM items WHERE item_status IN ('LeaseReady') AND item_lastmodified BETWEEN (CURRENT_TIMESTAMP - INTERVAL 168 DAY_HOUR + INTERVAL 24 DAY_HOUR) AND (CURRENT_TIMESTAMP - INTERVAL 168 DAY_HOUR + INTERVAL 72 DAY_HOUR)";
			ps1 = hcp.prepareStatement(sqlSelectLeasesToWarn);
			rs1 = ps1.executeQuery();
			
			while(rs1.next()){
				LOGGER.info(rs1.getInt("item_id") + "----");
			}
			
			String sqlSelectOldLeases = "SELECT item_id FROM items WHERE item_status IN ('LeaseReady') AND item_lastmodified <= (CURRENT_TIMESTAMP - INTERVAL 168 DAY_HOUR)";
			ps2 = hcp.prepareStatement(sqlSelectOldLeases);
			rs2 = ps2.executeQuery();
			
			while(rs2.next()){
				LOGGER.info(rs2.getInt("item_id") + "----");
			}
			
		}catch(SQLException e){
			LOGGER.warning("Error with the mysql operation");
			e.printStackTrace();
		}catch(Exception e){
			LOGGER.warning("Exception Occured");
			e.printStackTrace();
		}finally{
			try{
				if(rs2 != null) rs2.close();
				if(ps2 != null) ps2.close();
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
}
