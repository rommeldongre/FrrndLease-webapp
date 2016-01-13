package util;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import pojos.*;


public class FlsSendMail {
	
	private static String user_id;
	
	public enum Fls_Enum { FLS_MAIL_REGISTER, FLS_MAIL_POST_ITEM, SUMMER, FALL }
	public static void send(String userId, Fls_Enum fls_enum) throws Exception {
		//Fls_Enum = fls_enum;
		user_id = userId;
		//Class oclass = obj.getClass();
		
	    Email email = new SimpleEmail();
	    email.setSmtpPort(465);
	    email.setAuthenticator(new DefaultAuthenticator("aniruddh@greylabs.org",
	            "Windows@123"));
	    email.setDebug(true);
	    email.setHostName("smtp.gmail.com");
	    email.setFrom("aniruddh@greylabs.org");
	    
	    switch (fls_enum) {
		case FLS_MAIL_REGISTER:
			email.setSubject("SignUp Notification");
			email.setMsg(" You Have signed up ... :-)");
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
