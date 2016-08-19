package util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.bind.DatatypeConverter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.json.JSONObject;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

import connect.Connect;
import util.Event.Notification_Type;

public class FlsEmail extends Connect{

	private FlsLogger LOGGER = new FlsLogger(FlsEmail.class.getName());
	
	public boolean sendEmail(JSONObject obj, Notification_Type notificationType){

		String ENV_CONFIG = FlsConfig.env;
		String URL = FlsConfig.prefixUrl;
		
		String FROM = "BlueMarble@frrndlease.com", CC = "BlueMarble@frrndlease.com", TO, PREFIX, SUBJECT, BODY;
		String EMAIL_VERIFICATION_URL,EMAIL_INVITATION_URL,EMAIL_FORGOT_PASSWORD,EMAIL_ITEM_DETAILS;
		
		int credits;
		
		// this variable is used to store the image
		File imageFile = null;
		
		// this variable is used to store list of files
		List<File> imageFiles = new ArrayList<>();
		
		EMAIL_VERIFICATION_URL = URL + "/emailverification.html";
		EMAIL_INVITATION_URL = URL + "/ref_token=";
		EMAIL_FORGOT_PASSWORD = URL + "/forgotpassword.html";
		EMAIL_ITEM_DETAILS = URL + "/ItemDetails?uid=";
		
		if (ENV_CONFIG.equals("dev"))
			PREFIX = "[FrrndLease-Test]";
		else
			PREFIX = "[FrrndLease]";
		
		try{
			
			TO = obj.getString("toUserId");
			
			credits = getCurrentCredits(TO);
			
			// starting velocity engine
			VelocityEngine ve = new VelocityEngine();
			ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
			ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
			ve.init();
			
			// Initializing context for generating template
			VelocityContext context = new VelocityContext();
			
			// declaring template to get template for each email type from templates folder
			Template template = null;
			
			// the generated text from the template is written on the writer and is set in the body
			StringWriter writer = new StringWriter();
			
			// Build Email Subject and Body
			switch (notificationType) {
			case FLS_MAIL_FORGOT_PASSWORD:
				SUBJECT = " Forgot Password";
				BODY = "Click on this link to reset password for your frrndlease account. <br/>"
						+ "<a href='" + EMAIL_FORGOT_PASSWORD + "?act=" + obj.getString("toUserActivation") + "'>"
						+ EMAIL_FORGOT_PASSWORD + "?act=" + obj.getString("toUserActivation") + "</a>";
				break;
			case FLS_MAIL_SIGNUP_VALIDATION:
				SUBJECT = " Email Verification";
				BODY = "Hello " + obj.getString("toUserName")
						+ ". You have successfully signed up on fRRndLease. To start using frrndlease "
						+ "you need to activate your account. Click on this link to activate your frrndlease account. <br/>"
						+ "<a href='" + EMAIL_VERIFICATION_URL + "?token=" + obj.getString("toUserActivation") + "'>"
						+ EMAIL_VERIFICATION_URL + "?token=" + obj.getString("toUserActivation") + "</a>" + "<br/>";
				break;
			case FLS_MAIL_REGISTER:
				SUBJECT = " Welcome Aboard";
				BODY = "Hello " + obj.getString("toUserName")
						+ ". You have successfully signed up on fRRndLease, the platform that helps you Make Space for leading the Life you love. <br/>"
						+ "We love our stuff, but are passionate about utilizing them well to get rich, positive experiences. <br/>"
						+ "Check out and follow us on our Facebook community page at <a href='http://www.facebook.com/frrndlease'>frrndlease on facebook</a>. "
						+ "We use that to make announcements and share posts.<br/>"
						+ "We are happy to have you. Now go and offer your dormant stuff on the platform!! <br/>"
						+ "Remember, it will always be yours ... and will come back after enriching your friends, whenever you want!";
				break;

			case FLS_MAIL_DELETE_ITEM:
				SUBJECT = (" Your Item [" + obj.getString("title") + "] has been deleted from the Friend Store");
				BODY = ("<body>You have deleted the following item on fRRndLease<br/> <br/>" + " Title : " + obj.getString("title")
						+ "<br/>" + " Category : " + obj.getString("category") + "<br/>" + " Description : "
						+ obj.getString("description") + "<br/>" + " Lease Value : " + obj.getInt("leaseValue") + "<br/>"
						+ " Lease Term : " + obj.getString("leaseTerm") + "<br/>" + " Status : " + obj.getString("itemStatus") + "<br/>"
						+ "<img style=\"width:50%;\" src=\"cid:image\" alt=" + obj.getString("title") + " ></img>" + "</body>");
				imageFile = convertBinaryToImage(obj.getString("image"));
				break;

			case FLS_MAIL_POST_ITEM:
				SUBJECT = (" Your Item [" + obj.getString("title") + "] has been added to the Friend Store");
				BODY = ("<body>You have added the following item on fRRndLease <br/> <br/>" + " Title : " + obj.getString("title")
						+ "<br/>" + " Category : " + obj.getString("category") + "<br/>" + " Description : " + obj.getString("description")
						+ "<br/>" + " Lease Value : " + obj.getInt("leaseValue") + "<br/>" + " Lease Term : "
						+ obj.getString("leaseTerm") + "<br/>" + " Status : " + obj.getString("itemStatus") + "<br/>"
						+ "<img style=\"width:50%;\" src=\"cid:image\" alt=" + obj.getString("title") + " ></img>" + "</body>");
				imageFile = convertBinaryToImage(obj.getString("image"));
				break;

			case FLS_MAIL_MATCH_WISHLIST_ITEM:
				SUBJECT = (" Item [" + obj.getString("title") + "] has been added to the Friend Store");
				BODY = ("<body>Someone has posted this item that matches your wishlist. <br/> <br/>" + " Title : "
						+ obj.getString("title") + "<br/>" + " Category : " + obj.getString("category") + "<br/>"
						+ " Description : " + obj.getString("description") + "<br/>" + " Lease Value : "
						+ obj.getInt("leaseValue") + "<br/>" + " Lease Term : " + obj.getString("leaseTerm") + "<br/>"
						+ " Status : " + obj.getString("itemStatus") + "<br/>" + "<img style=\"width:50%;\" src=\"cid:image\" alt=" + obj.getString("title")
						+ " ></img>" + "</body>");
				imageFile = convertBinaryToImage(obj.getString("image"));
				break;

			case FLS_MAIL_MATCH_POST_ITEM:
				MatchItems matchItems = new MatchItems();
				List<JSONObject> listItems = matchItems.checkPostedItems(obj.getInt("itemId"));
				SUBJECT = (" Items present in the Friend Store match your wishlist");
				BODY = ("<body>These items match your wishlist. <br/> <br/>");

				int i = 0;
				
				for (JSONObject l : listItems) {
					BODY = BODY + (" Title : " + l.getString("title") + "<br/>" + " Category : "
							+ l.getString("category") + "<br/>" + " Description : "
							+ l.getString("description") + "<br/>" + " Lease Value : "
							+ l.getInt("leaseValue") + "<br/>" + " Lease Term : "
							+ l.getString("leaseTerm") + "<br/>" + " Status : " + l.getString("status")
							+ "<br/>" + "<img style=\"width:50%;\" src=\"cid:image" + Integer.toString(i) + "\" alt="
							+ l.getString("title") + " ></img><br/><br/>");
					i++;
					imageFiles.add(convertBinaryToImage(l.getString("image")));
				}

				BODY = BODY + ("</body>");

				break;

			case FLS_MAIL_ADD_FRIEND_FROM:
				SUBJECT = (" Your Friend '" + obj.getString("fromUserId")	+ "' has been added to your Friend List. ");
				BODY = "You have added '" + obj.getString("fromUserId") + "' to your Friend List. You can now lease items to each other ";
				break;

			case FLS_MAIL_ADD_FRIEND_TO:
				SUBJECT = (" Your Friend '" + obj.getString("fromUserId") + "' has added you to their Friend List");
				BODY = "You are now in '" + obj.getString("fromUserId") + "'\'s Friend List. You can now lease items to each other <br/> <br/>"
						+ "Click here to Sign Up "+EMAIL_INVITATION_URL+obj.getString("fromUserRefferalCode");
				break;

			case FLS_MAIL_DELETE_FRIEND_FROM:
				SUBJECT = (" Your Friend \'" + obj.getString("fromUserId") + "' has been removed from your Friend List");
				BODY = "You have now removed " + obj.getString("fromUserId")
						+ " from your Friend List. You can no longer lease items to each other. Tell us what went wrong! ";
				break;

			case FLS_MAIL_DELETE_FRIEND_TO:
				SUBJECT = (" Your Friend '" + obj.getString("fromUserId") + "' removed you from thier Friend List");
				BODY = "You have been removed from the Friend List of your Friend " + obj.getString("fromUserId")
						+ ". You can no longer lease items to each other. Tell us what went wrong! ";
				break;

			case FLS_MAIL_REJECT_REQUEST_FROM:
				SUBJECT = (" Request removed");
				BODY = "Request for item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> has been removed as a lease might be granted. ";
				break;

			case FLS_MAIL_REJECT_REQUEST_TO:
				SUBJECT = (" Request removed");
				BODY = "Request of item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> has been removed by the owner as a lease might be granted. ";
				break;

			case FLS_MAIL_DELETE_REQUEST_FROM:
				SUBJECT = (" Request removed");
				BODY = "Your Request for item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> has been removed. ";
				break;

			case FLS_MAIL_DELETE_REQUEST_TO:
				SUBJECT = (" Request removed");
				BODY = "Request of item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> has been removed a Requestor. ";
				break;

			case FLS_MAIL_GRANT_LEASE_FROM:
				SUBJECT = (" Lease granted to user [" + obj.getString("fromUserId") + "]");
				BODY = "You have sucessfully leased an item to [" + obj.getString("fromUserId") + "] on Friend Lease - ";
				break;

			case FLS_MAIL_GRANT_LEASE_TO:
				SUBJECT = (" Lease granted to you by [" + obj.getString("fromUserId") + "]");
				BODY = "An item has been leased by [" + obj.getString("fromUserId") + "] to you on Friend Lease - ";
				break;

			case FLS_MAIL_REJECT_LEASE_FROM:
				SUBJECT = (" Lease Cancelled to user [" + obj.getString("fromUserId") + "]");
				BODY = "You have closed lease of item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> and leasee ["
						+ obj.getString("fromUserId") + "] on Friend Lease - ";
				break;

			case FLS_MAIL_REJECT_LEASE_TO:
				SUBJECT = (" Lease Closed by the Owner");
				BODY = "Lease has been closed by the Owner for the item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> ";
				break;
				
			case FLS_MAIL_GRACE_PERIOD_OWNER:
				SUBJECT = (" Reminder to Renew Lease to user [" + obj.getString("fromUserId") + "]");
				BODY = "Less than 5 days left for lease to close.Please consider renewing the lease of item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> and leasee [" + obj.getString("fromUserId") + "] on Friend Lease - ";
				break;

			case FLS_MAIL_GRACE_PERIOD_REQUESTOR:
				SUBJECT = (" Reminder to Renew Lease");
				BODY = "Less than 5 days left for lease to close. Please consider renewing the lease of item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> ";
				break;
				
			case FLS_MAIL_RENEW_LEASE_OWNER:
				SUBJECT = (" Renewed Lease to user [" + obj.getString("fromUserId") + "]");
				BODY = "Lease has been renewed for item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> and leasee [" 
						+ obj.getString("fromUserId") + "] on Friend Lease - ";
				break;

			case FLS_MAIL_RENEW_LEASE_REQUESTOR:
				SUBJECT = (" Lease Renewed");
				BODY = "Lease has been renewed by the owner of item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> ";
				break;

			case FLS_MAIL_MAKE_REQUEST_FROM:
				SUBJECT = (" Item Requested");
				BODY = "You have sucessfully Requested an item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> on Friend Lease - ";
				break;

			case FLS_MAIL_MAKE_REQUEST_TO:
				SUBJECT = (" Item Requested");
				BODY = "Your Item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> has been requested on Friend Lease - ";
				break;

			default:
				SUBJECT = (" Default Subject");
				BODY = "Default Message ... Contact us, you should never get this! ";
				break;
			}
			
			// getting a default session
			Session session = Session.getDefaultInstance(new Properties());

			// mime message type from javax.mail library
			MimeMessage message = new MimeMessage(session);
			message.setSubject(PREFIX + SUBJECT, "UTF-8");

			// setting the basic properties of the email message
			message.setFrom(new InternetAddress(FROM));
			message.setReplyTo(new Address[] { new InternetAddress(FROM) });
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(TO));
			if (!ENV_CONFIG.equals("dev")) {
				message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(CC));
			}

