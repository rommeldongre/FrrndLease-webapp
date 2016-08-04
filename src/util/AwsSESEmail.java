package util;
/*
 * Copyright 2014-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Session;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.imageio.ImageIO;
import javax.mail.Message;
import javax.mail.Multipart;
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

import pojos.FriendsModel;
import pojos.GrantLeaseReqObj;
import pojos.ItemsModel;
import pojos.LeasesModel;
import pojos.PostItemReqObj;
import pojos.RenewLeaseReqObj;
import pojos.RequestsModel;
import pojos.UsersModel;
import tableOps.Wishlist;
import util.FlsEnums.Notification_Type;
import util.FlsConfig;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

import connect.Connect;

import com.amazonaws.auth.BasicAWSCredentials;

public class AwsSESEmail extends Connect {

	private static FlsLogger LOGGER = new FlsLogger(AwsSESEmail.class.getName());

	static final String FROM = "BlueMarble@frrndlease.com"; // Replace with your
															// "From" address.
															// This address must
															// be verified.
	static String TO; // Replace with a "To" address. If you have not yet
						// requested
						// production access, this address must be verified.
	static String CC = "BlueMarble@frrndlease.com";
	static String BODY;
	static String SUBJECT;
	static String PREFIX;

	private static String user_id;
	static String env_config = FlsConfig.env;

	/*
	 * Before running the code: Fill in your AWS access credentials in the
	 * provided credentials file template, and be sure to move the file to the
	 * default location (~/.aws/credentials) where the sample code will load the
	 * credentials from.
	 * https://console.aws.amazon.com/iam/home?#security_credential
	 *
	 * WANRNING: To avoid accidental leakage of your credentials, DO NOT keep
	 * the credentials file in your source directory.
	 */
	public void send(String userId, Notification_Type fls_enum, Object obj, String... apiflag) throws IOException {

		// Fls_Enum = fls_enum;
		user_id = userId;
		TO = userId;
		String EMAIL_VERIFICATION_URL,EMAIL_INVITATION_URL,EMAIL_FORGOT_PASSWORD;

		// this variable is used to store the image
		File imageFile = null;

		// this variable is used to store list of files
		List<File> imageFiles = new ArrayList<>();

		if (env_config.equals("dev")) {
			EMAIL_VERIFICATION_URL = "http://localhost:8080/flsv2/emailverification.html";
			EMAIL_INVITATION_URL ="http://localhost:8080/flsv2/ref_token=";
			EMAIL_FORGOT_PASSWORD = "http://localhost:8080/flsv2/forgotpassword.html";
			PREFIX = "[FrrndLease-Test]";
			
		} else {
			EMAIL_VERIFICATION_URL = "http://www.frrndlease.com/emailverification.html";
			EMAIL_INVITATION_URL ="http://www.frrndlease.com/ref_token=";
			EMAIL_FORGOT_PASSWORD = "http://www.frrndlease.com/forgotpassword.html";
			PREFIX = "[FrrndLease]";
		}
		
		int credit = 0;
		Connection hcp = getConnectionFromPool();
		PreparedStatement s1 = null;
		ResultSet rs1 = null;
		try{
			// getting the credits of the user
			String sqlGetCredit = "SELECT user_credit FROM users WHERE user_id=?";
			s1 = hcp.prepareStatement(sqlGetCredit);
			s1.setString(1, user_id);
			rs1 = s1.executeQuery();
			if (rs1.next()) {
				credit = rs1.getInt("user_credit");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { 
				if(rs1 != null)rs1.close();
				if(s1 != null)s1.close();
				if(hcp != null)hcp.close(); 
			} catch(SQLException e) {}
		}
		
		try {
			
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
		switch (fls_enum) {
		case FLS_MAIL_FORGOT_PASSWORD:
			String activation = (String)obj;
			SUBJECT = " Forgot Password";
			BODY = "Click on this link to reset password for your frrndlease account. <br/>"
					+ "<a href='" + EMAIL_FORGOT_PASSWORD + "?act=" + activation + "'>"
					+ EMAIL_FORGOT_PASSWORD + "?act=" + activation + "</a>";
			break;
		case FLS_MAIL_SIGNUP_VALIDATION:
			UsersModel um = (UsersModel) obj;
			SUBJECT = " Email Verification";
			BODY = "Hello " + um.getFullName()
					+ ". You have successfully signed up on fRRndLease. To start using frrndlease "
					+ "you need to activate your account. Click on this link to activate your frrndlease account. <br/>"
					+ "<a href='" + EMAIL_VERIFICATION_URL + "?token=" + um.getActivation() + "'>"
					+ EMAIL_VERIFICATION_URL + "?token=" + um.getActivation() + "</a>" + "<br/>";
			break;
		case FLS_MAIL_REGISTER:
			UsersModel uom = (UsersModel) obj;
			SUBJECT = " Welcome Aboard";
			BODY = "Hello " + uom.getFullName()
					+ ". You have successfully signed up on fRRndLease, the platform that helps you Make Space for leading the Life you love. <br/>"
					+ "We love our stuff, but are passionate about utilizing them well to get rich, positive experiences. <br/>"
					+ "Check out and follow us on our Facebook community page at <a href='http://www.facebook.com/frrndlease'>frrndlease on facebook</a>. "
					+ "We use that to make announcements and share posts.<br/>"
					+ "We are happy to have you. Now go and offer your dormant stuff on the platform!! <br/>"
					+ "Remember, it will always be yours ... and will come back after enriching your friends, whenever you want!";
			break;

		case FLS_MAIL_DELETE_ITEM:
			ItemsModel idom = (ItemsModel) obj;
			SUBJECT = (" Your Item [" + idom.getTitle() + "] has been deleted from the Friend Store");
			BODY = ("<body>You have deleted the following item on fRRndLease<br/> <br/>" + " Title : " + idom.getTitle()
					+ "<br/>" + " Category : " + idom.getCategory() + "<br/>" + " Description : "
					+ idom.getDescription() + "<br/>" + " Lease Value : " + idom.getLeaseValue() + "<br/>"
					+ " Lease Term : " + idom.getLeaseTerm() + "<br/>" + " Status : " + idom.getStatus() + "<br/>"
					+ "<img src=\"cid:image\" alt=" + idom.getTitle() + " ></img>" + "</body>");
			imageFile = convertBinaryToImage(idom.getImage());
			break;

		case FLS_MAIL_POST_ITEM:
			PostItemReqObj iom = (PostItemReqObj) obj;
			SUBJECT = (" Your Item [" + iom.getTitle() + "] has been added to the Friend Store");
			BODY = ("<body>You have added the following item on fRRndLease <br/> <br/>" + " Title : " + iom.getTitle()
					+ "<br/>" + " Category : " + iom.getCategory() + "<br/>" + " Description : " + iom.getDescription()
					+ "<br/>" + " Lease Value : " + iom.getLeaseValue() + "<br/>" + " Lease Term : "
					+ iom.getLeaseTerm() + "<br/>" + " Status : " + iom.getStatus() + "<br/>"
					+ "<img src=\"cid:image\" alt=" + iom.getTitle() + " ></img>" + "</body>");
			imageFile = convertBinaryToImage(iom.getImage());
			break;

		case FLS_MAIL_MATCH_WISHLIST_ITEM:
			PostItemReqObj itemObj = (PostItemReqObj) obj;
			SUBJECT = (" Item [" + itemObj.getTitle() + "] has been added to the Friend Store");
			BODY = ("<body>Someone has posted this item that matches your wishlist. <br/> <br/>" + " Title : "
					+ itemObj.getTitle() + "<br/>" + " Category : " + itemObj.getCategory() + "<br/>"
					+ " Description : " + itemObj.getDescription() + "<br/>" + " Lease Value : "
					+ itemObj.getLeaseValue() + "<br/>" + " Lease Term : " + itemObj.getLeaseTerm() + "<br/>"
					+ " Status : " + itemObj.getStatus() + "<br/>" + "<img src=\"cid:image\" alt=" + itemObj.getTitle()
					+ " ></img>" + "</body>");
			imageFile = convertBinaryToImage(itemObj.getImage());
			break;

		case FLS_MAIL_MATCH_POST_ITEM:
			List<PostItemReqObj> listItems = (List<PostItemReqObj>) obj;
			SUBJECT = (" Items present in the Friend Store match your wishlist");
			BODY = ("<body>These items match your wishlist. <br/> <br/>");

			int len = listItems.size();

			for (int i = 0; i < len; i++) {
				BODY = BODY + (" Title : " + listItems.get(i).getTitle() + "<br/>" + " Category : "
						+ listItems.get(i).getCategory() + "<br/>" + " Description : "
						+ listItems.get(i).getDescription() + "<br/>" + " Lease Value : "
						+ listItems.get(i).getLeaseValue() + "<br/>" + " Lease Term : "
						+ listItems.get(i).getLeaseTerm() + "<br/>" + " Status : " + listItems.get(i).getStatus()
						+ "<br/>" + "<img src=\"cid:image" + Integer.toString(i) + "\" alt="
						+ listItems.get(i).getTitle() + " ></img><br/><br/>");
				imageFiles.add(convertBinaryToImage(listItems.get(i).getImage()));
			}

			BODY = BODY + ("</body>");

			break;

		case FLS_MAIL_ADD_FRIEND_FROM:
			FriendsModel affm = (FriendsModel) obj;
			if (apiflag != null && apiflag[0] == "@api") {
				SUBJECT = (" Your Friend '" + affm.getFullName() + "' has been added to your Friend List");
				BODY = ("<body>You have added your trusted friend '" + affm.getFullName()
						+ "' to your Friend List. You can now lease items to each other <br/> <br/></body>");
			} else if (apiflag != null && apiflag[0] == "@email") {
				SUBJECT = (" Your Friend '" + (affm.getFriendId())
						+ "' has been added to your Friend List. ");
				BODY = "You have added '" + (affm.getFriendId()) + "' to your Friend List. You can now lease items to each other ";
			}
			break;

		case FLS_MAIL_ADD_FRIEND_TO:
			FriendsModel atfm = (FriendsModel) obj;
			SUBJECT = (" Your Friend '" + atfm.getUserId() + "' has added you to their Friend List");
			BODY = "You are now in '" + atfm.getUserId() + "'\'s Friend List. You can now lease items to each other <br/> <br/>"
					+ "Click here to Sign Up "+EMAIL_INVITATION_URL+atfm.getReferralCode();
			break;

		case FLS_MAIL_DELETE_FRIEND_FROM:
			FriendsModel dffm = (FriendsModel) obj;
			SUBJECT = (" Your Friend \'" + dffm.getUserId() + "' has been removed from your Friend List");
			BODY = "You have now removed " + dffm.getUserId()
					+ " from your Friend List. You can no longer lease items to each other. Tell us what went wrong! ";
			break;

		case FLS_MAIL_DELETE_FRIEND_TO:
			FriendsModel dtfm = (FriendsModel) obj;
			SUBJECT = (" Your Friend '" + dtfm.getFriendId() + "' removed you from thier Friend List");
			BODY = "You have been removed from the Friend List of your Friend " + dtfm.getFriendId()
					+ ". You can no longer lease items to each other. Tell us what went wrong! ";
			break;

		case FLS_MAIL_REJECT_REQUEST_FROM:
			RequestsModel dfrm = (RequestsModel) obj;
			SUBJECT = (" Request removed");
			BODY = "Request for item with id [" + dfrm.getItemId() + "] has been removed as a lease might be granted. ";
			break;

		case FLS_MAIL_REJECT_REQUEST_TO:
			RequestsModel dtrm = (RequestsModel) obj;
			SUBJECT = (" Request removed");
			BODY = "Request of item having id [" + dtrm.getItemId() + "] has been removed by the owner as a lease might be granted. ";
			break;

		case FLS_MAIL_DELETE_REQUEST_FROM:
			ItemsModel dfim = (ItemsModel) obj;
			SUBJECT = (" Request removed");
			BODY = "Your Request for item having id [" + dfim.getId() + "] has been removed. ";
			break;

		case FLS_MAIL_DELETE_REQUEST_TO:
			ItemsModel dtim = (ItemsModel) obj;
			SUBJECT = (" Request removed");
			BODY = "Request of item having id [" + dtim.getId() + "] has been removed a Requestor. ";
			break;

		case FLS_MAIL_GRANT_LEASE_FROM:
			GrantLeaseReqObj gflm = (GrantLeaseReqObj) obj;
			SUBJECT = (" Lease granted to user [" + gflm.getReqUserId() + "]");
			BODY = "You have sucessfully leased an item to [" + gflm.getReqUserId() + "] on Friend Lease - ";
			break;

		case FLS_MAIL_GRANT_LEASE_TO:
			GrantLeaseReqObj gtlm = (GrantLeaseReqObj) obj;
			SUBJECT = (" Lease granted to you by [" + gtlm.getUserId() + "]");
			BODY = "An item has been leased by [" + gtlm.getUserId() + "] to you on Friend Lease - ";
			break;

		case FLS_MAIL_REJECT_LEASE_FROM:
			RenewLeaseReqObj rflm = (RenewLeaseReqObj) obj;
			SUBJECT = (" Lease Cancelled to user [" + rflm.getReqUserId() + "]");
			BODY = "You have closed leased of item having id [" + rflm.getItemId() + "] and leasee ["
					+ rflm.getReqUserId() + "] on Friend Lease - ";
			break;

		case FLS_MAIL_REJECT_LEASE_TO:
			RenewLeaseReqObj rtlm = (RenewLeaseReqObj) obj;
			SUBJECT = (" Lease Closed by the Owner");
			BODY = "Lease has been closed by the Owner for the item having id [" + rtlm.getItemId() + "] ";
			break;
			
		case FLS_MAIL_GRACE_PERIOD_OWNER:
			RenewLeaseReqObj rlgpo = (RenewLeaseReqObj) obj;
			SUBJECT = (" Reminder to Renew Lease to user [" + rlgpo.getReqUserId() + "]");
			BODY = "Less than 5 days left for lease to close.Please consider renewing the lease of item having id [" 
					+ rlgpo.getItemId() + "] and leasee [" + rlgpo.getReqUserId() + "] on Friend Lease - ";
			break;

		case FLS_MAIL_GRACE_PERIOD_REQUESTOR:
			RenewLeaseReqObj rlgpr = (RenewLeaseReqObj) obj;
			SUBJECT = (" Reminder to Renew Lease");
			BODY = "Less than 5 days left for lease to close. Please consider renewing the lease of item having id [" 
					+ rlgpr.getItemId() + "] ";
			break;
			
		case FLS_MAIL_RENEW_LEASE_OWNER:
			RenewLeaseReqObj rlo = (RenewLeaseReqObj) obj;
			SUBJECT = (" Renewed Lease to user [" + rlo.getReqUserId() + "]");
			BODY = "Lease has been renewed for item having id [" + rlo.getItemId() + "] and leasee [" 
					+ rlo.getReqUserId() + "] on Friend Lease - ";
			break;

		case FLS_MAIL_RENEW_LEASE_REQUESTOR:
			RenewLeaseReqObj rlr = (RenewLeaseReqObj) obj;
			SUBJECT = (" Lease Renewed");
			BODY = "Lease has been renewed by the owner of item having id [" + rlr.getItemId() + "] ";
			break;

		case FLS_MAIL_MAKE_REQUEST_FROM:
			RequestsModel rfrm = (RequestsModel) obj;
			SUBJECT = (" Item Requested");
			BODY = "You have sucessfully Requested an item having id [" + rfrm.getItemId() + "] on Friend Lease - ";
			break;

		case FLS_MAIL_MAKE_REQUEST_TO:
			ItemsModel irtm = (ItemsModel) obj;
			SUBJECT = (" Item Requested");
			BODY = "Your Item [" + irtm.getTitle() + "] having id [" + irtm.getId() + "] has been requested on Friend Lease - ";
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
			if (!env_config.equals("dev")) {
				message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(CC));
			}

			Multipart multipart = new MimeMultipart("related");

			template = ve.getTemplate("templates/defaultEmail.vm");
			context.put("credits", credit);
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
					MimeBodyPart imagePart = new MimeBodyPart();
					LOGGER.warning("Sending Image!!");
					imagePart.attachFile(imageFiles.get(j));
					imagePart.setContentID("<image" + Integer.toString(j) + ">");
					imagePart.setDisposition(MimeBodyPart.INLINE);
					multipart.addBodyPart(imagePart);
					imageFile = null;

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

			LOGGER.info("====> Attempting to send an email through Amazon SES by using the AWS SDK for Java...");

			try {
				BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAITVFAR4O56SFRG6A",
						"F1M+ak2jT+qmFygNtXmwuomqsDpA8ZaNy/GBviF/");
				// Instantiate an Amazon SES client, which will make the service
				// call with the supplied AWS credentials.
				AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient(credentials);
				LOGGER.info("=====> Created client" + client);

				// Choose the AWS region of the Amazon SES endpoint you want to
				// connect to. Note that your production
				// access status, sending limits, and Amazon SES
				// identity-related settings are specific to a given
				// AWS region, so be sure to select an AWS region in which you
				// set up Amazon SES. Here, we are using
				// the US East (N. Virginia) region. Examples of other regions
				// that Amazon SES supports are US_WEST_2
				// and EU_WEST_1. For a complete list, see
				// http://docs.aws.amazon.com/ses/latest/DeveloperGuide/regions.html
				// Region REGION = Region.getRegion(Regions.US_EAST_1);
				Region REGION = Region.getRegion(Regions.US_WEST_2);
				client.setRegion(REGION);
				LOGGER.info("=====> got REGION" + REGION);

				// Send the email.
				client.sendRawEmail(request);
				LOGGER.warning("====> Email sent!");
			} catch (Throwable e) {
				// Catching throwable instead of Exception so that we also catch
				// Errors. Not
				// needed normally but since we're debugging we don't want to
				// miss anything
				LOGGER.warning("====> " + e.getMessage());
				e.printStackTrace(System.out);
			}

		} catch (Exception ex) {
			LOGGER.warning("====> The email was not sent.");
			LOGGER.warning("====> Error message: " + ex.getMessage());
			ex.printStackTrace(System.out);
		}
	}

	private static File convertBinaryToImage(String imageString) {
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
	
	public static File createFile(InputStream is) throws IOException {
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
