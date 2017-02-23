package util;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

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
		
		String LOGO_URL = "https://s3-ap-south-1.amazonaws.com/fls-meta/fls-logo.png";
		
		String FROM = "BlueMarble@frrndlease.com", CC = "BlueMarble@frrndlease.com", TO, PREFIX, SUBJECT, BODY;
		String EMAIL_VERIFICATION_URL,EMAIL_INVITATION_URL,EMAIL_FORGOT_PASSWORD,
		EMAIL_ITEM_DETAILS, EMAIL_LEASED_OUT_ITEMS, EMAIL_LEASED_IN_ITEMS, 
		EMAIL_PROFILE_PAGE, EMAIL_PICKUP_CONFIRMATION,EMAIL_DELIVERY_PLAN,
		EMAIL_GET_LEASE_AGGREMENT;
		
		int credits;
		
		EMAIL_VERIFICATION_URL = URL + "/emailverification.html";
		EMAIL_INVITATION_URL = URL + "/index.html?ref_token=";
		EMAIL_FORGOT_PASSWORD = URL + "/forgotpassword.html";
		EMAIL_ITEM_DETAILS = URL + "/ItemDetails?uid=";
		EMAIL_LEASED_OUT_ITEMS = URL + "/myapp.html#/myleasedoutitems";
		EMAIL_LEASED_IN_ITEMS = URL + "/myapp.html#/myleasedinitems";
		EMAIL_PROFILE_PAGE =  URL + "/myapp.html#/myprofile";
		EMAIL_PICKUP_CONFIRMATION = URL + "/confirmpickup.html";
		EMAIL_DELIVERY_PLAN = URL + "/deliveryplan.html";
		EMAIL_GET_LEASE_AGGREMENT = URL + "/GetLeaseAgreement";
		
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
				BODY = "Even Einstein forgot his keys all the time! Simply click on this link to reset your FrrndLease password. <br/>"
						+ "<a href='" + EMAIL_FORGOT_PASSWORD + "?act=" + obj.getString("toUserActivation") + "'>"
						+ EMAIL_FORGOT_PASSWORD + "?act=" + obj.getString("toUserActivation") + "</a>";
				break;
			case FLS_MAIL_SIGNUP_VALIDATION:
				SUBJECT = " Email Verification";
				BODY = "Hello " + obj.getString("toUserName")
						+ ". Almost there! You have successfully signed up on FrrndLease. One last step to start using FrrndLease. "
						+ "Click on this link to activate your account. <br/>"
						+ "<a href='" + EMAIL_VERIFICATION_URL + "?token=" + obj.getString("toUserActivation") + "'>"
						+ EMAIL_VERIFICATION_URL + "?token=" + obj.getString("toUserActivation") + "</a>" + "<br/>";
				break;
			case FLS_MAIL_REGISTER:
				SUBJECT = " Welcome Aboard";
				BODY = "Hello " + obj.getString("toUserName")
						+ "! <br/> You have successfully activated your account on FrrndLease, the Friendly Library of Things. <br/><br/>"
						+ "We are truely happy to have you. We are a community of sharers; we love our things, but only as a means to get rich, positive experiences. Here's what you can do now:<br/><br/>"
						+ "1. Go and offer your dormant things for others to share!! <br/>"
						+ "Remember, it will always be yours ... and we will bring it back, whenever you want! <br/><br/>"
						+ "2. Also check out what other Members are sharing. <br/>"
						+ "You might save some money or a trip to the mall ... <br/><br/>"
						+ "3. Check out and follow us on our Facebook community page at <a href='http://www.facebook.com/frrndlease'>frrndlease on facebook</a>. "
						+ "We use that to make announcements and share posts <br/>";
				
				break;
				
			case FLS_EMAIL_VERIFICATION:
				SUBJECT = " Email Verification";
				BODY = "Hello " + obj.getString("fromUserName")
						+ "We want to make sure you are real! Click on this link to activate your account. <br/>"
						+ "<a href='" + EMAIL_VERIFICATION_URL + "?token=" + obj.getString("fromUserActivation") + "'>"
						+ EMAIL_VERIFICATION_URL + "?token=" + obj.getString("fromUserActivation") + "</a>" + "<br/>";
				break;

			case FLS_MAIL_DELETE_ITEM:
				SUBJECT = (" Your Item [" + obj.getString("title") + "] has been deleted from FrrndLease");
				BODY = ("<body>You have deleted the following item on FrrndLease <br/>" + obj.getString("title")
						+ "(" + obj.getString("category") 
						+ ") | Insurance: " + obj.getInt("leaseValue") + "| Lease Term : " + obj.getString("leaseTerm")
						+ "<br/>"+ obj.getString("description")+"<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;

			case FLS_MAIL_POST_ITEM:
				SUBJECT = (" Your Item [" + obj.getString("title") + "] has been added to FrrndLease");
				BODY = ("<body>You have added the following item on FrrndLease <br/>" + obj.getString("title")
						+ "(" + obj.getString("category") 
						+ ") | Insurance: " + obj.getInt("leaseValue") + "| Lease Term : " + obj.getString("leaseTerm")
						+ "<br/>"+ obj.getString("description")+"<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;
				
			case FLS_MAIL_ITEM_ON_HOLD:
				SUBJECT = ("Your Item [" + obj.getString("title") + "] has been put on hold");
				BODY = ("<body>Your Item " + obj.getString("title") + "-<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img><br/><br/>"
								+ "Has been put on hold because of the inappropriate content.</body>");
				break;
				
			case FLS_MAIL_ITEM_INSTORE:
				SUBJECT = ("Your Item [" + obj.getString("title") + "] is back In Store");
				BODY = ("<body>Your Item " + obj.getString("title") + "-<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img><br/><br/>"
								+ "Your Item is back In Store.</body>");
				break;

			case FLS_MAIL_MATCH_WISHLIST_ITEM:
				SUBJECT = (" Your Wish [" + obj.getString("title") + "] has been added to FrrndLease");
				BODY = ("<body>Someone has posted this item that matches your wishlist. <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>Go get it!</a>"+"<br/>" + obj.getString("title")
						+ "(" + obj.getString("category") 
						+ ") | Insurance: " + obj.getInt("leaseValue") + "| Lease Term : " + obj.getString("leaseTerm") + "| Status: " + obj.getString("itemStatus")
						+ "<br/>"+ obj.getString("description")+"<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;

			case FLS_MAIL_MATCH_POST_ITEM:
				MatchItems matchItems = new MatchItems();
				List<JSONObject> listItems = matchItems.checkPostedItems(obj.getInt("itemId"));
				SUBJECT = (" You have a match on FrrndLease!");
				BODY = ("<body>Check out these items on FrrndLease that match your wishlist. Go request them! <br/> <br/>");
				
				for (JSONObject l : listItems) {
					BODY = BODY + (" Title : " + l.getString("title") + "<br/>" + " Category : "
							+ l.getString("category") + "<br/>" + " Description : "
							+ l.getString("description") + "<br/>" + " Insurance : "
							+ l.getInt("leaseValue") + "<br/>" + " Lease Term : "
							+ l.getString("leaseTerm") + "<br/>" + " Status : " + l.getString("status")+ "<br/>"
							+ "<img width=\"300\" src='" + l.getString("imageLinks") + "' alt="
							+ l.getString("title") + " ></img><br/><br/>");
				}

				BODY = BODY + ("</body>");

				break;

			case FLS_MAIL_ADD_FRIEND_FROM:
				SUBJECT = (" Your Friend '" + obj.getString("from")	+ "' has been added to your Friend List. ");
				BODY = "Great start! You have added '" + obj.getString("from") + "' to your Friend List. Once he/she responds, you will be able to lease items to each other with discounted credits!";
				break;

			case FLS_MAIL_ADD_FRIEND_TO:
				SUBJECT = (" Your Friend '" + obj.getString("from") + "' has added you to their Friend List");
				BODY = "You are now on '" + obj.getString("from") + "'\'s Friend List. Once you Sign Up, you can lease items to each other at discounted credits!<br/> <br/>"
						+ "Click here to Sign Up "+EMAIL_INVITATION_URL+obj.getString("fromUserRefferalCode");
				break;

			case FLS_MAIL_DELETE_FRIEND_FROM:
				SUBJECT = (" Your Friend \'" + obj.getString("from") + "' has been removed from your Friend List");
				BODY = "You have now removed " + obj.getString("from")
						+ " from your Friend List. You can no longer lease items to each other at discounted credits. Tell us what went wrong! ";
				break;

			case FLS_MAIL_DELETE_FRIEND_TO:
				SUBJECT = (" Your Friend '" + obj.getString("from") + "' removed you from thier Friend List");
				BODY = "You have been removed from the Friend List of your Friend " + obj.getString("from")
						+ ". You can no longer lease items to each other at discounted credits. Tell us what went wrong! ";
				break;
				
			case FLS_MAIL_MESSAGE_FRIEND_FROM:
				SUBJECT = (" Your Friend '" + obj.getString("fromUserName")	+ "' has been sent a Message on FrrndLease. ");
				BODY = obj.getString("message");
				break;

			case FLS_MAIL_MESSAGE_FRIEND_TO:
				SUBJECT = (" Your Friend '" + obj.getString("fromUserName") + "' has sent you a Message on FrrndLease.");
				BODY = obj.getString("message");
				break;
				
			case FLS_MAIL_MESSAGE_ITEM_FROM:
				SUBJECT = (" Message regarding item [" + obj.getString("title") + "] has been sent by you on FrrndLease. ");
				BODY = obj.getString("message");
				break;

			case FLS_MAIL_MESSAGE_ITEM_TO:
				SUBJECT = (" A message regarding item [" + obj.getString("title") + "] has sent to you on FrrndLease.");
				BODY = obj.getString("message");
				break;

			case FLS_MAIL_REJECT_REQUEST_FROM:
				SUBJECT = (" Request removed");
				BODY = ("<body> Your Request for item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> has been removed as a lease might have been granted to someone else. <br/>" + obj.getString("title")
						+ "(" + obj.getString("category") 
						+ ") | Insurance: " + obj.getInt("leaseValue") + "| Lease Term : " + obj.getString("leaseTerm")
						+ "<br/>"+ obj.getString("description")+"<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;

			case FLS_MAIL_REJECT_REQUEST_TO:
				SUBJECT = (" Request removed");
				BODY = ("<body> Request for item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> has been removed by the owner as a lease might have been granted to someone else. <br/> " + obj.getString("title")
						+ "(" + obj.getString("category") 
						+ ") | Insurance: " + obj.getInt("leaseValue") + "| Lease Term : " + obj.getString("leaseTerm")
						+ "<br/>"+ obj.getString("description")+"<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;

			case FLS_MAIL_DELETE_REQUEST_FROM:
				SUBJECT = (" Request deleted");
				BODY = ("<body> Your Request for item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> has been removed. It's ok to change your mind! <br/> " + obj.getString("title")
						+ "(" + obj.getString("category") 
						+ ") | Insurance: " + obj.getInt("leaseValue") + "| Lease Term : " + obj.getString("leaseTerm")
						+ "<br/>"+ obj.getString("description")+"<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;

			case FLS_MAIL_DELETE_REQUEST_TO:
				SUBJECT = (" Request deleted");
				BODY = ("<body> Request of item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> has been removed by the Requestor. People change their minds sometimes!<br/> " + obj.getString("title")
						+ "(" + obj.getString("category") 
						+ ") | Insurance: " + obj.getInt("leaseValue") + "| Lease Term : " + obj.getString("leaseTerm")
						+ "<br/>"+ obj.getString("description")+"<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;

			case FLS_MAIL_GRANT_LEASE_FROM_SELF:
				SUBJECT = (" Lease granted to user [" + obj.getString("fromUserName") + "]");
				BODY = ("<body> Congratulations! Your item is ready to be leased to [" + obj.getString("fromUserName") + "] on FrrndLease - <br/> " + obj.getString("title")
						+ "(" + obj.getString("category") 
						+ ") | Insurance: " + obj.getInt("leaseValue") + "| Lease Term : " + obj.getString("leaseTerm")
						+ "<br/>"+ obj.getString("description")+"<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>"
						+ "To start the lease, please confirm item pickup by clicking on this link - <a href='" + EMAIL_PICKUP_CONFIRMATION + "?isOw=true&leaseId=" + obj.getInt("leaseId") + "'>"
						+ "Click to confirm</a>");
				break;

			case FLS_MAIL_GRANT_LEASE_TO_SELF:
				SUBJECT = (" Lease granted to you by [" + obj.getString("fromUserName") + "]");
				BODY = ("<body> Congratulations! The following item is ready to be leased by [" + obj.getString("fromUserName") + "] to you on FrrndLease - <br/> " + obj.getString("title")
						+ "(" + obj.getString("category") 
						+ ") | Insurance: " + obj.getInt("leaseValue") + "| Lease Term : " + obj.getString("leaseTerm")
						+ "<br/>"+ obj.getString("description")+"<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>"
						+ "To start the lease, please confirm item pickup by clicking on this link - <a href='" + EMAIL_PICKUP_CONFIRMATION + "?isOw=false&leaseId=" + obj.getInt("leaseId") + "'>"
						+ "Click to confirm</a>");
				break;
				
			case FLS_MAIL_FROM_LEASE_STARTED:
				SUBJECT = (" Lease Started for Item");
				BODY = ("<body>Yay! Your lease has started. The good earth thanks you!"
						+ "<form action=\"" + EMAIL_GET_LEASE_AGGREMENT + "\" method=\"POST\" target=\"_blank\">"
                        +   "<input type=\"hidden\" name=\"leaseId\" value=\""+obj.getInt("leaseId")+"\" />"
                        +  "<input type=\"submit\" style=\"background-color:#1D62F0\" value=\"Download Agreement\" /></form></body>");
				break;
				
			case FLS_MAIL_TO_LEASE_STARTED:
				SUBJECT = (" Lease Started for Item");
				BODY = ("<body>Yay! Your lease has started. The good earth thanks you!"
						+ "<form action=\"" + EMAIL_GET_LEASE_AGGREMENT + "\" method=\"POST\" target=\"_blank\">"
                        +   "<input type=\"hidden\" name=\"leaseId\" value=\""+obj.getInt("leaseId")+"\" />"
                        +  "<input type=\"submit\" style=\"background-color:#1D62F0\" value=\"Download Agreement\" /></form></body>");
				break;
				
			case FLS_MAIL_GRANT_LEASE_FROM_PRIME:
				SUBJECT = (" Lease granted to user [" + obj.getString("fromUserName") + "]");
				BODY = ("<body> You have sucessfully leased the following item to [" + obj.getString("fromUserName") + "] on FrrndLease - <br/> " + obj.getString("title")
						+ "(" + obj.getString("category") 
						+ ") | Insurance: " + obj.getInt("leaseValue") + "| Lease Term : " + obj.getString("leaseTerm")
						+ "<br/>"+ obj.getString("description")+"<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>"
						+ "You dont need to do anything now. Your item will picked up shortly. We are waiting for the Requestor to pick his/her delivery option.");
				break;

			case FLS_MAIL_GRANT_LEASE_TO_PRIME:
				SUBJECT = (" Lease granted to you by [" + obj.getString("fromUserName") + "]");
				BODY = ("<body> Congratulations! The following item has been leased by [" + obj.getString("fromUserName") + "] to you on FrrndLease - <br/> " + obj.getString("title")
						+ "(" + obj.getString("category") 
						+ ") | Insurance: " + obj.getInt("leaseValue") + "| Lease Term : " + obj.getString("leaseTerm")
						+ "<br/>"+ obj.getString("description")+"<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>"
						+ "You are eligible for Prime pickup, where we pickup the item for you for a small delivery fee. However, you can choose to pick it up yourself. Please choose your delivery option, to start your lease: <br/><a href='" + EMAIL_DELIVERY_PLAN + "?delPlan=self&leaseId=" + obj.getInt("leaseId") + "'>"
						+ "Self Pickup</a><br/>or<br/><a href='" + EMAIL_DELIVERY_PLAN + "?delPlan=prime&leaseId=" + obj.getInt("leaseId") + "'>FrrndLease Pickup</a>");
				break;
				
			case FLS_MAIL_OPS_PICKUP_READY:
				SUBJECT = "Lease item is ready to be picked up";
				BODY = "<body> Lease id - " + obj.getInt("leaseId") + " for the item - " + obj.getString("title") + " is ready to be picked up.</body>";
				break;

			case FLS_MAIL_CLOSE_LEASE_FROM_SELF:
				SUBJECT = (" Lease Cancelled for user [" + obj.getString("fromUserName") + "]");
				BODY = ("<body> You have closed the lease for item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> and leasee ["
						+ obj.getString("fromUserName") + "] on Friend Lease - <br/> " + obj.getString("title")
						+ "(" + obj.getString("category") 
						+ ") | Insurance: " + obj.getInt("leaseValue") + "| Lease Term : " + obj.getString("leaseTerm")
						+ "<br/>"+ obj.getString("description")+"<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>"
						+ "To stop the lease and release credits, please confirm pickup by clicking on this link <a href='" + EMAIL_PICKUP_CONFIRMATION + "?isOw=true&leaseId=" + obj.getInt("leaseId") + "'>"
						+ "Click to confirm</a>");
				break;

			case FLS_MAIL_CLOSE_LEASE_TO_SELF:
				SUBJECT = (" Lease Closed by Owner");
				BODY = ("<body> The Owner closed the lease for this item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> <br/> " + obj.getString("title")
						+ "(" + obj.getString("category") 
						+ ") | Insurance: " + obj.getInt("leaseValue") + "| Lease Term : " + obj.getString("leaseTerm")
						+ "<br/>"+ obj.getString("description")+"<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>"
						+ "To stop the lease and release credits, please confirm pickup by clicking on this link <a href='" + EMAIL_PICKUP_CONFIRMATION + "?isOw=false&leaseId=" + obj.getInt("leaseId") + "'>"
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
				SUBJECT = (" Reminder to close lease to user [" + obj.getString("fromUserName") + "]");
				BODY = "There are less than 5 days for your lease to close. If you want the item back, please close the lease. It will auto-renew if the requestor has credits for it, and you do not need to do anything for the item <a href='" + EMAIL_LEASED_OUT_ITEMS + "'>" + obj.getString("title") + "</a> and Requestor [" + obj.getString("fromUserName") + "] on FrrndLease.";
				break;
				
			case FLS_MAIL_GRACE_PERIOD_REQUESTOR:
				SUBJECT = (" Reminder to close lease");
				BODY = "There are less than 5 days for your lease to close. It will auto-renew if you have enough credits and you do not need to do anything. If you want to return the item, please close the lease for item <a href='" + EMAIL_LEASED_IN_ITEMS + "'>" + obj.getString("title") + "</a> ";
				break;
				
			case FLS_MAIL_LEASE_ENDED_OWNER:
				SUBJECT = (" Reminder to update status of lease given to user [" + obj.getString("fromUserName") + "]");
				BODY = ("<body>Lease for your item has ended. Please update lease status for the item <a href='" + EMAIL_LEASED_OUT_ITEMS + "'>" + obj.getString("title") + "</a> and Requestor [" + obj.getString("fromUserName") + "] on FrrndLease.</body>"
						+ "To stop the lease and release credits, please confirm item pickup by clicking on this link - <a href='" + EMAIL_PICKUP_CONFIRMATION + "?isOw=true&leaseId=" + obj.getInt("leaseId") + "'>"
						+ "Click to confirm</a>");
				break;
				
			case FLS_MAIL_LEASE_ENDED_REQUESTOR:
				SUBJECT = (" Reminder to update lease status");
				BODY = ("<body>Lease for an item you leased has ended. Please update lease status for item <a href='" + EMAIL_LEASED_IN_ITEMS + "'>" + obj.getString("title") + "</a> </body>"
						+ "To stop the lease and release credits, please confirm item pickup by clicking on this link - <a href='" + EMAIL_PICKUP_CONFIRMATION + "?isOw=false&leaseId=" + obj.getInt("leaseId") + "'>"
						+ "Click to confirm</a>");
				break;
				
			case FLS_MAIL_LEASE_READY_OWNER:
				SUBJECT = (" Reminder to update status of lease given to user [" + obj.getString("fromUserName") + "]");
				BODY = ("<body>Lease for your item has started. Please update lease status for the item <a href='" + EMAIL_LEASED_OUT_ITEMS + "'>" + obj.getString("title") + "</a> and Requestor [" + obj.getString("fromUserName") + "] on FrrndLease.</body>"
						+ "To start the lease, please confirm item pickup by clicking on this link - <a href='" + EMAIL_PICKUP_CONFIRMATION + "?isOw=true&leaseId=" + obj.getInt("leaseId") + "'>"
						+ "Click to confirm</a>");
				break;
				
			case FLS_MAIL_LEASE_READY_REQUESTOR:
				SUBJECT = (" Reminder to update lease status");
				BODY = ("</body>Lease for an item you requested has started. Please update lease status for item <a href='" + EMAIL_LEASED_IN_ITEMS + "'>" + obj.getString("title") + "</a> </body>"
						+ "To start the lease, please confirm item pickup by clicking on this link - <a href='" + EMAIL_PICKUP_CONFIRMATION + "?isOw=false&leaseId=" + obj.getInt("leaseId") + "'>"
						+ "Click to confirm</a>");
				break;
				
			case FLS_MAIL_RENEW_LEASE_OWNER:
				SUBJECT = (" Lease renewed for user [" + obj.getString("fromUserName") + "]");
				BODY = ("<body>You have renewed lease to user " + obj.getString("fromUserName") + " for the following item on FrrndLease <br/> " + obj.getString("title")
						+ "(" + obj.getString("category") 
						+ ") | Insurance: " + obj.getInt("leaseValue") + "| Lease Term : " + obj.getString("leaseTerm")
						+ "<br/>"+ obj.getString("description")+"<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;
				
			case FLS_MAIL_RENEW_LEASE_REQUESTOR:
				SUBJECT = (" Lease Renewed");
				BODY = ("<body>Item Owner " +obj.getString("fromUserName") + " has renewed lease for the following item to you on FrrndLease <br/> " + obj.getString("title")
						+ "(" + obj.getString("category") 
						+ ") | Insurance: " + obj.getInt("leaseValue") + "| Lease Term : " + obj.getString("leaseTerm")
						+ "<br/>"+ obj.getString("description")+"<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;

			case FLS_MAIL_MAKE_REQUEST_FROM:
				SUBJECT = ("Item Requested");
				BODY = ("<body> You have Requested the item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> on FrrndLease. The Owner of the item will respond within a week!<br/>"  + obj.getString("title")
						+ "(" + obj.getString("category") 
						+ ") | Insurance: " + obj.getInt("leaseValue") + "| Lease Term : " + obj.getString("leaseTerm")
						+ "<br/>"+ obj.getString("description")+"<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;

			case FLS_MAIL_MAKE_REQUEST_TO:
				SUBJECT = ("Item Requested");
				BODY = ("<body> Your Item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> has been requested on FrrndLease. Check out <a href=\"" + URL + "/myapp.html#/myincomingrequests\">Your Incoming Requests</a>. Please respond within a week! <br/>" + obj.getString("title")
						+ "(" + obj.getString("category") 
						+ ") | Insurance: " + obj.getInt("leaseValue") + "| Lease Term : " + obj.getString("leaseTerm")
						+ "<br/>"+ obj.getString("description")+"<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") + " ></img>" + "</body>");
				break;

			case FLS_MAIL_OLD_ITEM_WARN:
				SUBJECT = (" Your Item might be deleted on FrrndLease!");
				BODY = "<body> Your Item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> has not seen any activity for past 6 months. That's a long time!<br/>"
						+ "It will be deleted if it there is no activity for 1 more week. If you want to keep it listed, you can edit your posting to tell us that you still want it. " + "</body>";
				break;
				
			case FLS_MAIL_OLD_REQUEST_WARN:
				SUBJECT = (" Pending Request on FrrndLease");
				BODY = "<body> You have a pending request for the item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> .<br/>"
						+ "If life is getting in the way, do nothing and we will delete this request on your behalf in 2 days." + "</body>";
				break;
				
			case FLS_MAIL_OLD_LEASE_WARN:
				SUBJECT = (" Awaiting Pickup of this item");
				BODY = "<body> Your lease has not started for this item <a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'>" + obj.getString("title") + "</a> because you have not picked it up yet. <br/>"
						+ "To start the lease and download your lease agreement, please confirm that the item has been picked up. If life is gettgin in the way, do nothing and we will delete this lease in the next 2 days. " + "</body>";
				break;
			
			case FLS_MAIL_REMIND_PHOTO_ID:
				SUBJECT = ("Reminder to Upload a Photo Id");
				BODY = "You are in a Prime location! This means that you can avail of Prime delivery from FrrndLease, by having a valid photo id stored with us! You can always choose to not get delivery through FrrndLease. Upload your Photo Id <a href='" + EMAIL_PROFILE_PAGE + "'>now</a> !!";
				break;
				
			case FLS_MAIL_OPS_ADD_LEAD:
				SUBJECT = ("New Lead Added");
				BODY = obj.getString("message");
				break;
			
			case FLS_MAIL_ADD_LEAD:
				SUBJECT = ("Subsciption Successful");
				BODY = "<body>Thank You for subscribing to FrrndLease. You will recieve periodic updates about our exciting offers</body>";
				break;
				
			case FLS_MAIL_OWNER_REQUEST_LIMIT:
				SUBJECT = ("Requests Limit Reached");
				BODY = "<body>People are not able to request since you have reached the limit for receiving incoming requests. To get unlimited number of requests please switch to Uber Plan.</body>";
				break;
				
			case FLS_MAIL_UBER_WARN:
				SUBJECT = ("Membership About to Expire");
				BODY = "<body>You have less than 3 days left for your uber membership to expiry. Please <a href='" + EMAIL_PROFILE_PAGE + "'>upgrade</a> it to avoid nulling of all your item lease fee.</body>";
				break;
				
			case FLS_CREDITS_INVOICE:
				SUBJECT = ("Credits Bought on FrrndLease");
				BODY = "<body>" + obj.getString("message") + "</body>";
				break;
				
			case FLS_MEMBERSHIP_INVOICE:
				SUBJECT = ("Uber Membership Bought on FrrndLease");
				BODY = "<body>" + obj.getString("message") + "</body>";
				break;
				
			case FLS_MAIL_ADMIN_PHOTO_ID_UPLOAD:
				SUBJECT = ("User Uploaded Photo Id");
				BODY = "<body>" + obj.getString("message") + "</body>";
				break;
				
			case FLS_MAIL_USER_PHOTO_ID_VERIFIED:
				SUBJECT = ("Congratulations!! Photo Id Verified");
				BODY = "<body>" + obj.getString("message") + "</body>";
				break;
				
			case FLS_MAIL_WEEKLY_DIGEST:
				SUBJECT = ("Weekly Digest");
				BODY = obj.getString("message");
				break;
				
			case FLS_MAIL_SHARE_ITEM_FRIEND:
				SUBJECT = (" Item [" + obj.getString("title") + "] shared with you on FrrndLease");
				BODY = ("<body>Your Friend [" + obj.getString("fromUserName") + "] has shared an item  with you on FrrndLease <br/>" + obj.getString("title")
						+ "(" + obj.getString("category") 
						+ ") | Insurance: " + obj.getInt("leaseValue") + "| Lease Term : " + obj.getString("leaseTerm")
						+ "<br/>"+ obj.getString("description")+"<br/><br/>"
						+ "<img width=\"300\" src='" + obj.getString("imageLinks") + "' alt=" + obj.getString("title") 
						+ " ></img><br/><br/><a href='" + EMAIL_ITEM_DETAILS + obj.getString("uid") + "'><button type='"+"button"+"'>View Item</button></a></body>");
				break;
			
			case FLS_MAIL_SHARE_ITEM_OWNER:
				SUBJECT = ("Item [" + obj.getString("title") + "] shared with your friends on FrrndLease");
				BODY = obj.getString("message");
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
			message.setFrom(new InternetAddress(FROM, "FrrndLease"));
			message.setReplyTo(new Address[] { new InternetAddress(FROM) });
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(TO));
			if (!ENV_CONFIG.equals("dev")) {
				message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(CC));
			}

			Multipart multipart = new MimeMultipart("related");

			if(notificationType == Notification_Type.FLS_MAIL_ADD_LEAD){
				template = ve.getTemplate("templates/leadEmail.vm");
			}else{
				template = ve.getTemplate("templates/defaultEmail.vm");
			}
			
			if(credits != -1)
				context.put("credits", credits);
			context.put("subject", SUBJECT);
			context.put("body", BODY);
			context.put("logo", LOGO_URL);
						
			BodyPart body = new MimeBodyPart();
						
			multipart.addBodyPart(body);
			           
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
	
}