			Multipart multipart = new MimeMultipart("related");

			template = ve.getTemplate("templates/defaultEmail.vm");
			if(credits != -1)
				context.put("credits", credits);
			context.put("subject", SUBJECT);
			context.put("body", BODY);
						
			BodyPart body = new MimeBodyPart();
						
			multipart.addBodyPart(body);
						
			InputStream logoUrl = getClass().getClassLoader().getResourceAsStream("images/fls-logo-min.png");
			MimeBodyPart logo = new MimeBodyPart();
			logo.attachFile(createFile(logoUrl));
			logo.setContentID("<logo>");
			logo.setDisposition(MimeBodyPart.INLINE);
			multipart.addBodyPart(logo);
			            
			if (imageFiles.isEmpty()) {
				if (imageFile != null) {
					MimeBodyPart imagePart = new MimeBodyPart();
					LOGGER.warning("Sending Image!!");
					imagePart.attachFile(imageFile);
					imagePart.setContentID("<image>");
					imagePart.setDisposition(MimeBodyPart.INLINE);
					multipart.addBodyPart(imagePart);
					imageFile = null;
					}
				} else {
					int len = imageFiles.size();
					for (int j = 0; j < len; j++) {
						// Image part if the message has an image
						if(imageFiles.get(j) != null){
							MimeBodyPart imagePart = new MimeBodyPart();
							LOGGER.warning("Sending Image!!");
							imagePart.attachFile(imageFiles.get(j));
							imagePart.setContentID("<image" + Integer.toString(j) + ">");
							imagePart.setDisposition(MimeBodyPart.INLINE);
							multipart.addBodyPart(imagePart);
							imageFile = null;
						}
					}
				}
			           
