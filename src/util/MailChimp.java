package util;

import java.io.IOException;
import java.util.Map;

import com.ecwid.maleorang.MailchimpClient;
import com.ecwid.maleorang.MailchimpObject;
import com.ecwid.maleorang.method.v3_0.lists.members.EditMemberMethod;
import com.ecwid.maleorang.method.v3_0.lists.members.MemberInfo;

public class MailChimp{
	
	private FlsLogger LOGGER = new FlsLogger(MailChimp.class.getName());
	
	private static final String MAIL_CHIMP_API_KEY ="0798aa292dbc37321458d36f9d02ba9d-us13";
	private static final String USER_LIST_ID = "5d2aa60dc0"; 
	private static final String LEAD_LIST_ID = "f8d48a3de3";
	
	public void addUserToList(String email, Map<String,String> usermap){
		
        MailchimpClient client = new MailchimpClient(MAIL_CHIMP_API_KEY);
        LOGGER.info("Inside mailchimp method ");
        try {
            EditMemberMethod.CreateOrUpdate method = new EditMemberMethod.CreateOrUpdate(USER_LIST_ID, email);
            method.status = "subscribed";
            method.merge_fields = new MailchimpObject();
            method.merge_fields.mapping.put("SIGNUP_DAT", usermap.get("SIGNUP_DAT"));
            method.merge_fields.mapping.put("FEE_EXPIRY", usermap.get("FEE_EXPIRY"));
            method.merge_fields.mapping.put("UID", usermap.get("UID"));
            method.merge_fields.mapping.put("PROFILE", usermap.get("PROFILE"));
            method.merge_fields.mapping.put("PLAN", usermap.get("PLAN"));
            method.merge_fields.mapping.put("FULL_NAME", usermap.get("FULL_NAME"));
            method.merge_fields.mapping.put("MOBILE", usermap.get("MOBILE"));
            method.merge_fields.mapping.put("SEC_ID", usermap.get("SEC_ID"));
            method.merge_fields.mapping.put("SUB_LOC", usermap.get("SUB_LOC"));
            method.merge_fields.mapping.put("LOCALITY", usermap.get("LOCALITY"));
            method.merge_fields.mapping.put("REF_CODE", usermap.get("REF_CODE"));
            method.merge_fields.mapping.put("PHOTO_ID", usermap.get("PHOTO_ID"));
            method.merge_fields.mapping.put("FRIEND_REF", usermap.get("FRIEND_REF"));
            method.merge_fields.mapping.put("CREDITS", usermap.get("CREDITS"));
            method.merge_fields.mapping.put("VERIFY", usermap.get("VERIFY"));
            method.merge_fields.mapping.put("STATUS", usermap.get("STATUS"));
            method.merge_fields.mapping.put("SIGN_SOURC", usermap.get("SIGN_SOURC"));
            method.merge_fields.mapping.put("ITEM_COUNT", usermap.get("ITEM_COUNT"));
            method.merge_fields.mapping.put("LEASE_COUN", usermap.get("LEASE_COUN"));
            method.merge_fields.mapping.put("RES_TIME", usermap.get("RES_TIME"));
            method.merge_fields.mapping.put("RES_COUNT", usermap.get("RES_COUNT"));
            method.merge_fields.mapping.put("ABOUT", usermap.get("ABOUT"));
            method.merge_fields.mapping.put("WEBSITE", usermap.get("WEBSITE"));
            method.merge_fields.mapping.put("BUS_EMAIL", usermap.get("BUS_EMAIL"));
            method.merge_fields.mapping.put("LOCATION", usermap.get("LOCATION"));
            method.merge_fields.mapping.put("BUS_NUM", usermap.get("BUS_NUM"));
            method.merge_fields.mapping.put("BUS_HOURS", usermap.get("BUS_HOURS"));

            MemberInfo member = client.execute(method);
            //client.execute(method);
            System.out.println("The new user has been successfully subscribed: " + member.toJson());

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
