package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font.FontFamily;

import connect.Connect;
import util.Event.Event_Type;
import util.Event.Notification_Type;

public class FlsPlan extends Connect{

	private FlsLogger LOGGER = new FlsLogger(FlsPlan.class.getName());
	
	private String URL = FlsConfig.prefixUrl;
	
	public enum Fls_Plan{
		FLS_SELFIE,
		FLS_PRIME,
		FLS_UBER
	}
	
	public enum Delivery_Plan{
		FLS_NONE,
		FLS_SELF,
		FLS_OPS
	}
	
	public void checkPlan(String userId){
		
		LOGGER.info("Inside check plan for the user id : " + userId);
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null, rs2 = null;
		
		try{
			
			String sqlSelectUser = "SELECT user_locality, user_verified_flag, user_plan FROM users where user_id=?";
			ps1 = hcp.prepareStatement(sqlSelectUser);
			ps1.setString(1, userId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				if(rs1.getInt("user_verified_flag") == 1){
					String sqlCheckPlaces = "SELECT * FROM places WHERE locality=?";
					
					ps2 = hcp.prepareStatement(sqlCheckPlaces);
					ps2.setString(1, rs1.getString("user_locality"));
					
					rs2 = ps2.executeQuery();
					
					if(rs2.next()){
						if(!Fls_Plan.FLS_PRIME.name().equals(rs1.getString("user_plan")))
							setUserPlan(userId, Fls_Plan.FLS_PRIME);
					}else{
						if(!Fls_Plan.FLS_SELFIE.name().equals(rs1.getString("user_plan")))
							setUserPlan(userId, Fls_Plan.FLS_SELFIE);
					}
				}else{
					LOGGER.info("The user is not verified");
					if(!Fls_Plan.FLS_SELFIE.name().equals(rs1.getString("user_plan")))
						setUserPlan(userId, Fls_Plan.FLS_SELFIE);
				}
			}else{
				LOGGER.info("The user id : " + userId + " does not exist");
			}
			
		}catch(Exception e){
			LOGGER.warning("Exception occured while checking plan");
			e.printStackTrace();
		}finally{
			try{
				if(rs2 != null)	rs2.close();
				if(ps2 != null) ps2.close();
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
	public void setUserPlan(String userId, Fls_Plan user_plan){
		
		LOGGER.info("Changing user plan to : " + user_plan);
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int rs1;
		
		try{
			
			String sql = "UPDATE users SET user_plan=? WHERE user_id=?";
			ps1 = hcp.prepareStatement(sql);
			ps1.setString(1, user_plan.name());
			ps1.setString(2, userId);
			
			rs1 = ps1.executeUpdate();
			
			if(rs1 == 1)
				LOGGER.info("user plan updated to : " + user_plan);
			else
				LOGGER.info("Not able to change user plan");
			
		}catch(Exception e){
			LOGGER.warning("Exception occured while checking plan");
			e.printStackTrace();
		}finally{
			try{
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
	public int changeDeliveryPlan(int leaseId, Delivery_Plan delivery_plan){
		
		LOGGER.info("Changing delivery plan to : " + delivery_plan);
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null;
		int rs2 = 0;
		
		try{
			
			String sqlGetLease = "SELECT tb1.*, tb2.* FROM leases tb1 INNER JOIN items tb2 ON tb1.lease_item_id=tb2.item_id WHERE lease_id=? AND lease_status=?";
			ps1 = hcp.prepareStatement(sqlGetLease);
			ps1.setInt(1, leaseId);
			ps1.setString(2, "Active");
			
			rs1 = ps1.executeQuery();
			
			if(!rs1.next()){
				LOGGER.info("Not able to find the leaseId - " + leaseId + " in the lease table.");
				return rs2;
			}

			if(!rs1.getString("item_status").equals("LeaseReady")){
				LOGGER.info("Item status is not LeaseReady!!");
				return rs2;
			}

			if(!rs1.getString("delivery_plan").equals("FLS_NONE")){
				LOGGER.info("Delivery Plan is not FLS_NONE so it is already changed..");
				return rs2;
			}

			String sql = "UPDATE leases SET delivery_plan=? WHERE lease_id=?";
			ps2 = hcp.prepareStatement(sql);
			ps2.setString(1, delivery_plan.name());
			ps2.setInt(2, leaseId);
						
			rs2 = ps2.executeUpdate();
						
			if(rs2 == 1){
				LOGGER.info("delivery plan updated to : " + delivery_plan);
				
				String reqUser = rs1.getString("lease_requser_id");
				String user = rs1.getString("lease_user_id");
				int itemId = rs1.getInt("item_id");
				
				Event event = new Event();
				if(rs1.getString("delivery_plan").equals(Delivery_Plan.FLS_SELF.name())){
					event.createEvent(reqUser, user, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_GRANT_LEASE_FROM_SELF, itemId, "You have sucessfully leased an item to <a href=\"" + URL + "/myapp.html#/myleasedoutitems\">" + reqUser + "</a> on Friend Lease ");
					event.createEvent(user, reqUser, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_GRANT_LEASE_TO_SELF, itemId, "An item has been leased by <a href=\"" + URL + "/myapp.html#/myleasedinitems\">" + user + "</a> to you on Friend Lease ");
				}else{
					event.createEvent("admin@frrndlease.com", "ops@frrndlease.com", Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_OPS_PICKUP_READY, itemId, "The lease item - " + itemId + " is ready to be picked up.");
					event.createEvent("ops@frrndlease.com", "admin@frrndlease.com", Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_OPS_PICKUP_READY, itemId, "The lease item - " + itemId + " is ready to be picked up.");
				}
			}else{
				LOGGER.info("Not able to change delivery plan for lease id : " + leaseId);
			}

		}catch(Exception e){
			LOGGER.warning("Exception occured while checking plan");
			e.printStackTrace();
		}finally{
			try{
				if(ps2 != null) ps2.close();
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return rs2;
		
	}
	
	public int changePickupStatus(int leaseId, boolean isOwner, boolean pickupStatus){
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int rs1 = 0;
		
		if(isOwner){
			LOGGER.info("Changing pickup status of leaseId : " + leaseId + " for owner " + " to " + pickupStatus);
		}else{
			LOGGER.info("Changing pickup status of leaseId : " + leaseId + " for requestor " + " to " + pickupStatus);
		}

		try{
			
			if(checkPickupStatus(leaseId)){
				return rs1;
			}
			
			String sql = "UPDATE leases SET ";
			if(isOwner){
				sql = sql + "owner_pickup_status=? WHERE lease_id=?";
			}else{
				sql = sql + "leasee_pickup_status=? WHERE lease_id=?";
			}
			
			ps1 = hcp.prepareStatement(sql);
			ps1.setBoolean(1, pickupStatus);
			ps1.setInt(2, leaseId);
			
			rs1 = ps1.executeUpdate();
			
			if(rs1 == 1)
				LOGGER.info("changed pickup status");
			else
				LOGGER.info("Not able to change pickup status");
			
			if(checkPickupStatus(leaseId)){
				if(isLeaseEnded(leaseId))
					closeLease(leaseId);
				else
					startLease(leaseId);
			}
			
		}catch(Exception e){
			LOGGER.warning("Exception occured while checking plan");
			e.printStackTrace();
		}finally{
			try{
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return rs1;
		
	}
	
	private boolean checkPickupStatus(int leaseId){
		
		LOGGER.info("Inside checkPickupStatus Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		try{
			
			String sqlgetBothStatus = "SELECT * FROM leases WHERE lease_id=? AND lease_status=? AND owner_pickup_status=? AND leasee_pickup_status=?";
			ps1 = hcp.prepareStatement(sqlgetBothStatus);
			ps1.setInt(1, leaseId);
			ps1.setString(2, "Active");
			ps1.setBoolean(3, true);
			ps1.setBoolean(4, true);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				return true;
			}
			
		}catch(Exception e){
			LOGGER.warning("Exception occured while checking plan");
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
		
		return false;
		
	}
	
	private boolean isLeaseEnded(int leaseId){
		
		LOGGER.info("Inside isLeaseReady Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		try{
			
			String sqlgetBothStatus = "SELECT tb1.*, tb2.* FROM leases tb1 INNER JOIN items tb2 ON tb1.lease_item_id=tb2.item_id WHERE lease_id=?";
			ps1 = hcp.prepareStatement(sqlgetBothStatus);
			ps1.setInt(1, leaseId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				if(rs1.getString("item_status").equals("LeaseEnded"))
					return true;
			}
			
		}catch(Exception e){
			LOGGER.warning("Exception occured while checking plan");
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
		
		return false;
		
	}
	
	private void startLease(int leaseId){
		
		LOGGER.info("Inside startLease Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null, ps3 = null;
		ResultSet rs1 = null;
		int rs2, rs3;
		
		try{
			
			String sqlGetLease = "SELECT * FROM leases WHERE lease_id=?";
			ps1 = hcp.prepareStatement(sqlGetLease);
			ps1.setInt(1, leaseId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){

				int itemId = rs1.getInt("lease_item_id");
				
				String sqlStartLease = "UPDATE items SET item_status=? WHERE item_id=?";
				ps2 = hcp.prepareStatement(sqlStartLease);
				ps2.setString(1, "LeaseStarted");
				ps2.setInt(2, itemId);
				
				rs2 = ps2.executeUpdate();
				
				if(rs2 == 1){
					Event event = new Event();
					event.createEvent(rs1.getString("lease_requser_id"), rs1.getString("lease_user_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_FROM_LEASE_STARTED, itemId, "The lease : " + leaseId + " has been started.");
					event.createEvent(rs1.getString("lease_user_id"), rs1.getString("lease_requser_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_TO_LEASE_STARTED, itemId, "Your lease : " + leaseId + " has been started.");
				}else{
					LOGGER.warning("Not able to start lease for leaseId : " + leaseId);
				}
				
				String sqlResetPickupStatus = "UPDATE leases set owner_pickup_status=? AND leasee_pickup_status=? WHERE lease_item_id=? AND lease_status=?";
				ps3 = hcp.prepareStatement(sqlResetPickupStatus);
				ps3.setBoolean(1, false);
				ps3.setBoolean(2, false);
				ps3.setInt(3, itemId);
				ps3.setString(4, "Active");
				
				rs3 = ps3.executeUpdate();
				
				if(rs3 == 1)
					LOGGER.info("Pickup status resetted for the lease item : " + itemId);
				else
					LOGGER.info("Not able to reset pickup status for the lease item : " + itemId);
				
			}
			
		}catch(Exception e){
			LOGGER.warning("Exception occured while checking plan");
			e.printStackTrace();
		}finally{
			try{
				if(ps3 != null) ps3.close();
				if(ps2 != null) ps2.close();
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
	private void closeLease(int leaseId){
		
		LOGGER.info("inside closeLease method");

		Connection hcp = getConnectionFromPool();
		
		PreparedStatement ps1 = null, ps2 = null, ps3 = null, ps4 = null;
		ResultSet rs1 =  null, rs4 = null;
		int rs2, rs3;

		try {
			String sqlGetLease = "SELECT * FROM leases WHERE lease_id=?";
			ps1 = hcp.prepareStatement(sqlGetLease);
			ps1.setInt(1, leaseId);

			rs1 = ps1.executeQuery();
			
			if (rs1.next()) {
				
				int itemId = rs1.getInt("lease_item_id");
				String userId = rs1.getString("lease_user_id");
				String reqUserId = rs1.getString("lease_requser_id");
				
				String sqlArchiveLease = "UPDATE leases SET lease_status=? WHERE lease_id=?";
				ps2 = hcp.prepareStatement(sqlArchiveLease);
				ps2.setString(1, "Archived");
				ps2.setInt(2, leaseId);
				
				rs2 = ps2.executeUpdate();
				
				if(rs2 == 1){
					LOGGER.info("Lease Id - " + leaseId + " status changed to 'Archived'");
					
					String sqlUpdateItemStatus = "UPDATE items SET item_status=? WHERE item_id=?";
					ps3 = hcp.prepareStatement(sqlUpdateItemStatus);
					ps3.setString(1, "InStore");
					ps3.setInt(2, itemId);
					
					rs3 = ps3.executeUpdate();
					
					if(rs3 == 1){
						LOGGER.info("Item id - " + itemId + "status updated back to 'InStore'");
						
						String sqlSelectItemDetails = "SELECT * FROM items WHERE item_id=?";
						ps4 = hcp.prepareStatement(sqlSelectItemDetails);
						ps4.setInt(1, itemId);
						
						rs4 = ps4.executeQuery();
						
						if(rs4.next()){
							
							// logging item status to back InStore
							LogItem li = new LogItem();
							li.addItemLog(itemId, "InStore", "", rs4.getString("item_primary_image_link"));
							
							String uid = rs4.getString("item_uid");
							String title = rs4.getString("item_name");
							Event event = new Event();
							event.createEvent(reqUserId, userId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_ITEM_INSTORE_FROM, itemId, "Your item <a href=\"" + URL + "/ItemDetails?uid=" + uid + "\">" + title + "</a> is back InStore.");
							event.createEvent(userId, reqUserId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_ITEM_INSTORE_TO, itemId, "The item <a href=\"" + URL + "/ItemDetails?uid=" + uid + "\">" + title + "</a> is back InStore.");
						}else{
							LOGGER.info("Not able to get item details for item id - " + rs1.getInt("lease_item_id"));
						}
						
					}else{
						LOGGER.info("Item id - " + rs1.getInt("lease_item_id") + "status not updated back to 'InStore'");
					}
					
				}else{
					LOGGER.info("Lease Id - " + leaseId + " status not changed to 'Archived'");
				}
				
			}
			
		} catch (Exception e) {
			LOGGER.warning("Exception occured..");
			e.printStackTrace();
		} finally{
			try {
				if(rs4 != null) rs4.close();
				if(ps4 != null) ps4.close();
				if(ps3 != null) ps3.close();
				if(ps2 != null) ps2.close();
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public Document getLeaseAgreement(Document doc, int leaseId){
		
		//create some special styles and font sizes
		Font bfBold18 = new Font(FontFamily.TIMES_ROMAN, 18, Font.BOLD, new BaseColor(0, 0, 0));
		
		try{
			//document header properties
			doc.addAuthor("Blue Marble");
			doc.addCreationDate();
			doc.addCreator("frrndlease.com");
			doc.addTitle("Lease Agreement");
			doc.setPageSize(PageSize.A4);
			doc.open();
			
			//add a new paragraph
			doc.add( new Paragraph("Lease Agreement", bfBold18));
		}catch(DocumentException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return doc;
	}
	
}
