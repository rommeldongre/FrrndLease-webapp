package util;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;


public class FlsSendMail {
	
	private static String user_id;
	
	public static void send(String userId) throws Exception {
		
		user_id = userId;
	    Email email = new SimpleEmail();
	    email.setSmtpPort(465);
	    email.setAuthenticator(new DefaultAuthenticator("aniruddh@greylabs.org",
	            "Windows@123"));
	    email.setDebug(true);
	    email.setHostName("smtp.gmail.com");
	    email.setFrom("aniruddh@greylabs.org");
	    email.setSubject("SignUp Notification");
	    email.setMsg("You Have signed up ... :-)");
	    email.addTo(user_id);
	    email.setSSLOnConnect(true);
	    email.send();
	    System.out.println("Mail sent!");
	}

}
