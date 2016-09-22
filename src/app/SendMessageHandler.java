package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import com.mysql.jdbc.MysqlErrorNumbers;

import connect.Connect;
import pojos.SendMessageReqObj;
import pojos.SendMessageResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.Event;
import util.Event.Event_Type;
import util.Event.Notification_Type;
import util.FlsConfig;
import util.FlsLogger;
import util.LogCredit;
import util.LogItem;
import util.OAuth;

public class SendMessageHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(SendMessageHandler.class.getName());

	private String URL = FlsConfig.prefixUrl;
	
	private static SendMessageHandler instance = null;

	public static SendMessageHandler getInstance() {
		if (instance == null)
			instance = new SendMessageHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside process method of Send Message Handler");
		
		SendMessageReqObj rq = (SendMessageReqObj) req;
		SendMessageResObj rs = new SendMessageResObj();
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		String userName ="",friendName="";
		List<String> results = new ArrayList<String>();
		try {
			
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if(!oauthcheck.equals(rq.getUserId())){
				LOGGER.warning("OAuth failed");
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}
			
			if(rq.getMessage()!=null ||!rq.getMessage().isEmpty() ||!rq.getMessage().equals("null")){
				
				LOGGER.info("Creating statement for selecting Names of From & To User .....");
				String sqlUserName = "SELECT user_full_name FROM `users` WHERE user_id IN (?,?)";
				ps1 = hcp.prepareStatement(sqlUserName);
				ps1.setString(1, rq.getUserId());
				ps1.setString(2, rq.getFriendId());
				rs1 = ps1.executeQuery();
				
				while(rs1.next()){
					results.add(rs1.getString(1));
				}
				
				userName = results.get(0);
				friendName = results.get(1);
				String message = rq.getMessage();
				
				Event event = new Event();
				if(rq.getUid()!=null){
					LOGGER.info("Item Details are: "+rq.getItemId()+" "+rq.getTitle()+" "+rq.getUid());
					event.createEvent(rq.getFriendId(), rq.getUserId(), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_MESSAGE_ITEM_FROM, rq.getItemId(), "You have sent a message to user <i>"+friendName+"</i> regarding an item <a href=\"" + URL + "/ItemDetails?uid=" + rq.getUid() + "\">" + rq.getTitle() + "</a>. The message is:- <br> <i>"+"'"+message+"' </i>");
					event.createEvent(rq.getUserId(), rq.getFriendId(), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_MESSAGE_ITEM_TO, rq.getItemId(), " You have recieved a message from user <i>"+userName+"</i> regarding an item <a href=\"" + URL + "/ItemDetails?uid=" + rq.getUid() + "\">" + rq.getTitle() + "</a>. The message is:- <br> <i>"+"'"+message+"' </i>");
					
				}else{
					LOGGER.info("Friend Message Info: "+rq.getUserId()+" "+rq.getFriendId()+" "+rq.getFriendName()+" "+rq.getMessage()+" "+rq.getItemId()+" "+rq.getTitle()+" "+rq.getUid());
					event.createEvent(rq.getFriendId(), rq.getUserId(), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_MESSAGE_FRIEND_FROM, 0, "Your friend <a href=\"" + URL + "/myapp.html#/myfriendslist\">" + friendName + "</a> has been sent a message. The message is:- <br> <i>"+"'"+message+"' </i>");
					event.createEvent(rq.getUserId(), rq.getFriendId(), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_MESSAGE_FRIEND_TO, 0, "Your friend <a href=\"" + URL + "/myapp.html#/myfriendslist\">" + userName + "</a> sent you a message. The message is:- <br> <i>"+"'"+message+"' </i>");
				}
				
			}
			rs.setCode(FLS_SUCCESS);
			rs.setMessage(FLS_SUCCESS_M);
		
		} catch (NullPointerException e) {
			LOGGER.warning("Null Pointer Exception in Send Message App Handler");
			rs.setCode(FLS_NULL_POINT);
			rs.setMessage(FLS_NULL_POINT_M);
			e.printStackTrace();
		} catch(Exception e){
			LOGGER.warning("not able to get scheduler inside Send Message App Handler");
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}finally {
			try {
				if(rs1 != null)rs1.close();
				if(ps1 != null)ps1.close();
				if(hcp != null)hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		LOGGER.info("Finished process method of Send Message handler");
	
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
	}
}