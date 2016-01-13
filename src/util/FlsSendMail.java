package util;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import pojos.*;


public class FlsSendMail {
	
	private static String user_id;
	
	public enum Fls_Enum { FLS_MAIL_REGISTER, FLS_MAIL_POST_ITEM, FLS_MAIL_DELETE_ITEM, FLS_MAIL_GRANT_REQUEST, FLS_MAIL_REJECT_REQUEST, FLS_MAIL_ADD_FRIEND, FLS_MAIL_DELETE_FRIEND, FLS_MAIL_GRANT_lEASE, FLS_MAIL_REJECT_lEASE   }
	public static void send(String userId, Fls_Enum fls_enum, Object obj) throws Exception {
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
			
		case FLS_MAIL_POST_ITEM:
			ItemsModel iom = (ItemsModel) obj;
			email.setSubject("Item "+iom.getTitle()+" Added to friendlease");
			email.setMsg("Item "+iom.getTitle()+" belonging to category "+iom.getCategory()+" has been added");
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
