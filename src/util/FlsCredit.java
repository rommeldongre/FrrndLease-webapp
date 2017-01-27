package util;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import connect.Connect;
import pojos.PromoCodeModel.Code_Type;
import util.Event.Event_Type;
import util.Event.Notification_Type;

public class FlsCredit extends Connect {

	private FlsLogger LOGGER = new FlsLogger(FlsCredit.class.getName());

	private String URL = FlsConfig.prefixUrl;
	
	int CREDIT_VALUE = FlsConfig.creditValue;
	int MEMBER_VALUE = FlsConfig.memberValue;
	
	public enum Credit{
		ADD,
		SUB
	}

	public void logCredit(String userId, int credits, String type, String description, Credit creditType) {

		LOGGER.info("Inside logCredit Method");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		int rs1, rs2;

		try {
			String sqlCreateCreditLog = "insert into credit_log (credit_user_id, credit_amount, credit_type, credit_desc) values (?,?,?,?)";
			ps1 = hcp.prepareStatement(sqlCreateCreditLog);
			ps1.setString(1, userId);
			if(creditType == Credit.ADD)
				ps1.setInt(2, credits);
			else
				ps1.setInt(2, -credits);
			ps1.setString(3, type);
			ps1.setString(4, description);

			rs1 = ps1.executeUpdate();

			if (rs1 == 1) {
				
				String sql = "UPDATE users SET ";
				if(creditType == Credit.ADD){
					LOGGER.info("Adding " + credits + " Credits to the userId : " + userId);
					sql = sql + "user_credit=user_credit+" + credits;
				} else {
					LOGGER.info("Subtracting " + credits + " Credits from the userId : " + userId);
					sql = sql + "user_credit=user_credit-" + credits;
				}
				sql = sql + " WHERE user_id=?";
				
				ps2 = hcp.prepareStatement(sql);
				ps2.setString(1, userId);
				
				rs2 = ps2.executeUpdate();
				
				if(rs2 == 1){
					if(creditType == Credit.ADD){
						LOGGER.info("Added credits to users account.");
					}else{
						LOGGER.info("Subtracted credits from users account.");
					}
				} else {
					LOGGER.info("Not able to add or subtract credits");
				}
				
			} else {
				LOGGER.info("Not able to log credit for the userId : " + userId);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(ps2 != null) ps2.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public int addRefferalCredits(String referrerCode){
		
		LOGGER.info("Inside addRefferalCredits Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		try {
			
			if(referrerCode != null){
				
				String sqlAddReferrerCredits = "SELECT user_id FROM users WHERE user_referral_code=?";
				ps1 = hcp.prepareStatement(sqlAddReferrerCredits);
				ps1.setString(1, referrerCode);
				rs1 = ps1.executeQuery();
				
				if(rs1.next()){
					LOGGER.info("ReferrerCode belongs to the userId - " + rs1.getString("user_id"));
					logCredit(rs1.getString("user_id"), 10, "Referred for sign up", "", Credit.ADD);
					return 1;
				}else{
					LOGGER.info("Not able to find the referrerCode - " + referrerCode);
				}
				
			}else{
				LOGGER.info("referrerCode is null");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return 0;
	}

	public int getCreditLogId(String userId, String promoCode) {
		
		LOGGER.info("Inside getCreditLogId Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		int id = -1;
		
		try {
			
			String sqlGetCreditLogId = "SELECT credit_log_id FROM credit_log WHERE credit_user_id=? AND credit_desc=? ORDER BY credit_date DESC LIMIT 1";
			ps1 = hcp.prepareStatement(sqlGetCreditLogId);
			ps1.setString(1, userId);
			ps1.setString(2, promoCode);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				id = rs1.getInt("credit_log_id");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return id;
	}

	public void addOrder(String userId, int amount, String promoCode, String razorPayId, int creditLogId, Code_Type flsInternal) {
		
		LOGGER.info("Inside addOrder Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int rs1;
		
		try {
			
			String sqlInsertOrder = "INSERT INTO `orders` (`order_user_id`, `amount`, `promo_code`, `razor_pay_id`, `credit_log_id`, `order_type`) VALUES (?, ?, ?, ?, ?, ?)";
			ps1 = hcp.prepareStatement(sqlInsertOrder);
			ps1.setString(1, userId);
			ps1.setInt(2, amount);
			ps1.setString(3, promoCode);
			ps1.setString(4, razorPayId);
			ps1.setInt(5, creditLogId);
			ps1.setString(6, flsInternal.name());
			
			rs1 = ps1.executeUpdate();
			
			if(rs1 == 1){
				LOGGER.info("New order created for the userId - " + userId + " and creditLogId - " + creditLogId);
			}else{
				LOGGER.info("Not able to create a new order");
			}		
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	public int getCurrentCredits(String userId) {
		
		LOGGER.info("Inside getCurrentCredits Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		try {
			
			String sqlGetCurrentCredits = "SELECT user_credit FROM users WHERE user_id=?";
			ps1 = hcp.prepareStatement(sqlGetCurrentCredits);
			ps1.setString(1, userId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				return rs1.getInt("user_credit");
			}	
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return 0;
	}
	
	public boolean expired(Date expiry) {

		int result = 0;

		try {
			Date current = new Date();
			current.setTime(0);
			result = current.compareTo(expiry);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}

		if (result <= 0)
			return false;
		else
			return true;
	}
	
	public void updateMembership(int orderId, String userId, int amountPaid, int promoCredits){
		
		LOGGER.info("Inside updateMembership Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null;
		int rs2;
		
		try {
			
			int totalAmount = amountPaid + promoCredits * CREDIT_VALUE;
			
			int monthsEarned = (Integer) totalAmount / MEMBER_VALUE;
			
			String sqlGetFeeExpiryDate = "SELECT user_fee_expiry FROM users WHERE user_id=?";
			ps1 = hcp.prepareStatement(sqlGetFeeExpiryDate);
			ps1.setString(1, userId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				LOGGER.info("Got user fee expiry date for the userId - " + userId + " as - " + rs1.getString("user_fee_expiry"));
				String sqlUpdateFeeExpiry = "UPDATE `users` SET user_fee_expiry=";
				if(rs1.getString("user_fee_expiry") == null){
					sqlUpdateFeeExpiry = sqlUpdateFeeExpiry + "DATE_ADD(CURRENT_TIMESTAMP, INTERVAL ? YEAR_MONTH) WHERE user_id=?";
				}else{
					sqlUpdateFeeExpiry = sqlUpdateFeeExpiry + "DATE_ADD(user_fee_expiry, INTERVAL ? YEAR_MONTH) WHERE user_id=?";
				}
				ps2 = hcp.prepareStatement(sqlUpdateFeeExpiry);
				ps2.setInt(1, monthsEarned);
				ps2.setString(2, userId);
				
				rs2 = ps2.executeUpdate();
				
				if(rs2 == 1){
					LOGGER.info("Updated user fee expiry by " + monthsEarned + " months for the userId - " + userId);
				}else{
					LOGGER.info("Not able to update user fee expiry by " + monthsEarned + " months for the userId - " + userId);
				}
			}else{
				LOGGER.info("Not able to get user fee expiry date for the userId - " + userId);
			}
			
			if(orderId != -1){
				try {
					Event event = new Event();
					event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MEMBERSHIP_INVOICE, 0, 
					  "Congratulations! You have bought membership for " + monthsEarned + " months."
					+ " Get an Invoice for the same <form action=\"" + URL + "/GetOrderInvoice\" method=\"POST\" target=\"_blank\">"
	                + "<input type=\"hidden\" name=\"orderId\" value=\"" + orderId + "\" />"
	                + "<input type=\"submit\" style=\"background-color:#1D62F0\" value=\"Get Invoice\" /></form>");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(ps2 != null) ps2.close();
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public ByteArrayOutputStream getOrderInvoice(int orderId){

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		Document doc = new Document();
		
		//create some special styles and font sizes
		Font h1 = new Font(FontFamily.TIMES_ROMAN, 28, Font.BOLD, BaseColor.BLACK);
		Font h2 = new Font(FontFamily.TIMES_ROMAN, 20, Font.BOLD, BaseColor.BLACK);
		Font h3 = new Font(FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.BLACK);
		Font h4 = new Font(FontFamily.HELVETICA, 14, Font.NORMAL, BaseColor.BLACK);
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null,ps2=null;
		ResultSet rs1 = null,rs2 = null;
		String credit_type ="";
		String membership="membership",order_date=null;
		final String LOGO_URL = "https://s3-ap-south-1.amazonaws.com/fls-meta/fls-logo.png";
		
		int quantity=0,amount_payable=0,amount_total=0,credits_count=0,discount_value=0,rate=0,internet_charges=0,service_tax=0;
		String activity="";
		
		int CREDIT_VALUE = FlsConfig.creditValue;
		int MEMBER_VALUE = FlsConfig.memberValue;
		FlsPlan flsP = new FlsPlan();
		
		try{
			String sqlGetLeaseData = "SELECT tb_orders.order_id , tb_orders.order_date, tb_orders.order_user_id, tb_users.user_full_name, tb_users.user_location, tb_orders.amount, tb_orders.promo_code, tb_orders.razor_pay_id, tb_orders.credit_log_id, tb_orders.order_type, tb_credits.credit_type FROM `orders` tb_orders INNER JOIN users tb_users ON tb_orders.order_user_id = tb_users.user_id INNER JOIN credit_log tb_credits ON tb_orders.credit_log_id = tb_credits.credit_log_id WHERE tb_orders.order_id=?";
			ps1 = hcp.prepareStatement(sqlGetLeaseData);
			ps1.setInt(1, orderId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){				
				amount_total = rs1.getInt("amount");
				credit_type= rs1.getString("credit_type").toLowerCase();
				order_date = flsP.formattedDate(rs1.getString("order_date"));
				
				if(!rs1.getString("promo_code").equals("")){
					
					String sqlGetPromovalue = "SELECT credit FROM `promo_credits` WHERE code=?";
					ps2 = hcp.prepareStatement(sqlGetPromovalue);
					ps2.setString(1, rs1.getString("promo_code"));
					
					rs2 = ps2.executeQuery();
					
					if(rs2.next()){
						credits_count = rs2.getInt("credit");
						discount_value = credits_count*CREDIT_VALUE;
						amount_payable = discount_value+ amount_total;
					}
				}else{
					amount_payable = amount_total;
				}
				
				if(credit_type.contains(membership)){
					activity = "Uber Membership";
					quantity = amount_payable/MEMBER_VALUE;
					rate = MEMBER_VALUE;
				}else{
					activity = "Credits Bought";
					quantity = amount_payable/CREDIT_VALUE;
					rate = CREDIT_VALUE;
				}
				
				
				
				PdfWriter.getInstance(doc, output);
				
				//document header properties
				doc.addAuthor("Blue Marble");
				doc.addCreationDate();
				doc.addCreator("frrndlease.com");
				doc.addTitle("Order Invoice");
				doc.setPageSize(PageSize.A4);
				doc.open();
				
				
				//Add Image
			    Image image = Image.getInstance(new URL(LOGO_URL));
			    //Fixed Positioning
			    image.setAlignment(Element.ALIGN_CENTER);
			    //Scale to new height and new width of image
			    image.scaleAbsolute(50, 50);
			    //Add to document
			    doc.add(image);
			    
			    doc.add( new Chunk("\n\n\n") );
			    
			    
			    // Heading
			     Paragraph text = new Paragraph("Invoice", h1);
				 text.setAlignment(Element.ALIGN_LEFT);
			     doc.add(text);
			    					
				
				// The Billing and Shipping Address
		        PdfPTable table = new PdfPTable(3); // 3 columns.
		        table.setWidthPercentage(100); //Width 100%
		        table.setSpacingBefore(20f); //Space before table
		        table.setSpacingAfter(10f); //Space after table
		 
		        //Set Column widths
		        float[] columnWidths = {1f, 1f, 1f};
		        table.setWidths(columnWidths);
		 
		        PdfPCell cell1 = new PdfPCell();
		        text = new Paragraph("Bill To:", h3);
		        text.setAlignment(Element.ALIGN_CENTER);
		        cell1.addElement(text);
		        
		        text = new Paragraph(rs1.getString("user_full_name")+"\n"+rs1.getString("user_location"), h4);
		        text.setAlignment(Element.ALIGN_CENTER);
		        cell1.addElement(text);
		        
		        cell1.setBorderColor(BaseColor.WHITE);
		        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
		        cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
		 
		        
		        PdfPCell cell2 = new PdfPCell();
		        text = new Paragraph("Ship To:", h3);
		        text.setAlignment(Element.ALIGN_CENTER);
		        cell2.addElement(text);
		        
		        text = new Paragraph(rs1.getString("user_full_name")+"\n"+rs1.getString("user_location"), h4);
		        text.setAlignment(Element.ALIGN_CENTER);
		        cell2.addElement(text);
		        
		        cell2.setBorderColor(BaseColor.WHITE);
		        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
		        cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
		 
		        PdfPCell cell3 = new PdfPCell();
		        text = new Paragraph("Invoice Id#: "+rs1.getString("order_id")+"\nOrder Date: "+order_date, h4);
		        text.setAlignment(Element.ALIGN_LEFT);
		        cell3.addElement(text);
		        
		        cell3.setBorderColor(BaseColor.WHITE);
		        cell3.setPaddingLeft(30);
		        cell3.setHorizontalAlignment(Element.ALIGN_LEFT);
		        cell3.setVerticalAlignment(Element.ALIGN_TOP);
		 
		        table.addCell(cell1);
		        table.addCell(cell2);
		        table.addCell(cell3);
		 
		        doc.add(table);
		        
		        
		        // The Order Details
		        PdfPTable table1= new PdfPTable(4); // 4 columns.
		        table1.setWidthPercentage(100); //Width 100%
		        table1.setSpacingBefore(20f); //Space before table
		        table1.setSpacingAfter(10f); //Space after table
		 
		        //Set Column widths
		        float[] order_columnWidths = {1f, 1f, 1f, 1f};
		        table1.setWidths(order_columnWidths);
		 
		        PdfPCell cell4 = new PdfPCell();
		        text = new Paragraph("Activity", h3);
		        text.setAlignment(Element.ALIGN_CENTER);
		        cell4.addElement(text);
		        
		        text = new Paragraph(activity, h4);
		        text.setAlignment(Element.ALIGN_CENTER);
		        cell4.addElement(text);
		        
		        cell4.setBorderColor(BaseColor.WHITE);
		        cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
		        cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
		 
		        
		        PdfPCell cell5 = new PdfPCell();
		        text = new Paragraph("Quantity", h3);
		        text.setAlignment(Element.ALIGN_CENTER);
		        cell5.addElement(text);
		        
		        text = new Paragraph(String.valueOf(quantity), h4);
		        text.setAlignment(Element.ALIGN_CENTER);
		        cell5.addElement(text);
		        
		        cell5.setBorderColor(BaseColor.WHITE);
		        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
		        cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
		 
		        PdfPCell cell6 = new PdfPCell();
		        text = new Paragraph("Rate", h3);
		        text.setAlignment(Element.ALIGN_CENTER);
		        cell6.addElement(text);
		        
		        text = new Paragraph("Rs "+String.valueOf(rate), h4);
		        text.setAlignment(Element.ALIGN_CENTER);
		        cell6.addElement(text);
		        
		        cell6.setBorderColor(BaseColor.WHITE);
		        cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
		        cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
		        
		       
		        PdfPCell cell7 = new PdfPCell();
			    text = new Paragraph("Amount", h3);
			    text.setAlignment(Element.ALIGN_CENTER);
			    cell7.addElement(text);
			        
			    text = new Paragraph("Rs "+String.valueOf(amount_payable), h4);
			    text.setAlignment(Element.ALIGN_CENTER);
			    cell7.addElement(text);
			        
			    cell7.setBorderColor(BaseColor.WHITE);
			    cell7.setHorizontalAlignment(Element.ALIGN_CENTER);
			    cell7.setVerticalAlignment(Element.ALIGN_MIDDLE);
		 
		        table1.addCell(cell4);
		        table1.addCell(cell5);
		        table1.addCell(cell6);
		        table1.addCell(cell7);
		 
		        doc.add(table1);
		        
		        
		        // The Summary Table
		        PdfPTable table2= new PdfPTable(2); // 2 columns.
		        table2.setWidthPercentage(50); //Width 50%
		        table2.setSpacingBefore(10f); //Space before table
		        table2.setSpacingAfter(10f); //Space after table
		 
		        //Set Column widths
		        float[] summary_columnWidths = {3f, 1f};
		        table2.setWidths(summary_columnWidths);
		 
		        PdfPCell cell8 = new PdfPCell();
		        text = new Paragraph("Sub Total", h3);
		        text.setAlignment(Element.ALIGN_CENTER);
		        cell8.addElement(text);
		        
		        cell8.setBorderColor(BaseColor.WHITE);
		        cell8.setHorizontalAlignment(Element.ALIGN_CENTER);
		        cell8.setVerticalAlignment(Element.ALIGN_MIDDLE);
		 
		        
		        PdfPCell cell9 = new PdfPCell();
		        text = new Paragraph("Rs "+String.valueOf(amount_payable), h4);
		        text.setAlignment(Element.ALIGN_CENTER);
		        cell9.addElement(text);
		        
		        cell9.setBorderColor(BaseColor.WHITE);
		        cell9.setHorizontalAlignment(Element.ALIGN_CENTER);
		        cell9.setVerticalAlignment(Element.ALIGN_MIDDLE);
		 
		        
		        PdfPCell cell10 = new PdfPCell();
		        text = new Paragraph("Discount", h3);
		        text.setAlignment(Element.ALIGN_CENTER);
		        
		        cell10.addElement(text);
		        cell10.setBorderColor(BaseColor.WHITE);
		        cell10.setHorizontalAlignment(Element.ALIGN_CENTER);
		        cell10.setVerticalAlignment(Element.ALIGN_MIDDLE);
		        
		        
		        PdfPCell cell11 = new PdfPCell();
			    text = new Paragraph("Rs "+discount_value, h4);
			    text.setAlignment(Element.ALIGN_CENTER);
			    cell11.addElement(text);
			        
			    cell11.setBorderColor(BaseColor.WHITE);
			    cell11.setHorizontalAlignment(Element.ALIGN_CENTER);
			    cell11.setVerticalAlignment(Element.ALIGN_MIDDLE);
			    
			    PdfPCell cell12 = new PdfPCell();
			    text = new Paragraph("Service Tax% ", h3);
			    text.setAlignment(Element.ALIGN_CENTER);
			    cell12.addElement(text);
			        
			    cell12.setBorderColor(BaseColor.WHITE);
			    cell12.setHorizontalAlignment(Element.ALIGN_CENTER);
			    cell12.setVerticalAlignment(Element.ALIGN_MIDDLE);
			    
			    PdfPCell cell13 = new PdfPCell();
			    text = new Paragraph("Rs "+String.valueOf(service_tax), h4);
			    text.setAlignment(Element.ALIGN_CENTER);
			    cell13.addElement(text);
			        
			    cell13.setBorderColor(BaseColor.WHITE);
			    cell13.setHorizontalAlignment(Element.ALIGN_CENTER);
			    cell13.setVerticalAlignment(Element.ALIGN_MIDDLE);
			    
			    PdfPCell cell14 = new PdfPCell();
			    text = new Paragraph("Internet Handling Charges  ", h3);
			    text.setAlignment(Element.ALIGN_CENTER);
			    cell14.addElement(text);
			        
			    cell14.setBorderColor(BaseColor.WHITE);
			    cell14.setHorizontalAlignment(Element.ALIGN_CENTER);
			    cell14.setVerticalAlignment(Element.ALIGN_MIDDLE);
			    
			    
			    PdfPCell cell15 = new PdfPCell();
			    text = new Paragraph("Rs "+String.valueOf(internet_charges), h4);
			    text.setAlignment(Element.ALIGN_CENTER);
			    cell15.addElement(text);
			        
			    cell15.setBorderColor(BaseColor.WHITE);
			    cell15.setHorizontalAlignment(Element.ALIGN_CENTER);
			    cell15.setVerticalAlignment(Element.ALIGN_MIDDLE);
			    
			    
			    PdfPCell cell16 = new PdfPCell();
			    text = new Paragraph("Total", h2);
			    text.setAlignment(Element.ALIGN_CENTER);
			    cell16.addElement(text);
			        
			    cell16.setBorderColor(BaseColor.WHITE);
			    cell16.setHorizontalAlignment(Element.ALIGN_CENTER);
			    cell16.setVerticalAlignment(Element.ALIGN_MIDDLE);
			    
			    
			    PdfPCell cell17 = new PdfPCell();
			    text = new Paragraph("Rs "+String.valueOf(amount_total), h4);
			    text.setAlignment(Element.ALIGN_CENTER);
			    cell17.addElement(text);
			        
			    cell17.setBorderColor(BaseColor.WHITE);
			    cell17.setHorizontalAlignment(Element.ALIGN_CENTER);
			    cell17.setVerticalAlignment(Element.ALIGN_MIDDLE);
			    
		        
			    table2.addCell(cell8);
		        table2.addCell(cell9);
		        table2.addCell(cell10);
		        table2.addCell(cell11);
		        table2.addCell(cell12);
		        table2.addCell(cell13);
		        table2.addCell(cell14);
		        table2.addCell(cell15);
		        table2.addCell(cell16);
		        table2.addCell(cell17);
		 
		        table2.setHorizontalAlignment(Element.ALIGN_RIGHT);
		        doc.add(table2);
		            
		        doc.close();
		        
			}else{
				return null;
			}
	        
		}catch(DocumentException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				if(rs1 != null) rs1.close();
				if(rs2 != null) rs2.close();
				if(ps1 != null) ps1.close();
				if(ps2 != null) ps2.close();
				if(hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return output;
	}
	
	public int getOrderId(int creditLogId) {
		
		LOGGER.info("Inside getOrderId Method");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		int id = -1;
		
		try {
			
			if(creditLogId == -1)
				return -1;
			
			String sqlGetOrderId = "SELECT order_id FROM orders WHERE credit_log_id=?";
			ps1 = hcp.prepareStatement(sqlGetOrderId);
			ps1.setInt(1, creditLogId);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				id = rs1.getInt("order_id");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return id;
	}

}
