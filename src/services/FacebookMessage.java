package services;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import pojos.AddLeadReqObj;
import util.FlsBot;
import util.FlsCredit;

@WebServlet(description = "Get Message from Facebook Page", urlPatterns = { "/FacebookMessage" })
public class FacebookMessage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String access_token = "EAABiKoMD4QQBAP4CshmMh49Gh18uVvUcU4qURZBNZBwAhNGSXPE3DMtGkvCnFgKJ8pDbHlJCnCZBn3MOmxWx8ZAMx81vbs0IQFlJPmz115oZCtI0VQHEmKEQd6f8yqyWd8ZBdo3nZA0TUT4Hg1SMgBdCCdvZBVh8S0ebbjrT4tQhFAZDZD";
	private static final  String verify_token = "fb_bot";
	String hub_verify_token = null;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		Enumeration headerNames = req.getHeaderNames();
		while(headerNames.hasMoreElements()) {
		  String headerName = (String)headerNames.nextElement();
		  System.out.println("Header Name - " + headerName + ", Value - " + req.getHeader(headerName));
		}
		
		Enumeration params = req.getParameterNames(); 
		while(params.hasMoreElements()){
		 String paramName = (String)params.nextElement();
		 System.out.println("Parameter Name - "+paramName+", Value - "+req.getParameter(paramName));
		}
		
		try{

			String challenge = req.getParameter("hub.challenge");
			hub_verify_token = req.getParameter("hub.verify_token");
			
			if (hub_verify_token.equals(verify_token)) {
				System.out.println(challenge);
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
			e.printStackTrace();
			System.out.println("Inside Exception of Facebook Message");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		FlsBot bot = new FlsBot();
		//String access_token = "EAABiKoMD4QQBAP4CshmMh49Gh18uVvUcU4qURZBNZBwAhNGSXPE3DMtGkvCnFgKJ8pDbHlJCnCZBn3MOmxWx8ZAMx81vbs0IQFlJPmz115oZCtI0VQHEmKEQd6f8yqyWd8ZBdo3nZA0TUT4Hg1SMgBdCCdvZBVh8S0ebbjrT4tQhFAZDZD";
		try{			  
			
			// Read from request
		    StringBuilder buffer = new StringBuilder();
		    BufferedReader reader = request.getReader();
		    String line;
		    while ((line = reader.readLine()) != null) {
		        buffer.append(line);
		    }
		    
		    String data = buffer.toString();
		    JSONObject row = new JSONObject(data);
		    System.out.println("JSON Object "+row);
		    
		    JSONArray rows = row.getJSONArray("entry");
		   // Data = row.getJSONArray("row");
		    //JSONArray array = new JSONArray(data);
		   // String first = rows.getString(0);
		    JSONObject first = rows.getJSONObject(0);
	        System.out.println("entry array is"+rows);
	        System.out.println("first array is "+first);
	        
	        JSONArray message_rows = first.getJSONArray("messaging");
	        JSONObject first1 = message_rows.getJSONObject(0);
	        System.out.println("message array is"+rows);
	        System.out.println("first1 array is "+first1);
	        JSONObject messageText = first1.getJSONObject("message");
	        System.out.println("final message array is"+messageText);
	        
	        //JSONObject messageText2 = messageText.getJSONObject("text");
	        System.out.println("final message array is"+messageText.getString("text"));
	        
	        JSONObject senderText = first1.getJSONObject("sender");
	        System.out.println("final sender id is"+senderText.getString("id"));
	        
	        String botMessage = bot.sendBotMessage(senderText.getString("id"), messageText.getString("text"));
	         
	        JSONObject root = new JSONObject();
	        JSONObject c0 = new JSONObject();
	        JSONObject c1 = new JSONObject();

	        root.put("recipient", c0);
	        root.put("message", c1);

	        c0.put("id", senderText.getString("id"));
	        c1.put("text", botMessage);
	        System.out.println(root.toString());
			
			HttpURLConnection httpcon = (HttpURLConnection) ((new URL("https://graph.facebook.com/v2.6/me/messages?access_token="+access_token).openConnection()));
			httpcon.setDoOutput(true);
			httpcon.setRequestProperty("Content-Type", "application/json");
			httpcon.setRequestMethod("POST");
			httpcon.connect();

			final OutputStreamWriter osw = new OutputStreamWriter(httpcon.getOutputStream());
			osw.write(root.toString());
			osw.close();
			
			int responseCode = httpcon.getResponseCode();
			System.out.println("Response Code for JSON POST : " + responseCode);

		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Inside Exception of Facebook Message");
		}
		
	}

}
