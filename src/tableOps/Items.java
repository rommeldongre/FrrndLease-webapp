package tableOps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.MysqlErrorNumbers;

import org.json.JSONException;
import org.json.JSONObject;

import connect.Connect;
import adminOps.Response;
import pojos.ItemsModel;
import util.LogItem;
import util.Event;
import util.FlsBadges;
import util.Event.Event_Type;
import util.Event.Notification_Type;
import util.FlsCredit.Credit;
import util.FlsS3Bucket.Bucket_Name;
import util.FlsS3Bucket.File_Name;
import util.FlsS3Bucket.Path_Name;
import util.FlsConfig;
import util.FlsCredit;
import util.FlsLogger;
import util.FlsS3Bucket;

public class Items extends Connect {

	private FlsLogger LOGGER = new FlsLogger(Items.class.getName());

	private String URL = FlsConfig.prefixUrl;
	
	private String operation, category, leaseTerm, userId, status, title, description, message, Id = null, image;
	private int leaseValue, id, token, Code, surcharge;
	private ItemsModel im;
	private Response res = new Response();
	private int check = 0;

	public Response selectOp(String Operation, ItemsModel itm, JSONObject obj) {
		operation = Operation.toLowerCase();
		im = itm;

		switch (operation) {

		case "add":
			LOGGER.info("Add op is selected..");
			Add();
			// status = "Performing addition operation..";
			break;

		case "delete":
			LOGGER.info("Delete operation is selected");
			Delete();
			break;

		case "edit":
			LOGGER.info("Edit operation is selected.");
			Edit();
			break;

		case "editstat":
			LOGGER.info("Edit status is selected");
			EditStat();
			break;

		case "getnext":
			LOGGER.info("Get Next operation is selected.");
			try {
				token = obj.getInt("token");
				GetNext();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}

			break;

		case "getprevious":
			LOGGER.info("Get Previous Operation is selected.");
			try {
				token = obj.getInt("token");
				GetPrevious();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;

		case "browsen":
			LOGGER.info("Browse Next Operation is selected.");
			try {
				token = obj.getInt("token");
				BrowseN();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;

		case "browsep":
			LOGGER.info("Browse Previous Operation is selected.");
			try {
				token = obj.getInt("token");
				BrowseP();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;

		case "deletepost":
			LOGGER.info("Delete Posting operation is selected");
			DeletePosting();
			break;

		case "deletewish":
			LOGGER.info("Delete Wishlist operation is selected");
			DeleteWishlist();
			break;

		case "searchitem":
			LOGGER.info("Search Item operation is selected");
			try {
				token = obj.getInt("token");
				SearchItem();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;
			
		case "getdetails":
			LOGGER.info("getdetails operation is selected");
			getDetails();
			break;
			
		case "deleteadmin":
			LOGGER.info("Admin Delete operation is selected");
			DeleteAdmin();
			break;

		default:
			res.setData(FLS_INVALID_OPERATION, "0", FLS_INVALID_OPERATION_M);
			break;
		}
		return res;
	}

	private void Add() {
		title = im.getTitle();
		description = im.getDescription();
		category = im.getCategory();
		userId = im.getUserId();
		leaseTerm = im.getLeaseTerm();
		leaseValue = im.getLeaseValue();
		status = im.getStatus();
		image = im.getImage();

		LOGGER.info("Inside add method...");
		
		PreparedStatement stmt = null,s = null, s1 =null, checkWishItem_ps = null, ps1 = null; 
		Statement stmt1 = null;
		ResultSet keys = null, rs = null, checkWishItem_rs = null;
		Connection hcp = getConnectionFromPool();
		
		try {
			String checkWishItem_sql = "SELECT * FROM items WHERE item_name=? AND item_user_id=? AND item_status=?";
			checkWishItem_ps = hcp.prepareStatement(checkWishItem_sql);
			checkWishItem_ps.setString(1, title);
			checkWishItem_ps.setString(2, userId);
			checkWishItem_ps.setString(3, status);
			
			checkWishItem_rs = checkWishItem_ps.executeQuery();
			if(!checkWishItem_rs.next()){
				
				LOGGER.info("Creating statement.....");
				String sql = "insert into items (item_name, item_category, item_desc, item_user_id, item_lease_value, item_lease_term, item_status) values (?,?,?,?,?,?,?)";
				stmt = hcp.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	
				LOGGER.info("Statement created. Executing query.....");
				stmt.setString(1, title);
				stmt.setString(2, category);
				stmt.setString(3, description);
				stmt.setString(4, userId);
				stmt.setInt(5, leaseValue);
				stmt.setString(6, leaseTerm);
				stmt.setString(7, status);
				stmt.executeUpdate();
				
				// getting the last item inserted id and appending it with the title to generate a uid
				keys = stmt.getGeneratedKeys();
				keys.next();
				int itemId = keys.getInt(1);
				
				String uidTitle = title;
				uidTitle = uidTitle.substring(0, Math.min(uidTitle.length(), 10));
				
				String uid = uidTitle + " " + itemId;
				uid = uid.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();
				
				// updating the item_uid value of the last item inserted
				String sqlUpdateUID = "UPDATE items SET item_uid=? WHERE item_id=?";
				s = hcp.prepareStatement(sqlUpdateUID);
				s.setString(1, uid);
				s.setInt(2, itemId);
				s.executeUpdate();
	
				status = "operation successfull!!!";
				message = "Item added into table";
				LOGGER.warning(message);
				
				// to add credit in user_credit
				String sqlAddCredit = "UPDATE users SET user_credit=user_credit+1 WHERE user_id=?";
				s1 = hcp.prepareStatement(sqlAddCredit);
				s1.setString(1, userId);
				s1.executeUpdate();
	
				Code = 000;
	
				if(!(image == null || image.equals(""))){
					FlsS3Bucket s3Bucket = new FlsS3Bucket(uid);
					String link = null;
					if(image.startsWith("http"))
						link = s3Bucket.copyImage(Bucket_Name.ITEMS_BUCKET, Path_Name.ITEM_POST, File_Name.ITEM_PRIMARY, image);
					else
						link = s3Bucket.uploadImage(Bucket_Name.ITEMS_BUCKET, Path_Name.ITEM_POST, File_Name.ITEM_PRIMARY, image, null);
					if(link != null){
						s3Bucket.savePrimaryImageLink(link);
					}
				}
				
				String status_W = im.getStatus(); // To be used to check if Request
													// is from WishItem API.
				if (!FLS_WISHLIST_ADD.equals(status_W)) {
					try {
						Event event = new Event();
						event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_POST_ITEM, itemId, "Your Item <a href=\"" + URL + "/ItemDetails?uid=" + uid + "\">" + title + "</a> has been added to the Friend Store");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				if(!userId.equals("anonymous") && FLS_WISHLIST_ADD.equals(status_W)){
					LOGGER.info("Wish Item for Logged in User");
					try {
						Event event = new Event();
						event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_NOMAIL_ADD_WISH_ITEM, itemId, "The Item <a href=\"" + URL + "/myapp.html#/mywishlists"+ "\">" + title + "</a> has been added to your Wish List");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
	
				// returning the new id
				sql = "SELECT MAX(item_id) FROM items";
				stmt1 = hcp.createStatement();
				rs = stmt1.executeQuery(sql);
				while (rs.next()) {
					id = rs.getInt(1);
				}
				Id = String.valueOf(id);
				res.setData(FLS_SUCCESS, Id, FLS_ITEMS_ADD);
				
				// Updating data for badges
				FlsBadges badges = new FlsBadges(userId);
				badges.updateItemsCount();
			}else{
				res.setData(FLS_DUPLICATE_ENTRY, null, FLS_POST_ITEM_F_M);
				String sqlUpdateItemsTable = "UPDATE items SET item_lastmodified=now() WHERE item_id=?";
				ps1 = hcp.prepareStatement(sqlUpdateItemsTable);
				ps1.setString(1, checkWishItem_rs.getString("item_id"));
				
				ps1.executeUpdate();
			}

		} catch (SQLException e) {
			LOGGER.warning("Couldnt create a statement");
			if (e.getErrorCode() == MysqlErrorNumbers.ER_DATA_TOO_LONG
					&& e.getMessage().matches(".*\\bitem_image\\b.*")) {
				LOGGER.warning("The image size is too large. Please select image less than 16MB");
				res.setData(FLS_SQL_EXCEPTION_I, String.valueOf(FLS_SQL_EXCEPTION_I), FLS_SQL_EXCEPTION_IMAGE);
				LOGGER.warning(e.getErrorCode() + " " + e.getMessage());
			} else {
				res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
				e.printStackTrace();
			}
		}finally{
				try {
					if(checkWishItem_rs != null)checkWishItem_rs.close();
					if(checkWishItem_ps != null)checkWishItem_ps.close();
					if(keys != null)keys.close();
					if(rs != null)rs.close();
					
					if(stmt != null)stmt.close();
					if(stmt1 != null)stmt1.close();
					if(s != null)s.close();
					if(s1 != null)s1.close();
					
					if(hcp != null)hcp.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		}
	}

	private void Delete() {
		
		id = im.getId();
		
		LOGGER.info("Inside delete method....");
		
		PreparedStatement stmt1 = null, stmt2 = null, stmt3 = null, stmt4 = null;
		ResultSet rs1 = null, rs2 = null, rs3 = null;
		int rs4;
		Connection hcp = getConnectionFromPool();
		
		try {
			LOGGER.info("Checking if leases and requests exist...");

			String sqlCheckLease = "SELECT * FROM leases WHERE lease_item_id=? AND lease_status=?";
			stmt1 = hcp.prepareStatement(sqlCheckLease);
			stmt1.setInt(1, id);
			stmt1.setString(2, "Active");
			rs1 = stmt1.executeQuery();
			if(rs1.next()) {
				res.setData(FLS_DUPLICATE_ENTRY, "0", "Cannot delete. This item is in leases table!!");
				return;
			}
			
			String sqlCheckRequests = "SELECT * FROM requests WHERE request_item_id=? AND request_status=?";
			stmt2 = hcp.prepareStatement(sqlCheckRequests);
			stmt2.setInt(1, id);
			stmt2.setString(2, "Active");
			rs2 = stmt2.executeQuery();
			if(rs2.next()) {
				res.setData(FLS_DUPLICATE_ENTRY, "0", "Cannot delete. This item is in requests table!!");
				return;
			}
			
			String sqlCheckItems = "SELECT * FROM items WHERE item_id=?";
			stmt3 = hcp.prepareStatement(sqlCheckItems);
			stmt3.setInt(1, id);
			rs3 = stmt3.executeQuery();			

			if (rs3.next()) {
				
				if(!rs3.getString("item_status").equals("Archived")){
					LOGGER.info("Delete Unsuccessful as Item Not in Archived State.");
					res.setData(FLS_ITEMS_DELETE_ARCHIVED, "0", FLS_ITEMS_DELETE_ARCHIVED_M);
					return;
				}

				if(rs3.getString("item_uid") != null && rs3.getString("item_primary_image_link") != null){
					FlsS3Bucket s3Bucket = new FlsS3Bucket(rs3.getString("item_uid"));
					int d = s3Bucket.deleteImage(Bucket_Name.ITEMS_BUCKET, rs3.getString("item_primary_image_link"));
					if(d == 1){
						LOGGER.info("item image deleted from s3");
						s3Bucket.deleteImages();
					}else{
						res.setData(FLS_INVALID_OPERATION, "0", "Not Able to delete images");
						return;
					}
					
				}
				
				LOGGER.info("Deleting item from items table");
				String sqlDeleteItem = "DELETE FROM items WHERE item_id=?";

				stmt4 = hcp.prepareStatement(sqlDeleteItem);
				stmt4.setInt(1, id);
				rs4 = stmt4.executeUpdate();

				if(rs4 == 1){
					res.setData(FLS_SUCCESS, "0", FLS_ITEMS_DELETE);
					
					// Updating Credits
					FlsCredit credits = new FlsCredit();
					credits.logCredit(rs3.getString("item_user_id"), 10, "Item Deleted Permanently", "", Credit.SUB);
					
				}else{
					res.setData(FLS_INVALID_OPERATION, "0", "Not able to delete from items table");
				}
				
				// Updating data for badges
				FlsBadges badges = new FlsBadges(rs3.getString("item_user_id"));
				badges.updateItemsCount();
				
			} else {
				LOGGER.warning("Entry not found in database!!");
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
		}finally{
			try {
				if(rs3 != null) rs3.close();
				if(rs2 != null) rs2.close();
				if(rs1 != null) rs1.close();
				if(stmt4 != null) stmt4.close();
				if(stmt3 != null) stmt3.close();
				if(stmt2 != null) stmt2.close();
				if(stmt1 != null) stmt1.close();
				if(hcp != null) hcp.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void Edit() {
		check = 0;
		id = im.getId();
		title = im.getTitle();
		description = im.getDescription();
		category = im.getCategory();
		userId = im.getUserId();
		leaseTerm = im.getLeaseTerm();
		leaseValue = im.getLeaseValue();
		surcharge = im.getSurcharge();
		status = im.getStatus();
		String item_image_primary_link=null;
		
		LOGGER.info("Inside edit method...");
		String sql = "UPDATE items SET item_name=?, item_category=?, item_desc=?, item_lease_value=?, item_surcharge=?, item_lease_term=? WHERE item_id=? AND item_user_id=?";

		PreparedStatement stmt = null, stmt2 = null, ps1 = null;
		ResultSet rs = null, rs1 = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Checking if lease is active for this item..");
			String sqlCheckActiveLease = "SELECT * FROM leases WHERE lease_status=? AND lease_item_id=?";
			ps1 = hcp.prepareStatement(sqlCheckActiveLease);
			ps1.setString(1, "Active");
			ps1.setInt(2, id);
			
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				res.setData(FLS_ACTIVE_LEASE, "0", FLS_LEASE_ITEM_EDIT_M);
				return;
			}
			
			LOGGER.info("Creating statement...");

			String sql2 = "SELECT * FROM items WHERE item_id=? AND item_user_id=?";
			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setInt(1, id);
			stmt2.setString(2, userId);
			rs = stmt2.executeQuery();

			if (rs.next()) {
				item_image_primary_link = rs.getString("item_primary_image_link");
				stmt = hcp.prepareStatement(sql);

				LOGGER.info("Statement created. Executing edit query...");
				stmt.setString(1, title);
				stmt.setString(2, category);
				stmt.setString(3, description);
				stmt.setInt(4, leaseValue);
				stmt.setInt(5, surcharge);
				stmt.setString(6, leaseTerm);
				stmt.setInt(7, id);
				stmt.setString(8, userId);
				stmt.executeUpdate();
				
				message = "operation successfull edited item id : " + id;
				LOGGER.warning(message);
				Id = String.valueOf(rs.getInt("item_id"));
				Code = 002;
				res.setData(FLS_SUCCESS, Id, FLS_ITEMS_EDIT);
				
				// logging item status to item edited
				LogItem li = new LogItem();
				li.addItemLog(id, "Item Edited", "", item_image_primary_link);
			} else {
				LOGGER.warning("Entry not found in database!!");
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}

		} catch (SQLException e) {
			LOGGER.warning("Couldnt create a statement");
			if (e.getErrorCode() == MysqlErrorNumbers.ER_DATA_TOO_LONG
					&& e.getMessage().matches(".*\\bitem_image\\b.*")) {
				LOGGER.warning("The image size is too large. Please select image less than 16MB");
				res.setData(FLS_SQL_EXCEPTION_I, String.valueOf(FLS_SQL_EXCEPTION_I), FLS_SQL_EXCEPTION_IMAGE);
				LOGGER.warning(e.getErrorCode() + " " + e.getMessage());
			} else {
				res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
				e.printStackTrace();
			}
		}finally{
			try {
				if(rs != null)rs.close();
				
				if(stmt != null)stmt.close();
				if(stmt2 != null)stmt2.close();
				
				if(hcp != null)hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void EditStat() {
		check = 0;
		id = im.getId();
		status = im.getStatus();
		image = im.getImage();
		String item_uid=null,item_status=null;
		String link =null;
		int Lease_Id= im.getLeaseId();
		
		LOGGER.info("Inside edit stat method...");
		String sql = "UPDATE items SET item_status=? WHERE item_id=?";

		PreparedStatement stmt2 = null ,stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();

		FlsCredit credits = new FlsCredit();
		
		try {
			LOGGER.info("Creating statement...");

			String sql2 = "SELECT * FROM items WHERE item_id=?";
			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setInt(1, id);
			rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getInt("item_id");
				item_uid = rs.getString("item_uid");
				item_status = rs.getString("item_status");
				Id = String.valueOf(check);
				
				if(status.equals("OnHold")){
					
					if(item_status.equals("InStore") || item_status.equals("Wished")){
						// Updating credits
						credits.logCredit(rs.getString("item_user_id"), 10, "Item Put OnHold", "", Credit.SUB);
						
						try {
							Event event = new Event();
							event.createEvent("admin@frrndlease.com", rs.getString("item_user_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_ITEM_ON_HOLD, id, "Your Item <a href=\"" + URL + "/ItemDetails?uid=" + item_uid + "\">" + rs.getString("item_name") + "</a> has been put on hold because of inappropriate content.");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else{
						LOGGER.info("Item Cannot be put on Hold as it is not InStore or Wished...");
						res.setData(FLS_ITEMS_ES_HOLD, Id, FLS_ITEMS_ES_HOLD_M);
						return;
					}
					
				} else if (status.equals("InStore")){
					
					// Updating credits
					credits.logCredit(rs.getString("item_user_id"), 10, "Item Back InStore", "", Credit.ADD);
					
					try {
						Event event = new Event();
						event.createEvent("admin@frrndlease.com", rs.getString("item_user_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_ITEM_INSTORE, id, "Your Item <a href=\"" + URL + "/ItemDetails?uid=" + item_uid + "\">" + rs.getString("item_name") + "</a> is back in store.");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else if(status.equals("Archived")){
					
					if(!item_status.equals("InStore") && !item_status.equals("Wished")){
						LOGGER.info("Item Cannot be Archived as it is not InStore or Wished...");
						res.setData(FLS_ITEMS_ES_ARCHIVED, Id, FLS_ITEMS_ES_ARCHIVED_M);
						return;
					}else{
						LOGGER.info(item_status);
					}
				}
			}

			if (check != 0) {
				stmt = hcp.prepareStatement(sql);

				LOGGER.info("Statement created. Executing edit stat query...");
				stmt.setString(1, status);
				stmt.setInt(2, id);
				stmt.executeUpdate();
				message = "operation successfull edited item id : " + id;
				LOGGER.warning(message);
				Id = String.valueOf(check);
				Code = 002;
				res.setData(FLS_SUCCESS, Id, FLS_ITEMS_EDIT_STAT);
				
				FlsS3Bucket s3Bucket = new FlsS3Bucket(item_uid,Lease_Id);
				if(status.equals("PickedUpOut")){
					link = s3Bucket.uploadImage(Bucket_Name.ITEMS_BUCKET, Path_Name.ITEM_LEASE, File_Name.PICKED_UP_OUT, image, null);
				}else if(status.equals("LeaseStarted")){
					link = s3Bucket.uploadImage(Bucket_Name.ITEMS_BUCKET, Path_Name.ITEM_LEASE, File_Name.LEASE_STARTED, image, null);
				}else if(status.equals("PickedUpIn")){
					link = s3Bucket.uploadImage(Bucket_Name.ITEMS_BUCKET, Path_Name.ITEM_LEASE, File_Name.PICKED_UP_IN, image, null);
				}else if(status.equals("LeaseEnded")){
					link = s3Bucket.uploadImage(Bucket_Name.ITEMS_BUCKET, Path_Name.ITEM_LEASE, File_Name.LEASE_ENDED, image, null);
				}
				
				// logging item status
				LogItem li = new LogItem();
				li.addItemLog(id, status, "", link);
			} else {
				LOGGER.warning("Entry not found in database!!");
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}

		} catch (SQLException e) {
			LOGGER.warning("Couldnt create a statement");
			e.printStackTrace();
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}finally{
			try {
				if(rs != null)rs.close();
				if(stmt != null)stmt.close();
				if(stmt2 != null)stmt2.close();
				if(hcp != null)hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
	}

	private void GetNext() {
		check = 0;
		LOGGER.info("Inside GetNext Method..");
		String sql = "SELECT * FROM items WHERE item_id > ? ORDER BY item_id LIMIT 1";
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating a statement .....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getNext query...");
			stmt.setInt(1, token);

			rs = stmt.executeQuery();

			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getInt("item_id"));
				json.put("title", rs.getString("item_name"));
				json.put("category", rs.getString("item_category"));
				json.put("description", rs.getString("item_desc"));
				json.put("userId", rs.getString("item_user_id"));
				json.put("leaseValue", rs.getInt("item_lease_value"));
				json.put("leaseTerm", rs.getString("item_lease_term"));
				json.put("status", rs.getString("item_status"));
				json.put("uid", rs.getString("item_uid"));

				message = json.toString();
				LOGGER.info(message);
				check = rs.getInt("item_id");
				// System.out.println(id);
			}
			if (check != 0) {
				Code = FLS_SUCCESS;
				Id = String.valueOf(check);
			} else {
				Id = "0";
				message = FLS_END_OF_DB_M;
				Code = FLS_END_OF_DB;
			}

			res.setData(Code, Id, message);

			status = String.valueOf(id);
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (JSONException e) {
			res.setData(FLS_JSON_EXCEPTION, "0", FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void GetPrevious() {
		check = 0;
		LOGGER.info("Inside getPrevious method");
		String sql = "SELECT * FROM items WHERE item_id < ? ORDER BY item_id DESC LIMIT 1";
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating a statement .....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getprevious query...");
			stmt.setInt(1, token);

			rs = stmt.executeQuery();

			LOGGER.info("itemId\tName\tDescription\tQuantity");
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getInt("item_id"));
				json.put("title", rs.getString("item_name"));
				json.put("category", rs.getString("item_category"));
				json.put("description", rs.getString("item_desc"));
				json.put("userId", rs.getString("item_user_id"));
				json.put("leaseValue", rs.getInt("item_lease_value"));
				json.put("leaseTerm", rs.getString("item_lease_term"));
				json.put("status", rs.getString("item_status"));
				json.put("uid", rs.getString("item_uid"));

				message = json.toString();
				LOGGER.info(message);
				check = rs.getInt("item_id");
			}
			if (check != 0) { // checks if result Set is empty
				Code = FLS_SUCCESS;
				Id = String.valueOf(check);
			} else {
				Id = "0";
				message = FLS_END_OF_DB_M;
				Code = FLS_END_OF_DB;
			}

			res.setData(Code, Id, message);

			// status = String.valueOf(Id);
		} catch (SQLException e) {
			LOGGER.warning("Couldnt create a statement");
			e.printStackTrace();
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
		} catch (JSONException e) {
			res.setData(FLS_JSON_EXCEPTION, "0", FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void BrowseN() {
		check = 0;
		status = im.getStatus();

		LOGGER.info("Inside Browse N method");
		String sql = "SELECT  tb1.*, tb2.user_full_name, tb2.user_location FROM items tb1 INNER JOIN users tb2 ON tb1.item_user_id = tb2.user_id WHERE tb1.item_id > ? AND tb1.item_status= ? ORDER BY item_id LIMIT 1";
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating a statement .....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing Browse P query...");
			stmt.setInt(1, token);
			stmt.setString(2, status);

			rs = stmt.executeQuery();

			LOGGER.info("itemId\tName\tDescription\tQuantity");
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getInt("item_id"));
				json.put("title", rs.getString("item_name"));
				json.put("category", rs.getString("item_category"));
				json.put("description", rs.getString("item_desc"));
				json.put("userId", rs.getString("item_user_id"));
				json.put("leaseValue", rs.getInt("item_lease_value"));
				json.put("leaseTerm", rs.getString("item_lease_term"));
				json.put("status", rs.getString("item_status"));
				if(rs.getString("item_primary_image_link") == null || rs.getString("item_primary_image_link").equals("null"))
					json.put("primaryImageLink", "");
				else
					json.put("primaryImageLink", rs.getString("item_primary_image_link"));
				json.put("fullName", rs.getString("user_full_name"));
				json.put("location", rs.getString("user_location"));
				json.put("uid", rs.getString("item_uid"));

				message = json.toString();
				check = rs.getInt("item_id");
			}
			if (check != 0) { // checks if result Set is empty
				Id = String.valueOf(check);
				Code = FLS_SUCCESS;
			} else {
				Id = "0";
				message = FLS_END_OF_DB_M;
				Code = FLS_END_OF_DB;
			}

			res.setData(Code, Id, message);

			// status = String.valueOf(Id);
		} catch (SQLException e) {
			LOGGER.warning("Couldnt create a statement");
			e.printStackTrace();
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
		} catch (JSONException e) {
			res.setData(FLS_JSON_EXCEPTION, "0", FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void BrowseP() {
		check = 0;
		status = im.getStatus();

		LOGGER.info("Inside Browse P method");
		String sql = "SELECT * FROM items WHERE item_id < ? AND item_status= ? ORDER BY item_id DESC LIMIT 1";
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();

		try {
			LOGGER.info("Creating a statement .....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing BrowseP query...");
			stmt.setInt(1, token);
			stmt.setString(2, status);

			rs = stmt.executeQuery();

			LOGGER.info("itemId\tName\tDescription\tQuantity");
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getInt("item_id"));
				json.put("title", rs.getString("item_name"));
				json.put("category", rs.getString("item_category"));
				json.put("description", rs.getString("item_desc"));
				json.put("userId", rs.getString("item_user_id"));
				json.put("leaseValue", rs.getInt("item_lease_value"));
				json.put("leaseTerm", rs.getString("item_lease_term"));
				json.put("status", rs.getString("item_status"));
				if(rs.getString("item_primary_image_link") == null || rs.getString("item_primary_image_link").equals("null"))
					json.put("primaryImageLink", "");
				else
					json.put("primaryImageLink", rs.getString("item_primary_image_link"));
				json.put("uid", rs.getString("item_uid"));

				message = json.toString();
				LOGGER.info(message);
				check = rs.getInt("item_id");
			}
			if (check != 0) { // checks if result Set is empty
				Id = String.valueOf(check);
				Code = FLS_SUCCESS;
			} else {
				Id = "0";
				message = FLS_END_OF_DB_M;
				Code = FLS_END_OF_DB;
			}

			res.setData(Code, Id, message);

			// status = String.valueOf(Id);
		} catch (SQLException e) {
			LOGGER.warning("Couldnt create a statement");
			e.printStackTrace();
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
		} catch (JSONException e) {
			res.setData(FLS_JSON_EXCEPTION, "0", FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String GetLeaseTerm(int itemId) {
		String term = null;
		LOGGER.info("Inside getItemLeaseTerm");
		String sql = "SELECT item_lease_term FROM items WHERE item_id=?";
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("executing getItemLesae Term query");
			stmt = hcp.prepareStatement(sql);
			stmt.setInt(1, itemId);

			rs = stmt.executeQuery();
			while (rs.next()) {
				term = rs.getString("item_lease_term");
				LOGGER.warning(term);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return term;
	}

	private void DeletePosting() {
		LOGGER.info("Inside delete posting method....");
		
		id = im.getId();
		userId = im.getUserId();

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null;
		int rs2;
		
		try {
			// checking whether the item id is valid
			String sqlCheckItem = "SELECT * FROM items WHERE item_id=? AND item_user_id=?";
			ps1 = hcp.prepareStatement(sqlCheckItem);
			ps1.setInt(1, id);
			ps1.setString(2, userId);
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				switch(rs1.getString("item_status")){
					case "InStore":
						String sqlArchiveItem = "UPDATE `items` SET `item_status`='Archived' WHERE item_id = ? AND item_user_id = ?";
						ps2 = hcp.prepareStatement(sqlArchiveItem);
						ps2.setInt(1, id);
						ps2.setString(2, userId);
						rs2 = ps2.executeUpdate();
						
						if(rs2 == 1){
							LOGGER.info("Item id: " + id + " has been archived!!");
							res.setData(FLS_SUCCESS, Id, FLS_ITEMS_DELETE_POSTING);
							
							// logging item status to archived
							LogItem li = new LogItem();
							li.addItemLog(id, "Archived", "", rs1.getString("item_primary_image_link"));
							
							// Updating credits
							FlsCredit credits = new FlsCredit();
							credits.logCredit(userId, 10, "Item Archived", "", Credit.SUB);
							
							Event event = new Event();
							event.createEvent(userId, userId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_DELETE_ITEM, id, "Your Item " + id + "has been deleted from frrndlease store.");
						}else{
							LOGGER.info("Item: " + id + " has not been archived!!");
							res.setData(FLS_ITEMS_DP_DEFAULT, Id, FLS_ITEMS_DP_DEFAULT_M);
						}
						
						break;
					case "OnHold":
						LOGGER.info("This item id:" + id + " is on Hold so cannot archive it.");
						res.setData(FLS_ITEMS_DP_HOLD, Id, FLS_ITEMS_DP_HOLD_M);
						break;
					case "LeaseReady":
					case "PickedUpOut":
					case "LeaseStarted":
					case "LeaseEnded":
					case "PickedUpIn":
						LOGGER.info("This item id:" + id + " is leased so cannot archive it.");
						res.setData(FLS_ITEMS_DP_LEASED, Id, FLS_ITEMS_DP_LEASED_M);
						break;
					default:
						LOGGER.info("This item id:" + id + " is neither InStore or Leased");
						res.setData(FLS_ITEMS_DP_DEFAULT, Id, FLS_ITEMS_DP_DEFAULT_M);
						break;
				}
				
				// Updating data for badges
				FlsBadges badges = new FlsBadges(userId);
				badges.updateItemsCount();
			} else {
				LOGGER.warning("Entry not found in database!!");
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
		} catch (Exception e) {
			e.printStackTrace();
			res.setData(FLS_INVALID_OPERATION, "0", FLS_INVALID_OPERATION_M);
		} finally{
			try {
				if(rs1 != null) rs1.close();
				if(ps2 != null) ps2.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void getDetails(){
		id = im.getId();
		userId = im.getUserId();
		
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null, rs2 = null;
		Connection hcp = getConnectionFromPool();
		
		try{
			
			String sqlGetDetails = "SELECT * FROM items WHERE item_id=? AND item_user_id=?";
			ps1 = hcp.prepareStatement(sqlGetDetails);
			ps1.setInt(1, id);
			ps1.setString(2, userId);
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				
				JSONObject json = new JSONObject();
				json.put("id", rs1.getInt("item_id"));
				json.put("title", rs1.getString("item_name"));
				json.put("category", rs1.getString("item_category"));
				json.put("description", rs1.getString("item_desc"));
				json.put("leaseValue", rs1.getInt("item_lease_value"));
				json.put("leaseTerm", rs1.getString("item_lease_term"));
				if(rs1.getString("item_primary_image_link") == null || rs1.getString("item_primary_image_link").equals("null"))
					json.put("primaryImageLink", "");
				else
					json.put("primaryImageLink", rs1.getString("item_primary_image_link"));
				json.put("uid", rs1.getString("item_uid"));
				json.put("surcharge", rs1.getInt("item_surcharge"));
				
				FlsS3Bucket s3Bucket = new FlsS3Bucket(rs1.getString("item_uid"));
				json.put("imageLinks", s3Bucket.getImagesLinks());
				
				String checkUberOrNot = "SELECT (CASE WHEN user_fee_expiry IS NULL THEN false WHEN user_fee_expiry < NOW() THEN false ELSE true END) AS isMerchant FROM users WHERE user_id=?";
				ps2 = hcp.prepareStatement(checkUberOrNot);
				ps2.setString(1, userId);
				
				rs2 = ps2.executeQuery();
				
				if(rs2.next()){
					json.put("isMerchant", rs2.getBoolean("isMerchant"));
				}else{
					json.put("isMerchant", false);
				}
				
				message = json.toString();
				
				res.setData(FLS_SUCCESS, "0", message);
			}else{
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}
			
		}catch(Exception e){
			e.printStackTrace();
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
		}finally{
			try {
				if(rs2 != null) rs2.close();
				if(ps2 != null) ps2.close();
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			} catch (SQLException e) {
				e.printStackTrace();
				res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			}
		}
	}

	private void DeleteWishlist() {
		id = im.getId();
		userId = im.getUserId();
		String check2 = null;
		LOGGER.info("Inside delete wishlist method....");

		PreparedStatement stmt = null, stmt2 = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating statement...");

			// checking whether the input id is present in table
			String sql2 = "SELECT * FROM items WHERE item_id=? AND item_user_id=? AND item_status=?";
			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setInt(1, id);
			stmt2.setString(2, userId);
			stmt2.setString(3, "Wished");
			rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getInt("item_id");
				check2 = rs.getString("item_status");
				LOGGER.warning(check2);
			}

			if (check != 0) {
				String sql = "DELETE FROM items WHERE item_id = ? AND item_user_id = ?";
				stmt = hcp.prepareStatement(sql);

				// deletes entry from items table

				LOGGER.info("Statement created. Executing delete wishlist query..." + check);
				stmt.setInt(1, id);
				stmt.setString(2, userId);
				stmt.executeUpdate();
				status = "Wish Deleted!!";
				Id = String.valueOf(check);
				message = status;
				LOGGER.warning(message);
				Code = 001;
				res.setData(FLS_SUCCESS, Id, FLS_ITEMS_DELETE_WISH);
			} else {
				LOGGER.warning("Entry not found in database!!");
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
		}finally{
			try {
				rs.close();
				stmt.close();
				stmt2.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void SearchItem() {
		check = 0;
		title = im.getTitle();
		description = im.getDescription();
		category = im.getCategory();
		leaseValue = im.getLeaseValue();
		leaseTerm = im.getLeaseTerm();
		status = im.getStatus();
		LOGGER.warning(title + description + category + leaseValue + leaseTerm + token);

		LOGGER.info("Inside Search Item method");
		String sql = "SELECT * FROM items WHERE item_id > ? AND item_name LIKE ? AND item_desc LIKE ? AND item_category LIKE ? AND item_lease_term LIKE ? ORDER BY item_id LIMIT 1";
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating a statement .....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing BrowseP query...");
			stmt.setInt(1, token);
			stmt.setString(2, title);
			stmt.setString(3, description);
			stmt.setString(4, category);
			// stmt.setInt(5, leaseValue);
			stmt.setString(5, leaseTerm);

			rs = stmt.executeQuery();

			LOGGER.info("itemId\tName\tDescription\tQuantity");
			while (rs.next()) {
				LOGGER.info("itemId\tName\tDescription\tQuantity");
				JSONObject json = new JSONObject();
				json.put("itemId", rs.getInt("item_id"));
				json.put("title", rs.getString("item_name"));
				json.put("category", rs.getString("item_category"));
				json.put("description", rs.getString("item_desc"));
				json.put("userId", rs.getString("item_user_id"));
				json.put("leaseValue", rs.getInt("item_lease_value"));
				json.put("leaseTerm", rs.getString("item_lease_term"));
				json.put("status", rs.getString("item_status"));
				if(rs.getString("item_primary_image_link") == null || rs.getString("item_primary_image_link").equals("null"))
					json.put("primaryImageLink", "");
				else
					json.put("primaryImageLink", rs.getString("item_primary_image_link"));
				json.put("uid", rs.getString("item_uid"));

				message = json.toString();
				LOGGER.info(message);
				check = rs.getInt("item_id");
				break;
			}
			if (check != 0) { // checks if result Set is empty
				Id = String.valueOf(check);
				Code = FLS_SUCCESS;
			} else {
				Id = "0";
				message = FLS_END_OF_DB_M;
				Code = FLS_END_OF_DB;
			}

			res.setData(Code, Id, message);

			// status = String.valueOf(Id);
		} catch (SQLException e) {
			LOGGER.warning("Couldnt create a statement");
			e.printStackTrace();
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
		} catch (JSONException e) {
			res.setData(FLS_JSON_EXCEPTION, "0", FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void DeleteAdmin(){
		
		userId= im.getUserId();
		title =im.getTitle();
		
		LOGGER.info("Inside delete Admin method....");
		
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null;
		int rs2;
		Connection hcp = getConnectionFromPool();
		
		try {
			
			String sqlCheckItems = "SELECT * FROM items WHERE item_user_id=? AND item_name=?";
			ps1 = hcp.prepareStatement(sqlCheckItems);
			ps1.setString(1, userId);
			ps1.setString(2, title);
			rs1 = ps1.executeQuery();

			if (rs1.next()) {

				if(rs1.getString("item_uid") != null && rs1.getString("item_primary_image_link") != null){
					FlsS3Bucket s3Bucket = new FlsS3Bucket(rs1.getString("item_uid"));
					int d = s3Bucket.deleteImage(Bucket_Name.ITEMS_BUCKET, rs1.getString("item_primary_image_link"));
					if(d == 1){
						LOGGER.info("item image deleted from s3");
						s3Bucket.deleteImages();
					}else{
						res.setData(FLS_INVALID_OPERATION, "0", "Not Able to delete images");
						return;
					}
					
				}
				
				LOGGER.info("Deleting item from items table");
				String sqlDeleteItem = "DELETE FROM items WHERE item_id=?";

				ps2 = hcp.prepareStatement(sqlDeleteItem);
				ps2.setInt(1, rs1.getInt("item_id"));
				rs2 = ps2.executeUpdate();

				if(rs2 == 1){
					res.setData(FLS_SUCCESS, "0", FLS_ITEMS_DELETE);
					
					// Updating credits
					FlsCredit credits = new FlsCredit();
					credits.logCredit(rs1.getString("item_user_id"), 10, "Item Deleted Permanently", "", Credit.SUB);
					
				}else{
					res.setData(FLS_INVALID_OPERATION, "0", "Not able to delete from items table");
				}
				
				// Updating data for badges
				FlsBadges badges = new FlsBadges(rs1.getString("item_user_id"));
				badges.updateItemsCount();
				
			} else {
				LOGGER.warning("Entry not found in database!!");
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
		}finally{
			try {
				if(rs1 != null) rs1.close();
				if(ps2 != null) ps2.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

}
