package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import app.AppHandler;

import com.mysql.jdbc.MysqlErrorNumbers;

import connect.Connect;
import pojos.ShareItemReqObj;
import pojos.ShareItemResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.Event;
import util.FlsConfig;
import util.FlsEmail;
import util.FlsLogger;
import util.OAuth;
import util.Event.Event_Type;
import util.Event.Notification_Type;

public class ShareItemHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(ShareItemHandler.class.getName());
	
	private static ShareItemHandler instance = null;

	public static ShareItemHandler getInstance() {
		if (instance == null)
			instance = new ShareItemHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside process method of Share Item Handler");
		
		ShareItemReqObj rq = (ShareItemReqObj) req;
		ShareItemResObj rs = new ShareItemResObj();
		
		String URL = FlsConfig.prefixUrl;
		String item_url = URL + "/ItemDetails?uid=";
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null,ps2=null;
		ResultSet rs1 = null;
		int friends_count=0;
		String friendList="",friendIntro="",signedUp="signedup",
				userId=rq.getUserId();
		boolean friensStatus=rq.isFriendsStatus();
		try {
			
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if (!oauthcheck.equals(userId)) {
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}
			
			String sql = "SELECT friend_id,friend_full_name,friend_status from friends WHERE friend_user_id='"+userId+ "' AND friend_id NOT LIKE '%@fb%' AND ";
			
				if(friensStatus){
					sql = sql + "friend_status LIKE '%' ";
				}else{
					sql = sql + "friend_status ='"+signedUp+"'";
				}
				
				LOGGER.info("Select statement for fetching all friends' email ids .....");
				LOGGER.info(sql);
				ps1 = hcp.prepareStatement(sql);
				rs1 = ps1.executeQuery();
				
				if (rs1.next()){
					Event event = new Event();
					rs1.beforeFirst();
					while(rs1.next()){
						if(!rs1.getString("friend_id").equals(rq.getItemOwnerId())){
							if(rs1.getString("friend_status").equals("signedup")){
								friendList = friendList + rs1.getString("friend_full_name")+"<br/>";
								event.createEvent(rq.getUserId(), rs1.getString("friend_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_SHARE_ITEM_FRIEND, rq.getItemId(), "Your Friend "+rq.getUserName()+" has shared an item <a href='" + item_url + rq.getItemUid() + "'>"+rq.getItemTitle()+"</a> with you on FrrndLease");
							}else{
								friendList = friendList + rs1.getString("friend_id")+"<br/>";
								event.createEvent(rq.getUserId(), rs1.getString("friend_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_SHARE_ITEM_FRIEND, rq.getItemId(), "Your Friend "+rq.getUserName()+" has shared an item <a href='" + item_url + rq.getItemUid() + "'>"+rq.getItemTitle()+"</a> with you on FrrndLease");
							}
							friends_count++;
						}
					}
					
					friendIntro = "You have shared the item <a href='" + item_url + rq.getItemUid() + "'>"+rq.getItemTitle()+"</a> with "+friends_count+" friends. Their names are:<br/><div align='"+"left"+"' style='"+"padding-left: 160px;"+"'>"+friendList+"</div>";
					event.createEvent("admin@frrndlease.com", userId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_SHARE_ITEM_OWNER, rq.getItemId(), friendIntro);
					rs.setCode(FLS_SUCCESS);
					rs.setMessage(FLS_SHARE_ITEM);
				}else{
					LOGGER.info("Result Set is null");
					rs.setCode(FLS_ENTRY_NOT_FOUND);
					rs.setMessage(FLS_SHARE_ITEM_EXCEPTION_M);
				}
				
		} catch (NullPointerException e) {
			LOGGER.warning("Null Pointer Exception in Share Item App Handler");
			rs.setCode(FLS_NULL_POINT);
			rs.setMessage(FLS_NULL_POINT_M);
			e.printStackTrace();
		} catch(Exception e){
			LOGGER.warning("Exception inside Share Item App Handler");
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}finally {
			try {
				if(rs1 != null)rs1.close();
				if(ps1 != null)ps1.close();
				if(ps2 != null)ps2.close();
				if(hcp != null)hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		LOGGER.info("Finished process method of Share Item handler");
	
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
	}
}