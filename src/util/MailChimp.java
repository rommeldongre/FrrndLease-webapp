package util;

import java.io.IOException;

import org.json.JSONObject;

import com.ecwid.maleorang.MailchimpClient;
import com.ecwid.maleorang.MailchimpObject;
import com.ecwid.maleorang.method.v3_0.lists.members.EditMemberMethod;
import com.ecwid.maleorang.method.v3_0.lists.members.GetMemberMethod;
import com.ecwid.maleorang.method.v3_0.lists.members.MemberInfo;

public class MailChimp {
	
	private FlsLogger LOGGER = new FlsLogger(MailChimp.class.getName());
	
	private static final String MAIL_CHIMP_API_KEY ="0798aa292dbc37321458d36f9d02ba9d-us13";
	private static final String USER_LIST_ID = "2aebe3dba5"; 
	private static final String LEAD_LIST_ID = "f8d48a3de3";
	
	public void addMemberToList(){
		
        MailchimpClient client = new MailchimpClient(MAIL_CHIMP_API_KEY);
        LOGGER.info("Inside mailchimp method ");
        try {
            EditMemberMethod.CreateOrUpdate method = new EditMemberMethod.CreateOrUpdate(USER_LIST_ID, "abc@xyz.com");
            method.status = "subscribed";
            method.merge_fields = new MailchimpObject();
            method.merge_fields.mapping.put("FNAME", "Sample First Name");
            method.merge_fields.mapping.put("LNAME", "Sample Last Name");
            method.merge_fields.mapping.put("FULLNAME", "Sample Full Name");
            method.merge_fields.mapping.put("LOCATION", "Pune");
            method.merge_fields.mapping.put("MOBILE", "1234567890");

            MemberInfo member = client.execute(method);
            client.execute(method);
            System.out.println("The user has been successfully subscribed: " + member.toJson());

        }catch(Exception e){
        	LOGGER.info("Exception inside RunMailChimp method");
        	e.printStackTrace();
        } finally {
            try {
				client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.info("Exception while closing mailchimp client");
				e.printStackTrace();
			}
        }
    }
	
public void addLeadToList(int leadID,String leadEmail,String leadType, String leadUrl,String leadTime){
		 
        MailchimpClient client = new MailchimpClient(MAIL_CHIMP_API_KEY);
        LOGGER.info("Inside mailchimp method ");
        
        try {
            EditMemberMethod.CreateOrUpdate method = new EditMemberMethod.CreateOrUpdate(LEAD_LIST_ID, leadEmail);
            method.status = "subscribed";
            method.merge_fields = new MailchimpObject();
            method.merge_fields.mapping.put("LEAD_TYPE", leadType);
            method.merge_fields.mapping.put("LEAD_URL", leadUrl);
            method.merge_fields.mapping.put("LEAD_DATE", leadTime);
            method.merge_fields.mapping.put("LEAD_ID", leadID);

            client.execute(method);

        }catch(Exception e){
        	LOGGER.info("Exception inside RunMailChimp method");
        	e.printStackTrace();
        } finally {
            try {
				client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.info("Exception while closing mailchimp client");
				e.printStackTrace();
			}
        }
    }

}
