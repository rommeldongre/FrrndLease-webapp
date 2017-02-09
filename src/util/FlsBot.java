package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

import connect.Connect;

public class FlsBot extends Connect{
	
	private FlsLogger LOGGER = new FlsLogger(FlsBot.class.getName());
	
	private String URL = FlsConfig.prefixUrl;
	private String item_title=null,item_public_url=null;
	int item_offset=0;
	
	
	public String sendBotMessage(String userId, String text, int offset){
		String botMessage = null;
		
		LOGGER.info("Text from page is "+text);
		boolean itemFound  = fetchItem(text,offset);
		if(!itemFound){
			String Message =null;
			if(offset==0){
				 Message = "No Such Items. Please search for another item";
			}else{
				 Message = "No More Items. Please search for another item";
			}
			
			JSONObject root = new JSONObject();
	        JSONObject c0 = new JSONObject();
	        JSONObject c1 = new JSONObject();

	        JSONObject attachment = new JSONObject();
	        JSONObject payload = new JSONObject();
	        JSONArray buttons = new JSONArray();
	        JSONObject button1 = new JSONObject();

	        root.put("recipient", c0);
	        root.put("message", c1);

	        c0.put("id", userId);
	        c1.put("attachment", attachment);

	        attachment.put("type", "template");
	        attachment.put("payload", payload);

	        payload.put("template_type", "button");
	        payload.put("text", Message);
	        payload.put("buttons", buttons);

	          
	        button1.put("type", "web_url");
	        button1.put("url", URL);
	        button1.put("title", "SignUp");
	        buttons.put(button1);
	        
	        botMessage= root.toString();
		}else{
				JSONObject root = new JSONObject();
		        JSONObject c0 = new JSONObject();
		        JSONObject c1 = new JSONObject();

		        JSONObject attachment = new JSONObject();
		        JSONObject payload = new JSONObject();
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
		        payload.put("text", "An Item was found with title : "+item_title);
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
		        
	        botMessage= root.toString();
		}
		
		return botMessage;
		
	}
	
	
	public String postBackMessage(String postback){
		String PostbackMessage = null,payloadMessage=null;
		
		LOGGER.info("Inside PostBAck Method");
		JSONObject row = new JSONObject(postback);
	    
	    JSONArray rows = row.getJSONArray("entry");
	    JSONObject first = rows.getJSONObject(0);
        
        JSONArray message_rows = first.getJSONArray("messaging");
        JSONObject first1 = message_rows.getJSONObject(0);
        JSONObject messageText = first1.getJSONObject("postback");
        
        JSONObject senderText = first1.getJSONObject("sender");
        
        if(messageText.getString("payload").contains("=")){
        	String[] parts = messageText.getString("payload").split("=");
            String last_text = parts[0]; 
            String last_offset = parts[1]; 
            
            int last_offset_int = Integer.parseInt(last_offset);
        
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
        }
        
        return PostbackMessage;
		
	} 
	
	public boolean fetchItem(String text,int offset){
		boolean item_status = false;
		
		LOGGER.info("Inside fetchItem Method");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1=null;
		
		String item_uid= null;
		try {
			String sqlFetchItem = "SELECT * from `items` WHERE item_status='InStore' AND item_name LIKE '%"+text+"%' ORDER BY item_id DESC LIMIT "+offset+", 1";
			ps1 = hcp.prepareStatement(sqlFetchItem);

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
