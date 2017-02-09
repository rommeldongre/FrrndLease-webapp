package util;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

import util.FlsCredit.Credit;
import connect.Connect;

public class FlsBot extends Connect{
	
	private FlsLogger LOGGER = new FlsLogger(FlsBot.class.getName());
	
	private static final String access_token = "EAABiKoMD4QQBAEShbRZBBRZAg3bgWWR14SRsqZABqZBDhVpEh4Kgz2pHnLaS29iNuqKQMhLUhWbIibmKnzWsiVaamD5X1FluMvvNZC8eVJLZB4cgmq42abAPrZCl3mBuxd19LZArvPgyGdMGAzhkilRxeDmbZCT5R8u3x6HZCa20zTigZDZD";
	private String FB_URL = "https://graph.facebook.com/v2.6/me/";
	
	private String URL = FlsConfig.prefixUrl;
	private String item_title=null,item_primary_image=null,item_desc=null,item_public_url=null;
	int item_offset=0;
	
	
	public String sendBotMessage(String userId, String text, int offset){
		String botMessage = null;
		
		LOGGER.info("Text from page is "+text);
		boolean itemFound  = fetchItem(text,offset);
		if(!itemFound){
			String Message = "Sorry Please search for another item";
			
			JSONObject root = new JSONObject();
	        JSONObject c0 = new JSONObject();
	        JSONObject c1 = new JSONObject();

	        root.put("recipient", c0);
	        root.put("message", c1);

	        c0.put("id", userId);
	        c1.put("text", Message);
	        botMessage= root.toString();
		}else{
			
			 JSONObject root = new JSONObject();
		        JSONObject c0 = new JSONObject();
		        JSONObject c1 = new JSONObject();

		        JSONObject attachment = new JSONObject();
		        JSONObject payload = new JSONObject();
		       // JSONObject buttons = new JSONObject();
		        JSONArray buttons = new JSONArray();
		        JSONObject button1 = new JSONObject();
		        JSONObject button2 = new JSONObject();
		        JSONObject button3 = new JSONObject();

		        root.put("recipient", c0);
		        root.put("message", c1);

		        c0.put("id", userId);
		        c1.put("attachment", attachment);

		        attachment.put("type", "template");
		        attachment.put("payload", payload);

		        payload.put("template_type", "button");
		        payload.put("text", "An Item was found: "+item_title);
		        payload.put("buttons", buttons);

		        
		        button1.put("type", "web_url");
		        button1.put("url", item_public_url);
		        button1.put("title", "show Item");
		        buttons.put(button1);
		        
		        button2.put("type", "postback");
		        button2.put("title", "Next Item");
		        button2.put("payload", text+"="+item_offset);
		        buttons.put(button2);
		        
		        button3.put("type", "web_url");
		        button3.put("url", URL);
		        button3.put("title", "SignUp");
		        buttons.put(button3);
		        /*JSONObject root1 = new JSONObject();
	        JSONObject c01 = new JSONObject();
	        JSONObject c11 = new JSONObject();
	        
	        JSONObject attachment = new JSONObject();
	        JSONObject payload = new JSONObject();
	        JSONArray arrayButton= new JSONArray();
	        JSONArray arrayelements= new JSONArray();
	        JSONObject elementsObj = new JSONObject();
	        JSONObject defaultAction = new JSONObject();
	        
	        JSONObject buttons1 = new JSONObject();
	        JSONObject buttons2 = new JSONObject();

	        root1.put("recipient", c01);
	        	c01.put("id", userId);
	        
	        root1.put("message", c11);
	        	c11.put("attachment", attachment);
	        		attachment.put("type", "template");
	        		attachment.put("payload", payload);
	        			payload.put("template_type", "generic");
	        			payload.put("elements", arrayelements);
	        				arrayelements.put(elementsObj);
	        					elementsObj.put("title", "Sample Title");
	        					elementsObj.put("image_url", "https://s3-ap-southeast-1.amazonaws.com/fls-items-dev/sample-item-4-95/post/sample-item-4-95-primary-4495.png");
	        					elementsObj.put("subtitle", "Sample Sub Title");
	        					elementsObj.put("default_action", defaultAction);
	        
	        						defaultAction.put("type", "web_url");
	        						defaultAction.put("url", "https://www.frrndlease.com/");
	        						defaultAction.put("messenger_extensions", true);
	        						defaultAction.put("webview_height_ratio", "tall");
	        						defaultAction.put("fallback_url", "https://www.frrndlease.com/");
	        
	        					
	        						
	        						buttons1.put("type", "web_url");
	        						buttons1.put("url", "https://google.com");
	        						buttons1.put("title", "show website");
	        					arrayButton.put(buttons1);

	        						
	        						buttons2.put("type", "postback");
	        						buttons2.put("title", "Hi There");
	        						buttons2.put("payload", "sample payload");
	        					arrayButton.put(buttons2);
	        					
	        					elementsObj.put("buttons", arrayButton);*/
	        
	        String botMessage1= root.toString();
	        botMessage= root.toString();
	        //botMessage = "Sorry I could not find this item. Please try search for another item";
	        LOGGER.info("JSON Output "+botMessage1);
		}
		
		return botMessage;
		
	}
	
