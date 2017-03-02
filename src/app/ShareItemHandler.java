package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import app.AppHandler;

import com.mysql.jdbc.MysqlErrorNumbers;

import connect.Connect;
import pojos.GetItemStoreByXResObj;
import pojos.ShareItemNumbersReqObj;
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
	private int friends_count=0;
	
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
		
		String friendList="",friendIntro="",userId=rq.getUserId();
		System.out.println(rq.getFriendNumbers());
		Event event = new Event();
		
		try {
			
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if (!oauthcheck.equals(userId)) {
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}
			
			if(rq.isGoogleStatus() && rq.getFriendNumbersLength()>0){
				
				for(int i=0;i<rq.getFriendNumbersLength();i++){
					LOGGER.info("Sending sms for name "+rq.getFriendNumbers().get(i).getName());
					LOGGER.info("Sending sms for number "+rq.getFriendNumbers().get(i).getNumber());
					event.createEvent(rq.getUserId(), rq.getFriendNumbers().get(i).getNumber(), Event_Type.FLS_EVENT_NOT_NOTIFICATION, Notification_Type.FLS_SMS_SHARE_ITEM_FRIEND, rq.getItemId(), "Your Friend "+rq.getUserName()+" has shared an item <a href=\"" + item_url + rq.getItemUid() + "\">"+rq.getItemTitle()+"</a> with you on FrrndLease");
					friendList = friendList + rq.getFriendNumbers().get(i).getName()+"<br/>";
					friends_count++;
					
				}
				
			}
			
			if(rq.isFlsStatus()){
				friendList = friendList + getFlsFriendList(rq.getItemOwnerId(),rq.getUserId(),rq.getUserName(),rq.getItemId(),rq.getItemTitle(),rq.getItemUid(),rq.isFriendsStatus());
			}
			
				if(friendList.equals(null) || friendList.equals("")){
					
					if(rq.isGoogleStatus()){
						rs.setMessage(FLS_SHARE_ITEM_GOOGLE_EXCEPTION_M);
					}else{
						rs.setMessage(FLS_SHARE_ITEM_EXCEPTION_M);
					}
					rs.setCode(FLS_ENTRY_NOT_FOUND);
					
				}else{
					friendIntro = "You have shared the item <a href='" + item_url + rq.getItemUid() + "'>"+rq.getItemTitle()+"</a> with "+friends_count+" friends. Their names are:<br/><div align='"+"left"+"' style='"+"padding-left: 160px;"+"'>"+friendList+"</div>";
					event.createEvent("admin@frrndlease.com", userId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_SHARE_ITEM_OWNER, rq.getItemId(), friendIntro);
					rs.setCode(FLS_SUCCESS);
					rs.setMessage(FLS_SHARE_ITEM);
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
		}
		
		LOGGER.info("Finished process method of Share Item handler");
	
		// return the response
		return rs;
	}
	
	private String getFlsFriendList(String itemOwnerId,String userId, String userName, int itemId, String itemTitle, String itemUid, boolean flsfriendsStatus){
		String flssList="",signedUp="signedup";
		
		String URL = FlsConfig.prefixUrl;
		String item_url = URL + "/ItemDetails?uid=";
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null,ps2=null;
		ResultSet rs1 = null;
		
		Event event = new Event();
		try {
			
			String sql = "SELECT friend_id,friend_full_name,friend_status from friends WHERE friend_user_id='"+userId+ "' AND friend_id NOT LIKE '%@fb%' AND ";
			
			if(flsfriendsStatus){
				sql = sql + "friend_status LIKE '%' ";
			}else{
				sql = sql + "friend_status ='"+signedUp+"'";
			}
			
			LOGGER.info("Select statement for fetching all friends' email ids .....");
			LOGGER.info(sql);
			ps1 = hcp.prepareStatement(sql);
			rs1 = ps1.executeQuery();
			
			if (rs1.next()){
				rs1.beforeFirst();
				while(rs1.next()){
					if(!rs1.getString("friend_id").equals(itemOwnerId)){
						if(rs1.getString("friend_status").equals("signedup")){
							flssList = flssList + rs1.getString("friend_full_name")+"<br/>";
						}else{
							flssList = flssList + rs1.getString("friend_id")+"<br/>";
						}
						event.createEvent(userId, rs1.getString("friend_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_SHARE_ITEM_FRIEND, itemId, "Your Friend "+userName+" has shared an item <a href='" + item_url + itemUid + "'>"+itemTitle+"</a> with you on FrrndLease");
						friends_count++;
					}
				}
			}else{
				LOGGER.info("Result Set for Fls friends is null");
			}
		} catch(Exception e){
			LOGGER.warning("Exception inside getFlsFriendList method of Share Item App Handler");
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
		
		return flssList;
	}
	
	@Override
	public void cleanup() {
	}
}