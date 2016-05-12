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
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.mail.Address;
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

import pojos.FriendsModel;
import pojos.ItemsModel;
import pojos.LeasesModel;
import pojos.PostItemReqObj;
import pojos.RequestsModel;
import pojos.UsersModel;
import tableOps.Wishlist;
import util.FlsSendMail.Fls_Enum;
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

public class AwsSESEmail extends Connect{

	private static FlsLogger LOGGER = new FlsLogger(AwsSESEmail.class.getName());

	static final String FROM = "BlueMarble@frrndlease.com"; // Replace with your
															// "From" address.
															// This address must
															// be verified.
	static String TO; // Replace with a "To" address. If you have not yet
						// requested
						// production access, this address must be verified.
	static String BODY;
	static String SUBJECT;

	private static String user_id;
	static String env_config  =  FlsConfig.env;
	

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
	public static void send(String userId, Fls_Enum fls_enum, Object obj, String... apiflag) throws IOException {

		// Fls_Enum = fls_enum;
		user_id = userId;
		TO = userId;
		String EMAIL_VERIFICATION_URL;
		
		// this variable is used to store the image
		File imageFile = null;

		if(env_config.equals("dev")){
			EMAIL_VERIFICATION_URL = "http://localhost:8080/flsv2/emailverification.html";
		} else {
			EMAIL_VERIFICATION_URL = "http://www.frrndlease.com/emailverification.html";

		}
		
		getConnection();
		
		int credit = 0;
		
		try{
			// getting the credits of the user
			String sqlGetCredit = "SELECT user_credit FROM users WHERE user_id=?";
			PreparedStatement s1 = connection.prepareStatement(sqlGetCredit);
			s1.setString(1, user_id);
			ResultSet rs1 = s1.executeQuery();
			if (rs1.next()) {
				credit = rs1.getInt("user_credit");
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		
		// Build Email Subject and Body
		switch (fls_enum) {
		case FLS_MAIL_SIGNUP_VALIDATION:
			UsersModel um = (UsersModel) obj;
			SUBJECT = "[fRRndLease] Email Verification";
			BODY = "<body>Hello " + um.getFullName()
					+ ". You have successfully signed up on fRRndLease. To start using frrndlease "
					+ "you need to activate your account. Click on this link to activate your frrndlease account. <br/>"
					+ "<a href='" + EMAIL_VERIFICATION_URL + "?token=" + um.getActivation() + "'>"
					+ EMAIL_VERIFICATION_URL + "?token=" + um.getActivation() + "</a>" + "<br/></body>";
			break;
		case FLS_MAIL_REGISTER:
			UsersModel uom = (UsersModel) obj;
			SUBJECT = "[fRRndLease] Welcome Aboard";
			BODY = "<body>Hello " + uom.getFullName()
					+ ". You have successfully signed up on fRRndLease, the platform that helps you Make Space for leading the Life you love. <br/>"
					+ "We love our stuff, but are passionate about utilizing them well to get rich, positive experiences. <br/>"
					+ "Check out and follow us on our Facebook community page at <a href='http://www.facebook.com/frrndlease'>frrndlease on facebook</a>. "
					+ "We use that to make announcements and share posts.<br/>"
					+ "We are happy to have you. Now go and offer your dormant stuff on the platform!! <br/>"
					+ "Remember, it will always be yours ... and will come back after enriching your friends, whenever you want!</body>";
			break;

		case FLS_MAIL_DELETE_ITEM:
			ItemsModel idom = (ItemsModel) obj;
			SUBJECT = ("[fRRndLease] Your Item [" + idom.getTitle() + "] has been deleted from the Friend Store");
			BODY = ("<body>You have deleted the following item on fRRndLease<br/> <br/>" + " Title : " + idom.getTitle()
					+ "<br/>" + " Category : " + idom.getCategory() + "<br/>" + " Description : "
					+ idom.getDescription() + "<br/>" + " Lease Value : " + idom.getLeaseValue() + "<br/>"
					+ " Lease Term : " + idom.getLeaseTerm() + "<br/>" + " Status : " + idom.getStatus() + "<br/>"
					+ "<img src=\"cid:image\" alt=" + idom.getTitle() + " ></img>" + "</body>");
			imageFile = convertBinaryToImage(idom.getImage());
			break;

		case FLS_MAIL_POST_ITEM:
			PostItemReqObj iom = (PostItemReqObj) obj;
			SUBJECT = ("[fRRndLease] Your Item [" + iom.getTitle() + "] has been added to the Friend Store");
			BODY = ("<body>You have added the following item on fRRndLease <br/> <br/>"
					+ " Title : " + iom.getTitle()
					+ "<br/>" + " Category : " + iom.getCategory() + "<br/>" + " Description : " + iom.getDescription()
					+ "<br/>" + " Lease Value : " + iom.getLeaseValue() + "<br/>" + " Lease Term : "
					+ iom.getLeaseTerm() + "<br/>" + " Status : " + iom.getStatus() + "<br/>"
					+ "<img src=\"cid:image\" alt=" + iom.getTitle() + " ></img>" + "</body>");
			imageFile = convertBinaryToImage(iom.getImage());
			break;

		case FLS_MAIL_ADD_FRIEND_FROM:
			FriendsModel affm = (FriendsModel) obj;
			if (apiflag != null && apiflag[0] == "@api") {
				SUBJECT = ("[fRRndLease] Your Friend '" + affm.getFullName() + "' has been added to your Friend List");
				BODY = ("<body>You have added your trusted friend '" + affm.getFullName()
						+ "' to your Friend List. You can now lease items to each other <br/> <br/></body>");
			} else if (apiflag != null && apiflag[0] == "@email") {
				SUBJECT = ("[fRRndLease] Your Friend '" + (affm.getFriendId())
						+ "' has been added to your Friend List. ");
				BODY = ("<body>You have added '" + (affm.getFriendId())
						+ "' to your Friend List. You can now lease items to each other <br/> <br/></body>");
			}
			break;

		case FLS_MAIL_ADD_FRIEND_TO:
			FriendsModel atfm = (FriendsModel) obj;
			SUBJECT = ("[fRRndLease] Your Friend '" + atfm.getUserId() + "' has added you to their Friend List");
			BODY = ("<body>You are now in '" + atfm.getUserId()
					+ "'\'s Friend List. You can now lease items to each other <br/> <br/></body>");
			break;

		case FLS_MAIL_DELETE_FRIEND_FROM:
			FriendsModel dffm = (FriendsModel) obj;
			SUBJECT = ("[fRRndLease] Your Friend \'" + dffm.getUserId() + "' has been removed from your Friend List");
			BODY = ("<body>You have now removed " + dffm.getUserId()
					+ " from your Friend List. You can no longer lease items to each other. Tell us what went wrong! <br/> <br/></body>");
			break;

		case FLS_MAIL_DELETE_FRIEND_TO:
			FriendsModel dtfm = (FriendsModel) obj;
			SUBJECT = ("[fRRndLease] Your Friend '" + dtfm.getFriendId() + "' removed you from thier Friend List");
			BODY = ("<body> You have been removed from the Friend List of your Friend " + dtfm.getFriendId()
					+ ". You can no longer lease items to each other. Tell us what went wrong! <br/> <br/></body>");
			break;

		case FLS_MAIL_REJECT_REQUEST_FROM:
			RequestsModel dfrm = (RequestsModel) obj;
			SUBJECT = ("[fRRndLease] Request removed");
			BODY = ("<body>Request for item with id [" + dfrm.getItemId()
					+ "] has been removed as a lease might be granted. <br/> <br/></body>");
			break;

		case FLS_MAIL_REJECT_REQUEST_TO:
			RequestsModel dtrm = (RequestsModel) obj;
			SUBJECT = ("[fRRndLease] Request removed");
			BODY = ("<body>Request of item having id [" + dtrm.getItemId()
					+ "] has been removed by the owner as a lease might be granted. <br/> <br/></body>");
			break;

		case FLS_MAIL_DELETE_REQUEST_FROM:
			ItemsModel dfim = (ItemsModel) obj;
			SUBJECT = ("[fRRndLease] Request removed");
			BODY = ("<body>Your Request for item having id [" + dfim.getId()
					+ "] has been removed. <br/> <br/></body>");
			break;

		case FLS_MAIL_DELETE_REQUEST_TO:
			ItemsModel dtim = (ItemsModel) obj;
			SUBJECT = ("[fRRndLease] Request removed");
			BODY = ("<body>Request of item having id [" + dtim.getId()
					+ "] has been removed a Requestor. <br/> <br/></body>");
			break;

		case FLS_MAIL_GRANT_LEASE_FROM:
			LeasesModel gflm = (LeasesModel) obj;
			SUBJECT = ("[fRRndLease] Lease granted to user [" + gflm.getReqUserId() + "]");
			BODY = ("<body>You have sucessfully leased an item to [" + gflm.getReqUserId()
					+ "] on Friend Lease - <br/> <br/></body>");
			break;

		case FLS_MAIL_GRANT_LEASE_TO:
			LeasesModel gtlm = (LeasesModel) obj;
			SUBJECT = ("[fRRndLease] Lease granted to you by [" + gtlm.getUserId() + "]");
			BODY = ("<body>An item has been leased by [" + gtlm.getUserId()
					+ "] to you on Friend Lease - <br/> <br/></body>");
			break;

		case FLS_MAIL_REJECT_LEASE_FROM:
			LeasesModel rflm = (LeasesModel) obj;
			SUBJECT = ("[fRRndLease] Lease Cancelled to user [" + rflm.getReqUserId() + "]");
			BODY = ("<body>You have closed leased of item having id [" + rflm.getItemId() + "] and leasee ["
					+ rflm.getReqUserId() + "] on Friend Lease - <br/> <br/></body>");
			break;

		case FLS_MAIL_REJECT_LEASE_TO:
			LeasesModel rtlm = (LeasesModel) obj;
			SUBJECT = ("[fRRndLease] Lease Closed by the Owner");
			BODY = ("<body>Lease has been closed by the Owner for the item having id [" + rtlm.getItemId()
					+ "] <br/> <br/></body>");
			break;

		case FLS_MAIL_MAKE_REQUEST_FROM:
			RequestsModel rfrm = (RequestsModel) obj;
			SUBJECT = ("[fRRndLease] Item Requested");
			BODY = ("<body>You have sucessfully Requested an item having id [" + rfrm.getItemId()
					+ "] on Friend Lease - <br/> <br/></body>");
			break;

		case FLS_MAIL_MAKE_REQUEST_TO:
			ItemsModel irtm = (ItemsModel) obj;
			SUBJECT = ("[fRRndLease] Item Requested");
			BODY = ("<body>Your Item [" + irtm.getTitle() + "] having id [" + irtm.getId()
					+ "] has been requested on Friend Lease - <br/> <br/></body>");
			break;

		default:
			SUBJECT = ("[fRRndLease] Default Subject");
			BODY = ("<body>Default Message ... Contact us, you should never get this! </body>");
			break;
		}

		try {

			// getting a default session
			Session session = Session.getDefaultInstance(new Properties());

			// mime message type from javax.mail library
			MimeMessage message = new MimeMessage(session);
			message.setSubject(SUBJECT, "UTF-8");

			// setting the basic properties of the email message
			message.setFrom(new InternetAddress(FROM));
			message.setReplyTo(new Address[] { new InternetAddress(FROM) });
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(TO));

			Multipart multipart = new MimeMultipart("related");
			
			// Body part of the email
			MimeBodyPart bodyPart = new MimeBodyPart();
			bodyPart.setContent("<u>Account Status</u>: <br/>You have <strong>" + credit + " credits</strong> left to spend on frrndlease.<br/><br/><u>Activity</u>: <br/>" + BODY, "text/html");
			multipart.addBodyPart(bodyPart);
			
			// Image part if the message has an image
			if (imageFile != null) {
				MimeBodyPart imagePart = new MimeBodyPart();
				LOGGER.warning("Sending Image!!");
				imagePart.attachFile(imageFile);
				imagePart.setContentID("<image>");
				imagePart.setDisposition(MimeBodyPart.INLINE);
				multipart.addBodyPart(imagePart);
				imageFile = null;
			}

			message.setContent(multipart);

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
}