	public int addPersistentMenu(String payload, String type){
		int responseCode= 0;
		
		LOGGER.info("Inside addPersistentMenu Method");
		HttpURLConnection httpcon;
		try {
			httpcon = (HttpURLConnection) ((new URL(FB_URL+"messages?access_token="+access_token).openConnection()));
			httpcon.setDoOutput(true);
			httpcon.setRequestProperty("Content-Type", "application/json");
			httpcon.setRequestMethod("POST");
			httpcon.connect();
			
			final OutputStreamWriter osw = new OutputStreamWriter(httpcon.getOutputStream());
			osw.write(payload);
			osw.close();
			
			responseCode = httpcon.getResponseCode();
			System.out.println("Response Code for JSON POST : " + responseCode);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return responseCode;
	}
	
	
	public String postBackMessage(String postback){
		String PostbackMessage = null,payloadMessage=null;
		
		LOGGER.info("Inside PostBAck Method");
		JSONObject row = new JSONObject(postback);
	    System.out.println("JSON Object "+row);
	    
	    JSONArray rows = row.getJSONArray("entry");
	    JSONObject first = rows.getJSONObject(0);
        System.out.println("entry array is"+rows);
        System.out.println("first array is "+first);
        
        JSONArray message_rows = first.getJSONArray("messaging");
        JSONObject first1 = message_rows.getJSONObject(0);
        System.out.println("message array is"+rows);
        System.out.println("first1 array is "+first1);
        JSONObject messageText = first1.getJSONObject("postback");
        System.out.println("final message array is"+messageText);
        
        //JSONObject messageText2 = messageText.getJSONObject("text");
        System.out.println("final post back message array is"+messageText.getString("payload"));
        
        JSONObject senderText = first1.getJSONObject("sender");
        System.out.println("final postback sender id is"+senderText.getString("id"));
        
        if(messageText.getString("payload").contains("=")){
        	String[] parts = messageText.getString("payload").split("=");
            String last_text = parts[0]; 
            String last_offset = parts[1]; 
            
            int last_offset_int = Integer.parseInt(last_offset);
        
            LOGGER.info("Inside PostBAck Method call"+ senderText.getString("id")+" "+last_text+" "+last_offset_int);
            PostbackMessage = sendBotMessage(senderText.getString("id"),last_text,last_offset_int);
        }else{
        	 switch (messageText.getString("payload")) {
    		case "item_search":
    		case "Item Search":
    			
    			payloadMessage = "What Item would you like to search??";
    			break;

    		default:
    			payloadMessage = "Sorry Please search for another item";
    			break;
    		}
        	 
        	 JSONObject root = new JSONObject();
             JSONObject c0 = new JSONObject();
             JSONObject c1 = new JSONObject();

             root.put("recipient", c0);
             root.put("message", c1);

             c0.put("id", senderText.getString("id"));
             c1.put("text", payloadMessage);
             PostbackMessage = root.toString();
             //System.out.println(PostbackMessage);
        }
        
        return PostbackMessage;
		
	} 
	
	public boolean fetchItem(String text,int offset){
		boolean item_status = false;
		
		LOGGER.info("Inside logCredit Method");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1=null, rs2=null;
		
		String item_uid= null;
		try {
			String sqlFetchItem = "SELECT * from `items` WHERE item_name LIKE '%"+text+"%' ORDER BY item_id DESC LIMIT "+offset+", 1";
			ps1 = hcp.prepareStatement(sqlFetchItem);
			//ps1.setString(1, userId);

			rs1 = ps1.executeQuery();

			if (rs1.next()) {
				item_title  = rs1.getString("item_name");
				item_uid =  rs1.getString("item_uid");
				item_public_url = URL + "/ItemDetails?uid="+item_uid;
				item_offset = offset+1;
				item_status = true;
				
			} else {
				LOGGER.info("No Item Marching String : " + text);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(ps2 != null) ps2.close();
				if(ps1 != null) ps1.close();
				if(rs1 != null) rs1.close();
				if(hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return item_status;
	}
		
	
}
