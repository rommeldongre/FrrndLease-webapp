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
import java.io.BufferedReader;
import java.io.InputStreamReader;

import pojos.FriendsModel;
import pojos.ItemsModel;
import pojos.LeasesModel;
import pojos.RequestsModel;
import pojos.UsersModel;
import util.FlsSendMail.Fls_Enum;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.auth.BasicAWSCredentials;

public class AwsSESEmail {

    static final String FROM = "TheGuys@frrndlease.com";  // Replace with your "From" address. This address must be verified.
    static String TO ; 										// Replace with a "To" address. If you have not yet requested
                                                      		// production access, this address must be verified.
    static String BODY ;
    static String SUBJECT ;

	private static String user_id;
	
  
    /*
     * Before running the code:
     *      Fill in your AWS access credentials in the provided credentials
     *      file template, and be sure to move the file to the default location
     *      (~/.aws/credentials) where the sample code will load the
     *      credentials from.
     *      https://console.aws.amazon.com/iam/home?#security_credential
     *
     * WANRNING:
     *      To avoid accidental leakage of your credentials, DO NOT keep
     *      the credentials file in your source directory.
     */
	public static void send(String userId, Fls_Enum fls_enum, Object obj, String... apiflag) throws IOException {

		//Fls_Enum = fls_enum;
		user_id = userId;
		TO = userId;
		
		//Build Email Subject and Body 
		 switch (fls_enum) {
			case FLS_MAIL_REGISTER:
				UsersModel uom = (UsersModel) obj;
				SUBJECT = "SignUp Notification";
				BODY = uom.getFullName()+" You Have signed up ... :-)";
				break;
				
			case FLS_MAIL_DELETE_ITEM:
				ItemsModel idom = (ItemsModel) obj;
				SUBJECT = ("Item ["+idom.getTitle()+"] deleted from friendlease");
				BODY = ("You have deleted a item, the details of which are:- \n \n"+
				              " Title : "+idom.getTitle()+"\n"+
				              " Category : "+idom.getCategory()+"\n"+
				              " Description : "+idom.getDescription()+"\n"+
				              " Lease Value : "+idom.getLeaseValue()+"\n"+
						      " Lease Term : "+idom.getLeaseTerm()+"\n"+
						      " Status : "+idom.getStatus());      
				break;
				
			case FLS_MAIL_POST_ITEM:
				ItemsModel iom = (ItemsModel) obj;
				SUBJECT = ("Item ["+iom.getTitle()+"] added to friendlease");
				BODY = ("You have added an item, the details of which are:- \n \n"+
				              " Title : "+iom.getTitle()+"\n"+
				              " Category : "+iom.getCategory()+"\n"+
				              " Description : "+iom.getDescription()+"\n"+
				              " Lease Value : "+iom.getLeaseValue()+"\n"+
						      " Lease Term : "+iom.getLeaseTerm()+"\n"+
						      " Status : "+iom.getStatus());      
				break;
				
			case FLS_MAIL_ADD_FRIEND_FROM:
				FriendsModel affm = (FriendsModel) obj;
				if (apiflag != null && apiflag[0]=="@api") {
					SUBJECT = ("["+affm.getFullName()+"] added to Friendlist");
					BODY = ( "You have sucessfully added ["
							+ affm.getFullName()
							+ "] to your Friendlist- \n \n");  
				}else if (apiflag != null && apiflag[0]=="@email"){
					SUBJECT = ("[" 
							+ (affm.getFriendId()) 
							+ "] added to Friendlist");
					BODY = ("You have sucessfully added [" 
								+ (affm.getFriendId()) 
								+ "] to your Friendlist- \n \n");
				}
				break;
			
			case FLS_MAIL_ADD_FRIEND_TO:
				FriendsModel atfm = (FriendsModel) obj;
				SUBJECT = ("["+atfm.getUserId()+"] added you");
				BODY = ("You have been connected to ["+atfm.getUserId()+"] on Friend Lease- \n \n");      
				break;
				
			case FLS_MAIL_DELETE_FRIEND_FROM:
				FriendsModel dffm = (FriendsModel) obj;
				SUBJECT = (dffm.getUserId()+" removed from Friendlist");
				BODY = ("You have sucessfully removed "+dffm.getUserId()+" from your Friendlist- \n \n");      
				break;
				
			case FLS_MAIL_DELETE_FRIEND_TO:
				FriendsModel dtfm = (FriendsModel) obj;
				SUBJECT = ("["+dtfm.getFriendId()+"] removed You");
				BODY = ("You have been removed from Friendlist of user "+dtfm.getFriendId()+"- \n \n");      
				break;
				
			case FLS_MAIL_REJECT_REQUEST_FROM:
				RequestsModel dfrm = (RequestsModel) obj;
				SUBJECT = ("Request removed");
				BODY = ("Request for item having id ["+dfrm.getItemId()+"] has been removed as a lease might be granted. \n \n");      
				break;
				
			case FLS_MAIL_REJECT_REQUEST_TO:
					RequestsModel dtrm = (RequestsModel) obj;
					SUBJECT = ("Request removed");
					BODY = ("Request of item having id ["+dtrm.getItemId()+"] has been removed by the owner as a lease might be granted. \n \n");
				break;
				
			case FLS_MAIL_DELETE_REQUEST_FROM:
				ItemsModel dfim = (ItemsModel) obj;
				SUBJECT = ("Request removed");
				BODY = ("Your Request for item having id ["+dfim.getId()+"] has been removed. \n \n");      
				break;
				
			case FLS_MAIL_DELETE_REQUEST_TO:
				ItemsModel dtim = (ItemsModel) obj;
					SUBJECT = ("Request removed");
					BODY = ("Request of item having id ["+dtim.getId()+"] has been removed a Requestor. \n \n");
				break;
				
			case FLS_MAIL_GRANT_LEASE_FROM:
				LeasesModel gflm = (LeasesModel) obj;
				SUBJECT = ("Lease granted to user ["+gflm.getReqUserId()+"]");
				BODY = ("You have sucessfully leased an item to ["+gflm.getReqUserId()+"] on Friend Lease - \n \n");      
				break;
				
			case FLS_MAIL_GRANT_LEASE_TO:
				LeasesModel gtlm = (LeasesModel) obj;
				SUBJECT = ("Lease granted to you by ["+gtlm.getUserId()+"]");
				BODY = ("An item has been leased by ["+gtlm.getUserId()+"] to you on Friend Lease - \n \n");      
				break;
				
			case FLS_MAIL_REJECT_LEASE_FROM:
				LeasesModel rflm = (LeasesModel) obj;
				SUBJECT = ("Lease Cancelled to user ["+rflm.getReqUserId()+"]");
				BODY = ("You have closed leased of item having id ["+rflm.getItemId()+"] and leasee ["+rflm.getReqUserId()+"] on Friend Lease - \n \n");      
				break;
				
			case FLS_MAIL_REJECT_LEASE_TO:
				LeasesModel rtlm = (LeasesModel) obj;
				SUBJECT = ("Lease Closed by the Owner");
				BODY = ("Lease has been closed by the Owner for the item having id ["+rtlm.getItemId()+"] \n \n");      
				break;
			 
			case FLS_MAIL_MAKE_REQUEST_FROM:
				RequestsModel rfrm = (RequestsModel) obj;
				SUBJECT = ("Item Requested");
				BODY = ("You have sucessfully Requested an item having id ["+rfrm.getItemId()+"] on Friend Lease - \n \n");      
				break;
				
			case FLS_MAIL_MAKE_REQUEST_TO:
				ItemsModel irtm = (ItemsModel) obj;
				SUBJECT = ("Item Requested");
				BODY = ("Your Item ["+irtm.getTitle()+"] having id ["+irtm.getId()+"] has been requested on Friend Lease - \n \n");      
				break;
				
			default:
				SUBJECT = ("Default Subject");
				BODY = ("Default Message ... :-)");
				break;
			}
		 
		
        // Construct an object to contain the recipient address.
        Destination destination = new Destination().withToAddresses(new String[]{TO});

        // Create the subject and body of the message.
        Content subject = new Content().withData(SUBJECT);
        Content textBody = new Content().withData(BODY);
        Body body = new Body().withText(textBody);

        // Create a message with the specified subject and body.
        Message message = new Message().withSubject(subject).withBody(body);

        // Assemble the email.
        SendEmailRequest request = new SendEmailRequest().withSource(FROM).withDestination(destination).withMessage(message);

        try {
            System.out.println("====> Attempting to send an email through Amazon SES by using the AWS SDK for Java...");
            
                		
    		
                        
            try {
            	BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAITVFAR4O56SFRG6A","F1M+ak2jT+qmFygNtXmwuomqsDpA8ZaNy/GBviF/");
            	// Instantiate an Amazon SES client, which will make the service call with the supplied AWS credentials.
            	AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient(credentials);
            	System.out.println("=====> Created client" + client);
            
            	// Choose the AWS region of the Amazon SES endpoint you want to connect to. Note that your production
            	// access status, sending limits, and Amazon SES identity-related settings are specific to a given
            	// AWS region, so be sure to select an AWS region in which you set up Amazon SES. Here, we are using
            	// the US East (N. Virginia) region. Examples of other regions that Amazon SES supports are US_WEST_2
            	// and EU_WEST_1. For a complete list, see http://docs.aws.amazon.com/ses/latest/DeveloperGuide/regions.html
            	//Region REGION = Region.getRegion(Regions.US_EAST_1);
            	Region REGION = Region.getRegion(Regions.US_WEST_2);
            	client.setRegion(REGION);
            	System.out.println("=====> got REGION" + REGION);

            	// Send the email.
            	client.sendEmail(request);
            	System.out.println("====> Email sent!");
            }
            catch (Throwable e) {
            	  // Catching throwable instead of Exception so that we also catch Errors. Not
            	  // needed normally but since we're debugging we don't want to miss anything
            	  System.out.println("====> " + e.getMessage());
            	  e.printStackTrace(System.out);
            }


        } catch (Exception ex) {
            System.out.println("====> The email was not sent.");
            System.out.println("====> Error message: " + ex.getMessage());
        }
    }
}
