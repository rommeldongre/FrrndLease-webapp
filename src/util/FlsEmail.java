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
		String EMAIL_VERIFICATION_URL,EMAIL_INVITATION_URL,EMAIL_FORGOT_PASSWORD,EMAIL_ITEM_DETAILS, EMAIL_PICKUP_CONFIRMATION,EMAIL_DELIVERY_PLAN;
		
		int credits;
		
		EMAIL_VERIFICATION_URL = URL + "/emailverification.html";
		EMAIL_INVITATION_URL = URL + "/ref_token=";
		EMAIL_FORGOT_PASSWORD = URL + "/forgotpassword.html";
		EMAIL_ITEM_DETAILS = URL + "/ItemDetails?uid=";
		EMAIL_PICKUP_CONFIRMATION = URL + "/confirmpickup.html";
		EMAIL_DELIVERY_PLAN = URL + "/deliveryplan.html";
		
		if (ENV_CONFIG.equals("dev"))
			PREFIX = "[FrrndLease-Test]";
		else
			PREFIX = "[FrrndLease]";
		
		try{
			
			TO = obj.getString("to");
			
			credits = obj.getInt("toUserCredit");
			
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
						+ ". You have successfully signed up on FrrndLease. To start using frrndlease "
						+ "you need to activate your account. Click on this link to activate your frrndlease account. <br/>"
						+ "<a href='" + EMAIL_VERIFICATION_URL + "?token=" + obj.getString("toUserActivation") + "'>"
						+ EMAIL_VERIFICATION_URL + "?token=" + obj.getString("toUserActivation") + "</a>" + "<br/>";
				break;
			case FLS_MAIL_REGISTER:
				SUBJECT = " Welcome Aboard";
				BODY = "Hello " + obj.getString("toUserName")
						+ ". You have successfully signed up on FrrndLease, the platform that helps you Make Space for leading the Life you love. <br/>"
						+ "We love our stuff, but are passionate about utilizing them well to get rich, positive experiences. <br/>"
						+ "Check out and follow us on our Facebook community page at <a href='http://www.facebook.com/frrndlease'>frrndlease on facebook</a>. "
						+ "We use that to make announcements and share posts.<br/>"
						+ "We are happy to have you. Now go and offer your dormant stuff on the platform!! <br/>"
						+ "Remember, it will always be yours ... and will come back after enriching your friends, whenever you want!";
				break;
				
			case FLS_EMAIL_VERIFICATION:
				SUBJECT = " Email Verification";
				BODY = "Hello " + obj.getString("fromUserName")
						+ "Click on this link to activate this email. <br/>"
						+ "<a href='" + EMAIL_VERIFICATION_URL + "?token=" + obj.getString("fromUserActivation") + "'>"
						+ EMAIL_VERIFICATION_URL + "?token=" + obj.getString("fromUserActivation") + "</a>" + "<br/>";
				break;

			case FLS_MAIL_DELETE_ITEM:
				SUBJECT = (" Your Item [" + obj.getString("title") + "] has been deleted from the Friend Store");
				BODY = ("<body>You have deleted the following item on FrrndLease<br/> <br/>" + " Title : " + obj.getString("title")
						+ "<br/>" + " Category : " + obj.getString("category") + "<br/>" + " Description : "
						+ obj.getString("description") + "<br/>" + " Insurance : " + obj.getInt("leaseValue") + "<br/>"
						+ " Lease Term : " + obj.getString("leaseTerm") + "<br/>" + " Status : " + obj.getString("itemStatus") + "<br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;

			case FLS_MAIL_POST_ITEM:
				SUBJECT = (" Your Item [" + obj.getString("title") + "] has been added to the Friend Store");
				BODY = ("<body>You have added the following item on FrrndLease <br/>" + obj.getString("title")
						+ "(" + obj.getString("category") 
						+ ") | Insurance: " + obj.getInt("leaseValue") + "| Lease Term : " + obj.getString("leaseTerm")
						+ "<br/>"+ obj.getString("description")+"<br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;

			case FLS_MAIL_MATCH_WISHLIST_ITEM:
				SUBJECT = (" Item [" + obj.getString("title") + "] has been added to the Friend Store");
				BODY = ("<body>Someone has posted this item that matches your wishlist. <br/> <br/>" + " Title : "
						+ obj.getString("title") + "<br/>" + " Category : " + obj.getString("category") + "<br/>"
						+ " Description : " + obj.getString("description") + "<br/>" + " Insurance : "
						+ obj.getInt("leaseValue") + "<br/>" + " Lease Term : " + obj.getString("leaseTerm") + "<br/>"
						+ " Status : " + obj.getString("itemStatus") + "<br/>" + "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title")
						+ " ></img>" + "</body>");
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
							+ l.getString("description") + "<br/>" + " Insurance : "
							+ l.getInt("leaseValue") + "<br/>" + " Lease Term : "
							+ l.getString("leaseTerm") + "<br/>" + " Status : " + l.getString("status")
							+ "<br/>" + "<img width=\"300\" src='" + l.getString("imageLinks") + "' alt="
							+ l.getString("title") + " ></img><br/><br/>");
					i++;
				}

				BODY = BODY + ("</body>");

				break;

			case FLS_MAIL_ADD_FRIEND_FROM:
				SUBJECT = (" Your Friend '" + obj.getString("from")	+ "' has been added to your Friend List. ");
				BODY = "You have added '" + obj.getString("from") + "' to your Friend List. You can now lease items to each other ";
				break;

			case FLS_MAIL_ADD_FRIEND_TO:
				SUBJECT = (" Your Friend '" + obj.getString("from") + "' has added you to their Friend List");
				BODY = "You are now in '" + obj.getString("from") + "'\'s Friend List. You can now lease items to each other <br/> <br/>"
						+ "Click here to Sign Up "+EMAIL_INVITATION_URL+obj.getString("fromUserRefferalCode");
				break;

			case FLS_MAIL_DELETE_FRIEND_FROM:
				SUBJECT = (" Your Friend \'" + obj.getString("from") + "' has been removed from your Friend List");
				BODY = "You have now removed " + obj.getString("from")
						+ " from your Friend List. You can no longer lease items to each other. Tell us what went wrong! ";
				break;

			case FLS_MAIL_DELETE_FRIEND_TO:
				SUBJECT = (" Your Friend '" + obj.getString("from") + "' removed you from thier Friend List");
				BODY = "You have been removed from the Friend List of your Friend " + obj.getString("from")
						+ ". You can no longer lease items to each other. Tell us what went wrong! ";
				break;
				
			case FLS_MAIL_MESSAGE_FRIEND_FROM:
				SUBJECT = (" Your Friend '" + obj.getString("from")	+ "' has been sent a Message on Frrndlease. ");
				BODY = obj.getString("message");
				break;

			case FLS_MAIL_MESSAGE_FRIEND_TO:
				SUBJECT = (" Your Friend '" + obj.getString("from") + "' has sent you a Message on Frrndlease.");
				BODY = obj.getString("message");
				break;
				
			case FLS_MAIL_MESSAGE_ITEM_FROM:
				SUBJECT = (" Message regarding item [" + obj.getString("title") + "] has been sent by you on Frrndlease. ");
				BODY = obj.getString("message");
				break;

			case FLS_MAIL_MESSAGE_ITEM_TO:
				SUBJECT = (" A message regarding item [" + obj.getString("title") + "] has sent to you on Frrndlease.");
				BODY = obj.getString("message");
				break;

			case FLS_MAIL_REJECT_REQUEST_FROM:
				SUBJECT = (" Request removed");
				BODY = ("<body> Request for item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> has been removed as a lease might be granted. <br/> <br/>"
						+ " Title : " + obj.getString("title")
						+ "<br/>" + " Category : " + obj.getString("category") + "<br/>" + " Description : " + obj.getString("description")
						+ "<br/>" + " Insurance : " + obj.getInt("leaseValue") + "<br/>" + " Lease Term : "
						+ obj.getString("leaseTerm") + "<br/>" + " Status : " + obj.getString("itemStatus") + "<br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;

			case FLS_MAIL_REJECT_REQUEST_TO:
				SUBJECT = (" Request removed");
				BODY = ("<body> Request of item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> has been removed by the owner as a lease might be granted. <br/> <br/>"
						+ " Title : " + obj.getString("title")
						+ "<br/>" + " Category : " + obj.getString("category") + "<br/>" + " Description : " + obj.getString("description")
						+ "<br/>" + " Insurance : " + obj.getInt("leaseValue") + "<br/>" + " Lease Term : "
						+ obj.getString("leaseTerm") + "<br/>" + " Status : " + obj.getString("itemStatus") + "<br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;

			case FLS_MAIL_DELETE_REQUEST_FROM:
				SUBJECT = (" Request removed");
				BODY = ("<body> Your Request for item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> has been removed. Item Details are as follows<br/> <br/> " 
						+ " Title : " + obj.getString("title")
						+ "<br/>" + " Category : " + obj.getString("category") + "<br/>" + " Description : " + obj.getString("description")
						+ "<br/>" + " Insurance : " + obj.getInt("leaseValue") + "<br/>" + " Lease Term : "
						+ obj.getString("leaseTerm") + "<br/>" + " Status : " + obj.getString("itemStatus") + "<br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;

			case FLS_MAIL_DELETE_REQUEST_TO:
				SUBJECT = (" Request removed");
				BODY = ("<body> Request of item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> has been removed by the Requestor. Item Details are as follows<br/> <br/> "
						+ " Title : " + obj.getString("title")
						+ "<br/>" + " Category : " + obj.getString("category") + "<br/>" + " Description : " + obj.getString("description")
						+ "<br/>" + " Insurance : " + obj.getInt("leaseValue") + "<br/>" + " Lease Term : "
						+ obj.getString("leaseTerm") + "<br/>" + " Status : " + obj.getString("itemStatus") + "<br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;

			case FLS_MAIL_GRANT_LEASE_FROM_SELF:
				SUBJECT = (" Lease granted to user [" + obj.getString("from") + "]");
				BODY = ("<body> You have sucessfully leased the following item to [" + obj.getString("from") + "] on Friend Lease - <br/> <br/>"
						+ " Title : " + obj.getString("title")
						+ "<br/>" + " Category : " + obj.getString("category") + "<br/>" + " Description : " + obj.getString("description")
						+ "<br/>" + " Insurance : " + obj.getInt("leaseValue") + "<br/>" + " Lease Term : "
						+ obj.getString("leaseTerm") + "<br/>" + " Status : " + obj.getString("itemStatus") + "<br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>"
						+ "Please confirm pickup by clicking on this link - <a href='" + EMAIL_PICKUP_CONFIRMATION + "?isOw=true&leaseId=" + obj.getInt("leaseId") + "'>"
						+ "Click to confirm</a>");
				break;

			case FLS_MAIL_GRANT_LEASE_TO_SELF:
				SUBJECT = (" Lease granted to you by [" + obj.getString("from") + "]");
				BODY = ("<body> The following item has been leased by [" + obj.getString("from") + "] to you on Friend Lease - <br/> <br/>"
						+ " Title : " + obj.getString("title")
						+ "<br/>" + " Category : " + obj.getString("category") + "<br/>" + " Description : " + obj.getString("description")
						+ "<br/>" + " Insurance : " + obj.getInt("leaseValue") + "<br/>" + " Lease Term : "
						+ obj.getString("leaseTerm") + "<br/>" + " Status : " + obj.getString("itemStatus") + "<br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>"
						+ "Please confirm pickup by clicking on this link - <a href='" + EMAIL_PICKUP_CONFIRMATION + "?isOw=false&leaseId=" + obj.getInt("leaseId") + "'>"
						+ "Click to confirm</a>");
				break;
				
			case FLS_MAIL_FROM_LEASE_STARTED:
				SUBJECT = ("Lease Started");
				BODY = ("<body>The lease has been started.</body>");
				break;
				
			case FLS_MAIL_TO_LEASE_STARTED:
				SUBJECT = ("Lease Started");
				BODY = ("<body>The lease has been started.</body>");
				break;
				
			case FLS_MAIL_GRANT_LEASE_FROM_PRIME:
				SUBJECT = (" Lease granted to user [" + obj.getString("from") + "]");
				BODY = ("<body> You have sucessfully leased the following item to [" + obj.getString("from") + "] on Friend Lease - <br/> <br/>"
						+ " Title : " + obj.getString("title")
						+ "<br/>" + " Category : " + obj.getString("category") + "<br/>" + " Description : " + obj.getString("description")
						+ "<br/>" + " Insurance : " + obj.getInt("leaseValue") + "<br/>" + " Lease Term : "
						+ obj.getString("leaseTerm") + "<br/>" + " Status : " + obj.getString("itemStatus") + "<br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>"
						+ "Your Item will shortly be picked up. Wait till the user decides if he will pick up the item or he wants frrndlease to come into the picture.");
				break;

			case FLS_MAIL_GRANT_LEASE_TO_PRIME:
				SUBJECT = (" Lease granted to you by [" + obj.getString("from") + "]");
				BODY = ("<body> The following item has been leased by [" + obj.getString("from") + "] to you on Friend Lease - <br/> <br/>"
						+ " Title : " + obj.getString("title")
						+ "<br/>" + " Category : " + obj.getString("category") + "<br/>" + " Description : " + obj.getString("description")
						+ "<br/>" + " Insurance : " + obj.getInt("leaseValue") + "<br/>" + " Lease Term : "
						+ obj.getString("leaseTerm") + "<br/>" + " Status : " + obj.getString("itemStatus") + "<br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>"
						+ "Please choose one of the options for delivery - <br/><a href='" + EMAIL_DELIVERY_PLAN + "?delPlan=self&leaseId=" + obj.getInt("leaseId") + "'>"
						+ "Self Pickup</a><br/>or<br/><a href='" + EMAIL_DELIVERY_PLAN + "?delPlan=prime&leaseId=" + obj.getInt("leaseId") + "'>Frrndlease Pickup</a>");
				break;
				
			case FLS_MAIL_OPS_PICKUP_READY:
				SUBJECT = "Lease item is ready to be picked up";
				BODY = "<body> Lease id - " + obj.getInt("leaseId") + " for the item - " + obj.getString("title") + " is ready to be picked up.</body>";
				break;

			case FLS_MAIL_CLOSE_LEASE_FROM_SELF:
				SUBJECT = (" Lease Cancelled to user [" + obj.getString("from") + "]");
				BODY = ("<body> You have closed lease of item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> and leasee ["
						+ obj.getString("from") + "] on Friend Lease - <br/> <br/>"
						+ " Title : " + obj.getString("title")
						+ "<br/>" + " Category : " + obj.getString("category") + "<br/>" + " Description : " + obj.getString("description")
						+ "<br/>" + " Insurance : " + obj.getInt("leaseValue") + "<br/>" + " Lease Term : "
						+ obj.getString("leaseTerm") + "<br/>" + " Status : " + obj.getString("itemStatus") + "<br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>"
						+ "Please confirm pickup by clicking on this link - <a href='" + EMAIL_PICKUP_CONFIRMATION + "?isOw=true&leaseId=" + obj.getInt("leaseId") + "'>"
						+ "Click to confirm</a>");
				break;

			case FLS_MAIL_CLOSE_LEASE_TO_SELF:
				SUBJECT = (" Lease Closed by the Owner");
				BODY = ("<body> Lease has been closed by the Owner for the item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> <br/> <br/>"
						+ " Title : " + obj.getString("title")
						+ "<br/>" + " Category : " + obj.getString("category") + "<br/>" + " Description : " + obj.getString("description")
						+ "<br/>" + " Insurance : " + obj.getInt("leaseValue") + "<br/>" + " Lease Term : "
						+ obj.getString("leaseTerm") + "<br/>" + " Status : " + obj.getString("itemStatus") + "<br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>"
						+ "Please confirm pickup by clicking on this link - <a href='" + EMAIL_PICKUP_CONFIRMATION + "?isOw=false&leaseId=" + obj.getInt("leaseId") + "'>"
						+ "Click to confirm</a>");
				break;
				
			case FLS_MAIL_OPS_PICKUP_CLOSE:
				SUBJECT = "Lease item is ready to be picked up";
				BODY = "<body> Lease id - " + obj.getInt("leaseId") + " for the item - " + obj.getString("title") + " is ready to be picked up.</body>";
				break;
				
			case FLS_MAIL_ITEM_INSTORE_FROM:
				SUBJECT = "Lease closed for the item";
				BODY = "<body>Your item which was leased is back InStore.</body>";
				break;
				
			case FLS_MAIL_ITEM_INSTORE_TO:
				SUBJECT = "Lease closed for the item";
				BODY = "<body>The item whose lease you had is back InStore.</body>";
				break;
				
			case FLS_MAIL_GRACE_PERIOD_OWNER:
				SUBJECT = (" Reminder to Renew Lease to user [" + obj.getString("from") + "]");
				BODY = "Less than 5 days left for lease to close.Please consider renewing the lease of item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> and leasee [" + obj.getString("from") + "] on Friend Lease - ";
				break;

			case FLS_MAIL_GRACE_PERIOD_REQUESTOR:
				SUBJECT = (" Reminder to Renew Lease");
				BODY = "Less than 5 days left for lease to close. Please consider renewing the lease of item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> ";
				break;
				
			case FLS_MAIL_RENEW_LEASE_OWNER:
				SUBJECT = (" Renewed Lease to user [" + obj.getString("fromUserName") + "]");
				BODY = ("<body>You have renewed lease to user " + obj.getString("fromUserName") + " for the following item on FrrndLease <br/> <br/>" + " Title : " + obj.getString("title")
						+ "<br/>" + " Category : " + obj.getString("category") + "<br/>" + " Description : " + obj.getString("description")
						+ "<br/>" + " Insurance : " + obj.getInt("leaseValue") + "<br/>" + " Lease Term : "
						+ obj.getString("leaseTerm") + "<br/>" + " Status : " + obj.getString("itemStatus") + "<br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;
				
			case FLS_MAIL_RENEW_LEASE_REQUESTOR:
				SUBJECT = (" Lease Renewed");
				BODY = ("<body>Item Owner " +obj.getString("fromUserName") + " has renewed lease for the following item to you on FrrndLease <br/> <br/>" + " Title : " + obj.getString("title")
						+ "<br/>" + " Category : " + obj.getString("category") + "<br/>" + " Description : " + obj.getString("description")
						+ "<br/>" + " Insurance : " + obj.getInt("leaseValue") + "<br/>" + " Lease Term : "
						+ obj.getString("leaseTerm") + "<br/>" + " Status : " + obj.getString("itemStatus") + "<br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;

			case FLS_MAIL_MAKE_REQUEST_FROM:
				SUBJECT = (" Item Requested");
				BODY = ("<body> You have sucessfully Requested the item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> on Friend Lease - <br/> <br/>"
						+ " Title : " + obj.getString("title")
						+ "<br/>" + " Category : " + obj.getString("category") + "<br/>" + " Description : " + obj.getString("description")
						+ "<br/>" + " Insurance : " + obj.getInt("leaseValue") + "<br/>" + " Lease Term : "
						+ obj.getString("leaseTerm") + "<br/>" + " Status : " + obj.getString("itemStatus") + "<br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;

			case FLS_MAIL_MAKE_REQUEST_TO:
				SUBJECT = (" Item Requested");
				BODY = ("<body> Your Item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> has been requested on Friend Lease - <br/> <br/>"
						+ " Title : " + obj.getString("title")
						+ "<br/>" + " Category : " + obj.getString("category") + "<br/>" + " Description : " + obj.getString("description")
						+ "<br/>" + " Insurance : " + obj.getInt("leaseValue") + "<br/>" + " Lease Term : "
						+ obj.getString("leaseTerm") + "<br/>" + " Status : " + obj.getString("itemStatus") + "<br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;

			case FLS_MAIL_OLD_ITEM_WARN:
				SUBJECT = (" No Item's History");
				BODY = "<body> Your Item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> has not been inactive for past 6 months.<br/>"
						+ "It will be deleted from the Store if it remains inactive for 1 more week. " + "</body>";
				break;
				
			case FLS_MAIL_OLD_REQUEST_WARN:
				SUBJECT = (" Pending Request");
				BODY = "<body> You have not responded to the request for the item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> .<br/>"
						+ "If is remains inactive for more than 2 days we will delete this request on your behalf." + "</body>";
				break;
				
			case FLS_MAIL_OLD_LEASE_WARN:
				SUBJECT = (" Awaiting Pickup of this item");
				BODY = "<body> You have not picked up this item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> for which got the lease .<br/>"
						+ "You will not get your lease agreement until the item is confirmed 'PickedUp'. If the status remains 'LeaseReady' we will delete this lease after less than 2 days." + "</body>";
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
	
}
