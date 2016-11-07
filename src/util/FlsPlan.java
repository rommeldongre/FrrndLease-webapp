package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.ByteArrayOutputStream;
import java.net.URL;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import connect.Connect;
import util.Event.Event_Type;
import util.Event.Notification_Type;

public class FlsPlan extends Connect{

	private FlsLogger LOGGER = new FlsLogger(FlsPlan.class.getName());
	
	private String URL = FlsConfig.prefixUrl;
	
	private String LOGO_URL = "http://s3-ap-south-1.amazonaws.com/fls-meta/fls-logo.png";
	private String USER_ICON = "http://s3-ap-south-1.amazonaws.com/fls-meta/user_icon.png";
	
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
			
			if(rs1 == 1){
				LOGGER.info("changed pickup status");
				if(checkPickupStatus(leaseId)){
					if(isLeaseEnded(leaseId))
						closeLease(leaseId);
					else
						startLease(leaseId);
				}
			}
			else{
				LOGGER.info("Not able to change pickup status");
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
			ps1.setInt(3, 1);
			ps1.setInt(4, 1);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				LOGGER.info("Pickup confirmed by both the parties....");
				return true;
			}else{
				LOGGER.info("Pickup not confirmed by both the parties..");
			}
			
		}catch(Exception e){
			LOGGER.warning("Exception occured while checking pickup status");
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
		
		LOGGER.info("Inside isLeaseEnded Method");
		
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
				
				String sqlResetPickupStatus = "UPDATE leases set owner_pickup_status=?, leasee_pickup_status=? WHERE lease_id=? AND lease_status=?";
				ps3 = hcp.prepareStatement(sqlResetPickupStatus);
				ps3.setInt(1, 0);
				ps3.setInt(2, 0);
				ps3.setInt(3, leaseId);
				ps3.setString(4, "Active");
				
				rs3 = ps3.executeUpdate();
				
				if(rs3 == 1)
					LOGGER.info("Pickup status resetted for the lease id : " + leaseId);
				else
					LOGGER.info("Not able to reset pickup status for the lease id : " + leaseId);
				
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
	
	public void closeLease(int leaseId){
		
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
	
	public ByteArrayOutputStream getLeaseAgreement(int leaseId){

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		Document doc = new Document();
		
		//create some special styles and font sizes
		Font h1 = new Font(FontFamily.TIMES_ROMAN, 28, Font.BOLD, BaseColor.BLACK);
		Font h2 = new Font(FontFamily.TIMES_ROMAN, 20, Font.BOLD, BaseColor.BLACK);
		Font h3 = new Font(FontFamily.HELVETICA, 16, Font.NORMAL, BaseColor.BLACK);
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		try{
			String sqlGetLeaseData = "SELECT tb1.*, tb2.user_full_name as requestor, tb3.user_full_name as owner, tb4.* FROM leases tb1 INNER JOIN users tb2 ON tb1.lease_requser_id=tb2.user_id INNER JOIN users tb3 ON tb1.lease_user_id=tb3.user_id INNER JOIN items tb4 ON tb1.lease_item_id=tb4.item_id WHERE tb1.lease_id=?";
			ps1 = hcp.prepareStatement(sqlGetLeaseData);
			ps1.setInt(1, leaseId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				
				PdfWriter.getInstance(doc, output);
				
				//document header properties
				doc.addAuthor("Blue Marble");
				doc.addCreationDate();
				doc.addCreator("frrndlease.com");
				doc.addTitle("Lease Agreement");
				doc.setPageSize(PageSize.A4);
				doc.open();
				
				//Add Image
			    Image image = Image.getInstance(new URL(LOGO_URL));
			    //Fixed Positioning
			    image.setAlignment(Element.ALIGN_MIDDLE);
			    //Scale to new height and new width of image
			    image.scaleAbsolute(50, 50);
			    //Add to document
			    doc.add(image);
				
				// Heading
				Paragraph text = new Paragraph("Lease Agreement\nBetween", h1);
				text.setAlignment(Element.ALIGN_CENTER);
				doc.add(text);
				
				// The owner and the requestor
		        PdfPTable table = new PdfPTable(3); // 3 columns.
		        table.setWidthPercentage(100); //Width 100%
		        table.setSpacingBefore(10f); //Space before table
		        table.setSpacingAfter(10f); //Space after table
		 
		        //Set Column widths
		        float[] columnWidths = {1f, 1f, 1f};
		        table.setWidths(columnWidths);
		 
		        PdfPCell cell1 = new PdfPCell();
		        image = Image.getInstance(new URL(USER_ICON));
		        image.setAlignment(Element.ALIGN_MIDDLE);
		        image.scaleAbsolute(50, 50);
		        cell1.addElement(image);
		        text = new Paragraph(rs1.getString("owner")+"\n(Owner)", h3);
		        text.setAlignment(Element.ALIGN_CENTER);
		        cell1.addElement(text);
		        cell1.setBorderColor(BaseColor.WHITE);
		        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
		        cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
		 
		        PdfPCell cell2 = new PdfPCell(new Paragraph("&", h1));
		        cell2.setBorderColor(BaseColor.WHITE);
		        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
		        cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
		 
		        PdfPCell cell3 = new PdfPCell();
		        image = Image.getInstance(new URL(USER_ICON));
		        image.setAlignment(Element.ALIGN_MIDDLE);
		        image.scaleAbsolute(50, 50);
		        cell3.addElement(image);
		        text = new Paragraph(rs1.getString("requestor")+"\n(Requestor)", h3);
		        text.setAlignment(Element.ALIGN_CENTER);
		        cell3.addElement(text);
		        cell3.setBorderColor(BaseColor.WHITE);
		        cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
		        cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
		 
		        table.addCell(cell1);
		        table.addCell(cell2);
		        table.addCell(cell3);
		 
		        doc.add(table);
		        
		        text = new Paragraph("For Item", h2);
		        text.setAlignment(Element.ALIGN_CENTER);
		        text.setPaddingTop(5);
		        doc.add(text);
		        
		        if(rs1.getString("item_primary_image_link") != null){
			        Image imageLink = Image.getInstance(new URL(rs1.getString("item_primary_image_link")));
			        imageLink.scaleAbsolute(100, 100);
			        doc.add(imageLink);
		        }
		        
		        text = new Paragraph("Title: " + rs1.getString("item_name"), h3);
		        text.setPaddingTop(5);
		        doc.add(text);
		        
		        text = new Paragraph("Description: " + rs1.getString("item_desc"), h3);
		        text.setPaddingTop(5);
		        doc.add(text);
		        
		        text = new Paragraph("Insurance Amount - Rs." + rs1.getString("item_lease_value"), h3);
		        text.setPaddingTop(5);
		        doc.add(text);
		        
		        text = new Paragraph("Duration of lease: ", h2);
		        text.setAlignment(Element.ALIGN_CENTER);
		        text.setPaddingTop(10);
		        doc.add(text);
		        
		        text = new Paragraph("Lease Term - " + rs1.getString("item_lease_term"), h3);
		        text.setPaddingTop(5);
		        doc.add(text);
		        
		        text = new Paragraph("Expiry Date - " + rs1.getString("lease_expiry_date"), h3);
		        text.setPaddingTop(5);
		        doc.add(text);
		        
		        text = new Paragraph("Cost of lease: ", h2);
		        text.setAlignment(Element.ALIGN_CENTER);
		        text.setPaddingTop(10);
		        doc.add(text);
		        
		        text = new Paragraph("Credits - 10", h3);
		        text.setPaddingTop(5);
		        doc.add(text);
		        
		        doc.close();
		        
			}else{
				return null;
			}
	        
		}catch(DocumentException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return output;
	}
	
}
