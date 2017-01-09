package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import adminOps.AdminOpsHandler;
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
		checkMembershipExpiry();
		
	}

	private void checkOldItems(){
		
		LOGGER.info("Checking old items");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null, ps3 = null;
		ResultSet rs1 = null, rs2 = null;
		
		try{
			
			String sqlSelectItemsToWarn = "SELECT * FROM items WHERE item_status IN ('InStore') AND item_lastmodified BETWEEN (CURRENT_TIMESTAMP - INTERVAL 6 YEAR_MONTH) AND (CURRENT_TIMESTAMP - INTERVAL 6 YEAR_MONTH + INTERVAL 192 DAY_HOUR)";
			ps1 = hcp.prepareStatement(sqlSelectItemsToWarn);
			rs1 = ps1.executeQuery();
			
			while(rs1.next()){
				LOGGER.info("Sending a warning to the onwer about the item id --- " + rs1.getInt("item_id"));
				try {
					Event event = new Event();
					event.createEvent(rs1.getString("item_user_id"), rs1.getString("item_user_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_OLD_ITEM_WARN, rs1.getInt("item_id"), "Your Item <a href=\"" + URL + "/ItemDetails?uid=" + rs1.getString("item_uid") + "\">" + rs1.getString("item_name") + "</a>. has been inactive for past 6 months.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			String sqlSelectOldItems = "SELECT * FROM items WHERE item_status IN ('InStore') AND item_lastmodified <= (CURRENT_TIMESTAMP - INTERVAL 6 YEAR_MONTH)";
			ps2 = hcp.prepareStatement(sqlSelectOldItems);
			rs2 = ps2.executeQuery();
			
			while(rs2.next()){
				LOGGER.info("Archiving item id --- " + rs2.getInt("item_id"));
				
				String sqlArchiveItem = "UPDATE `items` SET `item_status`='Archived' WHERE item_id = ?";
				ps3 = hcp.prepareStatement(sqlArchiveItem);
				ps3.setInt(1, rs2.getInt("item_id"));
				ps3.executeUpdate();
				
				// logging item status to archived
				LogItem li = new LogItem();
				li.addItemLog(rs2.getInt("item_id"), "Archived", "", "");
				
				Event event = new Event();
				event.createEvent(rs2.getString("item_user_id"), rs2.getString("item_user_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_DELETE_ITEM, rs2.getInt("item_id"), "Your Item " + rs2.getInt("item_id") + " has been deleted from frrndlease store.");
				
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
			
			String sqlSelectRequestsToWarn = "SELECT tb1.*, tb2.* FROM requests tb1 INNER JOIN items tb2 ON tb1.request_item_id=tb2.item_id WHERE request_status='Active' AND request_lastmodified BETWEEN (CURRENT_TIMESTAMP - INTERVAL 168 DAY_HOUR) AND (CURRENT_TIMESTAMP - INTERVAL 168 DAY_HOUR + INTERVAL 72 DAY_HOUR)";
			ps1 = hcp.prepareStatement(sqlSelectRequestsToWarn);
			rs1 = ps1.executeQuery();
			
			while(rs1.next()){
				LOGGER.info("Sending a warning to the onwer about the request id --- " + rs1.getInt("request_id"));
				try {
					Event event = new Event();
					event.createEvent(rs1.getString("item_user_id"), rs1.getString("item_user_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_OLD_REQUEST_WARN, rs1.getInt("request_item_id"), "You have not responded to the request for the item " + rs1.getString("item_name") + ". It will be removed in less than 2 days.");
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
			
			String sqlSelectLeasesToWarn = "SELECT tb1.*, tb2.* FROM items tb1 INNER JOIN (SELECT * FROM leases WHERE lease_status='Active') tb2 ON tb1.item_id=tb2.lease_item_id WHERE item_status IN ('LeaseReady') AND item_lastmodified BETWEEN (CURRENT_TIMESTAMP - INTERVAL 168 DAY_HOUR) AND (CURRENT_TIMESTAMP - INTERVAL 168 DAY_HOUR + INTERVAL 72 DAY_HOUR)";
			ps1 = hcp.prepareStatement(sqlSelectLeasesToWarn);
			rs1 = ps1.executeQuery();
			
			while(rs1.next()){
				LOGGER.info("Sending a warning to the leasee about item - " + rs1.getInt("item_id") + " not picked up.");
				try {
					Event event = new Event();
					event.createEvent(rs1.getString("lease_requser_id"), rs1.getString("lease_requser_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_OLD_LEASE_WARN, rs1.getInt("item_id"), "You have not picked up the leased item " + rs1.getString("item_name") + ". This lease will be removed in less than 2 days.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			String sqlSelectOldLeases = "SELECT tb1.*, tb2.* FROM items tb1 INNER JOIN (SELECT * FROM leases WHERE lease_status='Active') tb2 ON tb1.item_id=tb2.lease_item_id WHERE item_status IN ('LeaseReady') AND item_lastmodified <= (CURRENT_TIMESTAMP - INTERVAL 168 DAY_HOUR)";
			ps2 = hcp.prepareStatement(sqlSelectOldLeases);
			rs2 = ps2.executeQuery();
			
			while(rs2.next()){
				LOGGER.info("Archiving lease id --- " + rs2.getInt("lease_id"));
				
				AdminOpsHandler aoh = new AdminOpsHandler();
				
				JSONObject json = new JSONObject();
				json.put("operation", "closelease");
				
				JSONObject row = new JSONObject();
				row.put("reqUserId", rs2.getString("lease_requser_id"));
				row.put("itemId", Integer.toString(rs2.getInt("item_id")));
				row.put("userId", rs2.getString("lease_user_id"));
				row.put("status", "");
				json.put("row", row);
				
				aoh.getInfo("leases", json);
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

	private void checkMembershipExpiry() {
		
		LOGGER.info("Inside CheckMembershipExpiry Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null, ps3 = null, ps4 = null;
		ResultSet rs1 = null, rs2 = null;
		int rs3, rs4;
		
		try{
			
			String sqlSelectUsersToWarn = "SELECT user_id FROM users WHERE user_fee_expiry IS NOT NULL AND user_fee_expiry BETWEEN (CURRENT_TIMESTAMP) AND (CURRENT_TIMESTAMP + INTERVAL 72 DAY_HOUR)";
			ps1 = hcp.prepareStatement(sqlSelectUsersToWarn);
			rs1 = ps1.executeQuery();
			
			while(rs1.next()){
				LOGGER.info("Sending a reminder to the user about upgrading membership date to avoid nulling lease fee");
				try {
					Event event = new Event();
					event.createEvent("admin@frrndlease.com", rs1.getString("user_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_UBER_WARN, 0, "You have less than 3 days left for your uber membership to expiry. Please upgrade it to avoid nulling of all your items lease fee.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			String sqlGettingExpiredMemberships = "SELECT user_id FROM users WHERE user_fee_expiry IS NOT NULL AND user_fee_expiry <= CURRENT_TIMESTAMP";
			ps2 = hcp.prepareStatement(sqlGettingExpiredMemberships);
			rs2 = ps2.executeQuery();
			
			while(rs2.next()){
				LOGGER.info("Expiring Membership of the user --- " + rs2.getString("user_id") +  " by making user_fee_expiry date to null");
				
				String sqlUpdateExpiryFee = "UPDATE users SET user_fee_expiry=null WHERE user_id=?";
				ps3 = hcp.prepareStatement(sqlUpdateExpiryFee);
				ps3.setString(1, rs2.getString("user_id"));
				
				rs3 = ps3.executeUpdate();
				
				if(rs3 == 1){
					LOGGER.info("User expiry fee made null for the userId --- " + rs2.getString("user_id"));
				}else{
					LOGGER.info("User expiry fee could not be made null for the userId --- " + rs2.getString("user_id"));
				}
				
				LOGGER.info("Making item surcharge for items of the user --- " + rs2.getString("user_id") +  " to 0");
				
				String sqlUpdateItemsSurcharge = "UPDATE items SET item_surcharge=0 WHERE item_user_id=?";
				ps4 = hcp.prepareStatement(sqlUpdateItemsSurcharge);
				ps4.setString(1, rs2.getString("user_id"));
				
				rs4 = ps4.executeUpdate();
				
				if(rs4 == 1){
					LOGGER.info("Items Surcharge made 0 for the userId --- " + rs2.getString("user_id"));
				}else{
					LOGGER.info("Items Surcharge could not be made 0 for the userId --- " + rs2.getString("user_id"));
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
				if(ps4 != null) ps4.close();
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
	
}
