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
import util.AwsSESEmail;
import util.Event;
import util.Event.Event_Type;
import util.Event.Notification_Type;
import util.FlsLogger;
import util.LogCredit;
import util.LogItem;
import util.MatchItems;
import util.OAuth;

public class PostItemHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(PostItemHandler.class.getName());

	private static PostItemHandler instance = null;

	public static PostItemHandler getInstance() {
		if (instance == null)
			instance = new PostItemHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		
		// TODO Auto-generated method stub
		PostItemReqObj rq = (PostItemReqObj) req;
		PostItemResObj rs = new PostItemResObj();
		Connection hcp = getConnectionFromPool();

		LOGGER.info("Inside process method " + rq.getUserId() + ", " + rq.getId());
		
		// TODO: Core of the processing takes place here

		LOGGER.info("Inside process method of Post Item");
		if(rq.getCategory().contains("Category")){
			
			//if user does not select a category
			
			rs.setItemId(0);
			rs.setCode(200);
			rs.setUid("Error");
			rs.setMessage("Item Not Posted as no Valid Category was selected");
			return rs;
		}
		
		final String userId;
		String desciption = null;
		desciption = rq.getDescription();
		
		if(rq.getDescription() == null){
			desciption = "";
		}
		
		String sqlUserLatLng = "SELECT user_lat, user_lng FROM users WHERE user_id=?";
		
		userId = rq.getUserId();
		String sql = "insert into items (item_name, item_category, item_desc, item_user_id, item_lease_value, item_lease_term, item_status, item_image, item_lat, item_lng) values (?,?,?,?,?,?,?,?,?,?)";

		try {
			
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if(!oauthcheck.equals(rq.getUserId())){
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}
			
			float lat = 0, lng = 0;
			
			LOGGER.info("Creating statement for selecting users lat lng.....");
			PreparedStatement s1 = hcp.prepareStatement(sqlUserLatLng);
			
			LOGGER.info("Statement created. Executing query.....");
			s1.setString(1, rq.getUserId());
			ResultSet r1 = s1.executeQuery();
			
			if(r1.next()){
				lat = r1.getFloat("user_lat");
				lng = r1.getFloat("user_lng");
			}
			
			r1.close();
			s1.close();
			
			LOGGER.info("Creating statement.....");
			PreparedStatement stmt = hcp.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			LOGGER.info("Statement created. Executing query.....");
			stmt.setString(1, rq.getTitle());
			stmt.setString(2, rq.getCategory());
			stmt.setString(3, desciption);
			stmt.setString(4, rq.getUserId());
			stmt.setInt(5, rq.getLeaseValue());
			stmt.setString(6, rq.getLeaseTerm());
			stmt.setString(7, rq.getStatus());
			stmt.setString(8, rq.getImage());
			stmt.setFloat(9, lat);
			stmt.setFloat(10, lng);
			stmt.executeUpdate();
			
			
			// getting the last item inserted id and appending it with the title to generate a uid
			ResultSet keys = stmt.getGeneratedKeys();
			keys.next();
			int itemId = keys.getInt(1);
			
			String uid= null;
			uid = rq.getTitle()+ " " + itemId;
			uid = uid.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();
			
			// updating the item_uid value of the last item inserted
			int uidAction=0,storeAction=0;
			String sqlUpdateUID = "UPDATE items SET item_uid=? WHERE item_id=?";
			PreparedStatement s = hcp.prepareStatement(sqlUpdateUID);
			s.setString(1, uid);
			s.setInt(2, itemId);
			uidAction = s.executeUpdate();
			
			if(uidAction> 0){
				String sqlInsertStoreID = "insert into store (store_item_id) values (?)";
				PreparedStatement storeID = hcp.prepareStatement(sqlInsertStoreID);
				storeID.setInt(1, itemId);
				storeAction =storeID.executeUpdate();
				storeID.close();
				if(storeAction> 0){
					LOGGER.info("Value in store table after UID query excecution: "+uidAction+" "+itemId);	
				}
			}
			String message;
			message = "Item added into table";
			LOGGER.warning(message);
			
			LogItem li = new LogItem();
			li.addItemLog(itemId, rq.getStatus(), "", "");
			
			// to add credit in user_credit
			String sqlAddCredit = "UPDATE users SET user_credit=user_credit+10 WHERE user_id=?";
			PreparedStatement psCredit = hcp.prepareStatement(sqlAddCredit);
			psCredit.setString(1, rq.getUserId());
			psCredit.executeUpdate();
			
			LogCredit lc = new LogCredit();
			lc.addLogCredit(rq.getUserId(),10,"Item Added In Store","");

			String status_W = rq.getStatus(); // To be used to check if Request
												// is from WishItem API.
			if (!FLS_WISHLIST_ADD.equals(status_W)) {
				try {
					AwsSESEmail newE = new AwsSESEmail();
					newE.send(userId, Notification_Type.FLS_MAIL_POST_ITEM, rq);
					Event event = new Event();
					event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_POST_ITEM, rq.getId(), "Your Item <a href=\"/flsv2/ItemDetails?uid=" + uid + "\">" + rq.getTitle() + "</a> has been added to the Friend Store");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// returning the new id
			int id=0;
			sql = "SELECT MAX(item_id) FROM items";
			Statement stmt1 = hcp.createStatement();
			ResultSet resultset = stmt1.executeQuery(sql);
			
			while (resultset.next()) {
				id = resultset.getInt(1);
			}
			
			rs.setItemId(id);
			rs.setCode(0);
			rs.setUid(uid);
			rs.setMessage(message);
			stmt.close();
			stmt1.close();
			psCredit.close();
			s.close();
		} catch (SQLException e) {
			LOGGER.warning("Couldnt create a statement");
			if (e.getErrorCode() == MysqlErrorNumbers.ER_DATA_TOO_LONG
					&& e.getMessage().matches(".*\\bitem_image\\b.*")) {
				LOGGER.warning("The image size is too large. Please select image less than 16MB");
				rs.setItemId(FLS_SQL_EXCEPTION_I);
				rs.setCode(FLS_SQL_EXCEPTION_I);
				rs.setUid("Error");
				rs.setMessage(FLS_SQL_EXCEPTION_IMAGE);
				LOGGER.warning(e.getErrorCode() + " " + e.getMessage());
			} else {
				rs.setItemId(0);
				rs.setCode(FLS_SQL_EXCEPTION);
				rs.setUid("Error");
				rs.setMessage(FLS_SQL_EXCEPTION_M);
				e.printStackTrace();
			}
		} catch (NullPointerException e) {
			rs.setCode(FLS_NULL_POINT);
			rs.setMessage(FLS_NULL_POINT_M);
		} finally {
			hcp.close();
		}
		
		try{
			// checking the wish list if this posted item matches someone's requirements
			MatchItems matchItems = new MatchItems(rq);
			matchItems.checkWishlist();
		}catch(Exception e){
			LOGGER.warning(e.getMessage());
		}
		
		LOGGER.info("Finished process method ");
	
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
	}
}
