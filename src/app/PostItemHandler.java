package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.MysqlErrorNumbers;

import connect.Connect;
import pojos.PostItemReqObj;
import pojos.PostItemResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.Event;
import util.Event.Event_Type;
import util.Event.Notification_Type;
import util.FlsConfig;
import util.FlsLogger;
import util.FlsS3Bucket;
import util.FlsS3Bucket.Bucket_Name;
import util.FlsS3Bucket.File_Name;
import util.FlsS3Bucket.Path_Name;
import util.LogCredit;
import util.LogItem;
import util.OAuth;

public class PostItemHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(PostItemHandler.class.getName());

	private String URL = FlsConfig.prefixUrl;
	
	private static PostItemHandler instance = null;

	public static PostItemHandler getInstance() {
		if (instance == null)
			instance = new PostItemHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside process method of Post Item Handler");
		
		PostItemReqObj rq = (PostItemReqObj) req;
		PostItemResObj rs = new PostItemResObj();
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null, ps3 = null, ps5 = null;
		ResultSet keys = null, rs1 = null;
		int rs2, rs3,rs5;
		
		String userId = rq.getUserId();
		int itemId = 0;
		String uid = null,link;
		
		float lat = 0, lng = 0;
		
		String desciption = rq.getDescription();
		if(desciption == null){
			desciption = "";
		}
		
		try {
			
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if(!oauthcheck.equals(rq.getUserId())){
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}
			
			LOGGER.info("Creating statement for selecting users lat lng.....");
			String sqlUserLatLng = "SELECT user_lat, user_lng FROM users WHERE user_id=?";
			ps1 = hcp.prepareStatement(sqlUserLatLng);
			ps1.setString(1, rq.getUserId());
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				lat = rs1.getFloat("user_lat");
				lng = rs1.getFloat("user_lng");
			}
			

			hcp.setAutoCommit(false);
			
			LOGGER.info("Creating statement to insert item......");
			String sqlInsertItem = "insert into items (item_name, item_category, item_desc, item_user_id, item_lease_value, item_lease_term, item_status, item_lat, item_lng) values (?,?,?,?,?,?,?,?,?)";
			ps2 = hcp.prepareStatement(sqlInsertItem, Statement.RETURN_GENERATED_KEYS);
			ps2.setString(1, rq.getTitle());
			ps2.setString(2, rq.getCategory());
			ps2.setString(3, desciption);
			ps2.setString(4, rq.getUserId());
			ps2.setInt(5, rq.getLeaseValue());
			ps2.setString(6, rq.getLeaseTerm());
			ps2.setString(7, rq.getStatus());
			ps2.setFloat(8, lat);
			ps2.setFloat(9, lng);
			rs2 = ps2.executeUpdate();
			LOGGER.info("Result of insertion query : " + rs2);
			
			// getting the last item inserted id and appending it with the title to generate a uid
			keys = ps2.getGeneratedKeys();
			keys.next();
			itemId = keys.getInt(1);
			LOGGER.info("Item created with item id : " + itemId);
			
			String uidTitle = rq.getTitle();
			uidTitle = uidTitle.substring(0, Math.min(uidTitle.length(), 10));
			
			uid = uidTitle+ " " + itemId;
			uid = uid.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();
			
			// updating the item_uid value of the last item inserted
			String sqlUpdateUID = "UPDATE items SET item_uid=? WHERE item_id=?";
			ps3 = hcp.prepareStatement(sqlUpdateUID);
			ps3.setString(1, uid);
			ps3.setInt(2, itemId);
			rs3 = ps3.executeUpdate();
			LOGGER.info("UID created for the item : " + uid);
			
			LOGGER.warning("Item added into table");
			
			// Adding entry to Item log
			LogCredit lc = new LogCredit();
			lc.addLogCredit(rq.getUserId(),10,"Item Added In Friend Store","");
			
			// adding credit to users account
			String sqlAddCredit = "UPDATE users SET user_credit=user_credit+10 WHERE user_id=?";
			ps5 = hcp.prepareStatement(sqlAddCredit);
			ps5.setString(1, rq.getUserId()+"");
			rs5 = ps5.executeUpdate();
			
			if(rs5 == 1){
				LOGGER.info("10 credits added to the users table");
				hcp.commit();
				FlsS3Bucket s3Bucket = new FlsS3Bucket(uid);
				link = s3Bucket.uploadImage(Bucket_Name.ITEMS_BUCKET, Path_Name.ITEM_POST, File_Name.ITEM_PRIMARY, rq.getImage(), null);
				if(link != null){
					s3Bucket.savePrimaryImageLink(link);
				}
				try {
					Event event = new Event();
					event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_POST_ITEM, itemId, "Your Item <a href=\"" + URL + "/ItemDetails?uid=" + uid + "\">" + rq.getTitle() + "</a> has been added to the Friend Store");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// Adding entry to item log
				LogItem li = new LogItem();
				li.addItemLog(itemId, rq.getStatus(), "", link);
			}else{
				LOGGER.info("Credits not added to the users table");
				hcp.rollback();
			}

			rs.setUid(uid);
			rs.setCode(FLS_SUCCESS);
			rs.setMessage(FLS_SUCCESS_M);
			
		} catch (SQLException e) {
			LOGGER.warning("Couldnt create a statement");
			if (e.getErrorCode() == MysqlErrorNumbers.ER_DATA_TOO_LONG && e.getMessage().matches(".*\\bitem_image\\b.*")) {
				LOGGER.warning("The image size is too large. Please select image less than 16MB");
				rs.setCode(FLS_SQL_EXCEPTION_I);
				rs.setMessage(FLS_SQL_EXCEPTION_IMAGE);
				rs.setUid("Error");
			} else {
				rs.setCode(FLS_SQL_EXCEPTION);
				rs.setUid("Error");
				rs.setMessage(FLS_SQL_EXCEPTION_M);
				e.printStackTrace();
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			rs.setCode(FLS_NULL_POINT);
			rs.setMessage(FLS_NULL_POINT_M);
		} catch (Exception e){
			e.printStackTrace();
			rs.setCode(FLS_INVALID_OPERATION);
			rs.setMessage(FLS_INVALID_OPERATION_M);
		}finally {
			try {
				if(ps5 != null)ps5.close();
				if(ps3 != null)ps3.close();
				if(keys != null)keys.close();
				if(ps2 != null)ps2.close();
				if(rs1 != null)rs1.close();
				if(ps1 != null)ps1.close();
				if(hcp != null)hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		LOGGER.info("Finished process method of post item handler");
	
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
	}
}
