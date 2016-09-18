package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

		LOGGER.info("Inside process method of Add Fb Id Handler");
		
		SendMessageReqObj rq = (SendMessageReqObj) req;
		SendMessageResObj rs = new SendMessageResObj();
		
		try {
			
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if(!oauthcheck.equals(rq.getUserId())){
				LOGGER.warning("OAuth failed");
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}
			
			if(rq.getMessage()!=null ||!rq.getMessage().isEmpty() ||rq.getMessage()!="null"){
				
				Event event = new Event();
				event.createEvent(rq.getFriendId(), rq.getUserId(), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_MESSAGE_FRIEND_FROM, 0, "Your friend <a href=\"" + URL + "/myapp.html#/myfriendslist\">" + rq.getFriendId() + "</a> has been sent a message. The message is:- <br> <i>"+"'"+rq.getMessage()+"' </i>");
				event.createEvent(rq.getUserId(), rq.getFriendId(), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_MESSAGE_FRIEND_TO, 0, "Your friend <a href=\"" + URL + "/myapp.html#/myfriendslist\">" + rq.getUserId() + "</a> sent you a message. The message is:- <br> <i>"+"'"+rq.getMessage()+"' </i>");
				LOGGER.info("Relevant Info: "+rq.getUserId()+" "+rq.getFriendId()+" "+rq.getFriendName()+" "+rq.getMessage()+" "+rq.getItemId());
			}
			rs.setCode(FLS_SUCCESS);
			rs.setMessage(FLS_SUCCESS_M);
		
		} catch (NullPointerException e) {
			rs.setCode(FLS_NULL_POINT);
			rs.setMessage(FLS_NULL_POINT_M);
			e.printStackTrace();
		} catch(Exception e){
			LOGGER.warning("not able to get scheduler");
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}
		
		LOGGER.info("Finished process method of Send Message handler");
	
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
	}
}
