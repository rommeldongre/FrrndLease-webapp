package services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import util.FlsBot;
import util.FlsLogger;

@WebServlet(description = "Get Message from Facebook Page", urlPatterns = { "/FacebookMessage" })
public class FacebookMessage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private FlsLogger LOGGER = new FlsLogger(FacebookMessage.class.getName());
	
	private static final String access_token = "EAABiKoMD4QQBAEShbRZBBRZAg3bgWWR14SRsqZABqZBDhVpEh4Kgz2pHnLaS29iNuqKQMhLUhWbIibmKnzWsiVaamD5X1FluMvvNZC8eVJLZB4cgmq42abAPrZCl3mBuxd19LZArvPgyGdMGAzhkilRxeDmbZCT5R8u3x6HZCa20zTigZDZD";
	private static final  String verify_token = "fb_bot";
	String hub_verify_token = null;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			
		LOGGER.info("Inside GET Method");
		try{

			String challenge = req.getParameter("hub.challenge");
			hub_verify_token = req.getParameter("hub.verify_token");
			
			if (hub_verify_token.equals(verify_token)) {
				LOGGER.info(challenge);
			}else{
				LOGGER.info("Tokens not same");
			}
			
			resp.setHeader("Expires", "0");
			resp.setHeader("Cache-Control","must-revalidate, post-check=0, pre-check=0");
			resp.setHeader("Pragma", "public");
			
			//set the response content type to JSON
			resp.setContentType("application/json");
			
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().write(challenge);
			resp.getWriter().flush();
			resp.getWriter().close();
		}catch(Exception e){
			LOGGER.info("Exception occured inside GET method of Facebook Message");
			e.printStackTrace();
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		LOGGER.info("Inside POST Method");
		FlsBot bot = new FlsBot();
		String botMessage=null;
		try{			  
			
			// Read from request
		    StringBuilder buffer = new StringBuilder();
		    BufferedReader reader = request.getReader();
		    String line;
		    while ((line = reader.readLine()) != null) {
		        buffer.append(line);
		    }
		    
		    String data = buffer.toString();
		    if(data.contains("postback")){
		    	botMessage = bot.postBackMessage(data);
		    	
		    }else{
		    	JSONObject row = new JSONObject(data);
			    
			    JSONArray rows = row.getJSONArray("entry");
			    JSONObject first = rows.getJSONObject(0);
		        
		        JSONArray message_rows = first.getJSONArray("messaging");
		        JSONObject first1 = message_rows.getJSONObject(0);
		        JSONObject messageText = first1.getJSONObject("message");
		        
		        JSONObject senderText = first1.getJSONObject("sender");
		        
		        botMessage = bot.sendBotMessage(senderText.getString("id"), messageText.getString("text"),0);
		         
		    }
		    
			if(botMessage!=null){
				HttpURLConnection httpcon = (HttpURLConnection) ((new URL("https://graph.facebook.com/v2.6/me/messages?access_token="+access_token).openConnection()));
				httpcon.setDoOutput(true);
				httpcon.setRequestProperty("Content-Type", "application/json");
				httpcon.setRequestMethod("POST");
				httpcon.connect();

				final OutputStreamWriter osw = new OutputStreamWriter(httpcon.getOutputStream());
				osw.write(botMessage);
				osw.close();
				
				int responseCode = httpcon.getResponseCode();
				String responseMessage = httpcon.getResponseMessage();
				
				LOGGER.info("Response Code for JSON POST : " + responseCode);
				LOGGER.info("Response Message for JSON POST : " + responseMessage);
					
			}
			

		}catch(Exception e){
			LOGGER.info("Exception occured inside POST method of Facebook Message");
			e.printStackTrace();
			
		}
		
	}

}
