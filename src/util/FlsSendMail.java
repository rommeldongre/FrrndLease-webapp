package util;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import pojos.*;


public class FlsSendMail {
	
	private static String user_id;
	
	public enum Fls_Enum { FLS_MAIL_REGISTER, //done
						   FLS_MAIL_POST_ITEM, //done
						   FLS_MAIL_DELETE_ITEM, //done
						   FLS_MAIL_MAKE_REQUEST_FROM, //done
						   FLS_MAIL_MAKE_REQUEST_TO, // done
						   FLS_MAIL_GRANT_REQUEST_FROM, //same as grant lease from
						   FLS_MAIL_GRANT_REQUEST_TO, //same as grant lease to
						   FLS_MAIL_REJECT_REQUEST_FROM, // working partially. Set flag for lease part(done)
						   FLS_MAIL_REJECT_REQUEST_TO, // not done as pojo needs to be filled
						   FLS_MAIL_ADD_FRIEND_FROM, //done
						   FLS_MAIL_ADD_FRIEND_TO, //done
						   FLS_MAIL_DELETE_FRIEND_FROM, //done
						   FLS_MAIL_DELETE_FRIEND_TO, //done
						   FLS_MAIL_GRANT_LEASE_FROM,//done
						   FLS_MAIL_GRANT_LEASE_TO,//done
						   FLS_MAIL_REJECT_LEASE_FROM, // not done pojo needs to be filled   //done
						   FLS_MAIL_REJECT_LEASE_TO } //done
	public static void send(String userId, Fls_Enum fls_enum, Object obj, String... apiflag) throws Exception {
		//Fls_Enum = fls_enum;
		user_id = userId;
		
	    Email email = new SimpleEmail();
	    email.setSmtpPort(465);
	    email.setAuthenticator(new DefaultAuthenticator("aniruddh@greylabs.org",
	            "Windows@123"));
	    email.setDebug(true);
	    email.setHostName("smtp.gmail.com");
	    email.setFrom("aniruddh@greylabs.org");
	    
	    switch (fls_enum) {
		case FLS_MAIL_REGISTER:
			UsersModel uom = (UsersModel) obj;
			email.setSubject("SignUp Notification");
			email.setMsg(uom.getFullName()+" You Have signed up ... :-)");
			break;
			
		case FLS_MAIL_DELETE_ITEM:
			ItemsModel idom = (ItemsModel) obj;
			email.setSubject("Item ["+idom.getTitle()+"] deleted from friendlease");
			email.setMsg("You have deleted a item, the details of which are:- \n \n"+
			              " Title : "+idom.getTitle()+"\n"+
			              " Category : "+idom.getCategory()+"\n"+
			              " Description : "+idom.getDescription()+"\n"+
			              " Lease Value : "+idom.getLeaseValue()+"\n"+
					      " Lease Term : "+idom.getLeaseTerm()+"\n"+
					      " Status : "+idom.getStatus());      
			break;
			
		case FLS_MAIL_POST_ITEM:
			ItemsModel iom = (ItemsModel) obj;
			email.setSubject("Item ["+iom.getTitle()+"] added to friendlease");
			email.setMsg("You have added an item, the details of which are:- \n \n"+
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
				email.setSubject("["+affm.getFullName()+"] added to Friendlist");
				email.setMsg( "You have sucessfully added ["
						+ affm.getFullName()
						+ "] to your Friendlist- \n \n");  
			}else if (apiflag != null && apiflag[0]=="@email"){
				email.setSubject("[" 
						+ (affm.getFriendId()) 
						+ "] added to Friendlist");
				email.setMsg("You have sucessfully added [" 
							+ (affm.getFriendId()) 
							+ "] to your Friendlist- \n \n");
			}
			break;
		
		case FLS_MAIL_ADD_FRIEND_TO:
			FriendsModel atfm = (FriendsModel) obj;
			email.setSubject("["+atfm.getUserId()+"] added you");
			email.setMsg("You have been connected to ["+atfm.getUserId()+"] on Friend Lease- \n \n");      
			break;
			
		case FLS_MAIL_DELETE_FRIEND_FROM:
			FriendsModel dffm = (FriendsModel) obj;
			email.setSubject(dffm.getUserId()+" removed from Friendlist");
			email.setMsg("You have sucessfully removed "+dffm.getUserId()+" from your Friendlist- \n \n");      
			break;
			
		case FLS_MAIL_DELETE_FRIEND_TO:
			FriendsModel dtfm = (FriendsModel) obj;
			email.setSubject("["+dtfm.getFriendId()+"] removed You");
			email.setMsg("You have been removed from Friendlist of user "+dtfm.getFriendId()+"- \n \n");      
			break;
			
		case FLS_MAIL_REJECT_REQUEST_FROM:
			RequestsModel dfrm = (RequestsModel) obj;
			email.setSubject("Request removed");
			email.setMsg("Request for item having id ["+dfrm.getItemId()+"] has been removed as a lease might be granted. \n \n");      
			break;
			
		case FLS_MAIL_REJECT_REQUEST_TO:
			RequestsModel dtrm = (RequestsModel) obj;
			email.setSubject("Request removed");
			email.setMsg("Request of item having id ["+dtrm.getItemId()+"] has been removed by the owner as a lease might be granted. \n \n");      
			break;
			
		case FLS_MAIL_GRANT_LEASE_FROM:
			LeasesModel gflm = (LeasesModel) obj;
			email.setSubject("Lease granted to user ["+gflm.getReqUserId()+"]");
			email.setMsg("You have sucessfully leased an item to ["+gflm.getReqUserId()+"] on Friend Lease - \n \n");      
			break;
			
		case FLS_MAIL_GRANT_LEASE_TO:
			LeasesModel gtlm = (LeasesModel) obj;
			email.setSubject("Lease granted to you by ["+gtlm.getUserId()+"]");
			email.setMsg("An item has been leased by ["+gtlm.getUserId()+"] to you on Friend Lease - \n \n");      
			break;
			
		case FLS_MAIL_REJECT_LEASE_FROM:
			LeasesModel rflm = (LeasesModel) obj;
			email.setSubject("Lease Cancelled to user ["+rflm.getReqUserId()+"]");
			email.setMsg("You have closed leased of item having id ["+rflm.getItemId()+"] and leasee ["+rflm.getReqUserId()+"] on Friend Lease - \n \n");      
			break;
			
		case FLS_MAIL_REJECT_LEASE_TO:
			LeasesModel rtlm = (LeasesModel) obj;
			email.setSubject("Lease Closed by the Owner");
			email.setMsg("Lease has been closed by the Owner for the item having id ["+rtlm.getItemId()+"] \n \n");      
			break;
		 
		case FLS_MAIL_MAKE_REQUEST_FROM:
			RequestsModel rfrm = (RequestsModel) obj;
			email.setSubject("Item Requested");
			email.setMsg("You have sucessfully Requested an item having id ["+rfrm.getItemId()+"] on Friend Lease - \n \n");      
			break;
			
		case FLS_MAIL_MAKE_REQUEST_TO:
			ItemsModel irtm = (ItemsModel) obj;
			email.setSubject("Item Requested");
			email.setMsg("Your Item ["+irtm.getTitle()+"] having id ["+irtm.getId()+"] has been requested on Friend Lease - \n \n");      
			break;
			
		default:
			email.setSubject("Default Subject");
			email.setMsg("Default Message ... :-)");
			break;
		}
	    email.addTo(user_id);
	    email.setSSLOnConnect(true);
	    email.send();
	    System.out.println("Mail sent!");
	    System.out.println("Printing object");
	}

}