				template.merge(context, writer);
				
			    /* you can add html tags in your text to decorate it. */
			    body.setContent(writer.toString(), "text/html");
			      
				message.setContent(multipart, "text/html");

				// converting the mime message into ram message
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				message.writeTo(outputStream);
				RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

				// Assemble the email.
				SendRawEmailRequest request = new SendRawEmailRequest(rawMessage);

				try {
					BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAITVFAR4O56SFRG6A", "F1M+ak2jT+qmFygNtXmwuomqsDpA8ZaNy/GBviF/");
					// Instantiate an Amazon SES client, which will make the service
					// call with the supplied AWS credentials.
					AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient(credentials);
					
					Region REGION = Region.getRegion(Regions.US_WEST_2);
					client.setRegion(REGION);

					// Send the email.
					client.sendRawEmail(request);
					LOGGER.warning("====> Email sent!");
					
					return true;
					
				} catch (Throwable e) {
					LOGGER.warning("====> " + e.getMessage());
					e.printStackTrace(System.out);
				}			
			
		}catch(Exception e){
			LOGGER.warning("====> The email was not sent.");
			e.printStackTrace();
		}
		
		return false;
		
	}
	
	private File convertBinaryToImage(String imageString) {
		// TODO Auto-generated method stub
		
		if(imageString == null || imageString == ""){
			return null;
		}
		
		try {
			String[] i = imageString.split(",");
			String binary = i[1];

			BufferedImage image = null;
			byte[] imageByte;

			imageByte = DatatypeConverter.parseBase64Binary(binary);
			ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
			image = ImageIO.read(bis);
			bis.close();

			// write the image to a file
			File file = new File("ItemImage.png");
			ImageIO.write(image, "png", file);

			return file;
		} catch (IOException e) {
			LOGGER.warning("Not able to decode the image");
			e.printStackTrace();
		}
		
		return null;
	}
	
	public File createFile(InputStream is) throws IOException {
	    File tmp = null;
	    FileOutputStream tmpOs = null;
	    try {
	        tmp = File.createTempFile("xml", "tmp");
	        tmpOs = new FileOutputStream(tmp);
	        int len = -1;
	        byte[] b = new byte[4096];
	        while ((len = is.read(b)) != -1) {
	            tmpOs.write(b, 0, len);
	        }
	    } finally {
	        try { is.close(); } catch (Exception e) {}
	        try { tmpOs.close(); } catch (Exception e) {}
	    }
	    return tmp;
	}
	
	private int getCurrentCredits(String userId){
		int credit = -1;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		
		try{
			// getting the credits of the user
			String sqlGetCredit = "SELECT user_credit FROM users WHERE user_id=?";
			ps = hcp.prepareStatement(sqlGetCredit);
			ps.setString(1, userId);
			rs = ps.executeQuery();
			if (rs.next()) {
				credit = rs.getInt("user_credit");
			}
		} catch (SQLException e) {
			LOGGER.warning("Not able to get credits");
			e.printStackTrace();
		} finally {
			try { 
				if(rs != null)rs.close();
				if(ps != null)ps.close();
				if(hcp != null)hcp.close(); 
			} catch(SQLException e) {}
		}
		
		return credit;
	}
	
}
