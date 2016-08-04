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
import tableOps.Store;
import pojos.ItemsModel;
import util.FlsEnums;
import util.LogItem;
import util.AwsSESEmail;
import util.FlsLogger;

public class Items extends Connect {

	private FlsLogger LOGGER = new FlsLogger(Items.class.getName());

	private String operation, category, leaseTerm, userId, status, title, description, message, Id = null, image;
	private int leaseValue, id, token, Code;
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
		
		PreparedStatement stmt = null,s = null, s1 =null, checkWishItem_ps = null; 
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
				String sql = "insert into items (item_name, item_category, item_desc, item_user_id, item_lease_value, item_lease_term, item_status, item_image) values (?,?,?,?,?,?,?,?)";
				stmt = hcp.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	
				LOGGER.info("Statement created. Executing query.....");
				stmt.setString(1, title);
				stmt.setString(2, category);
				stmt.setString(3, description);
				stmt.setString(4, userId);
				stmt.setInt(5, leaseValue);
				stmt.setString(6, leaseTerm);
				stmt.setString(7, status);
				stmt.setString(8, image);
				stmt.executeUpdate();
				
				// getting the last item inserted id and appending it with the title to generate a uid
				keys = stmt.getGeneratedKeys();
				keys.next();
				int itemId = keys.getInt(1);
				
				String uid = title + " " + itemId;
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
	
				String status_W = im.getStatus(); // To be used to check if Request
													// is from WishItem API.
				if (!FLS_WISHLIST_ADD.equals(status_W)) {
					try {
						AwsSESEmail newE = new AwsSESEmail();
						newE.send(userId, FlsEnums.Notification_Type.FLS_MAIL_POST_ITEM, im);
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
			}else{
				res.setData(FLS_DUPLICATE_ENTRY, null, FLS_POST_ITEM_F_M);
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
		check = 0;
		LOGGER.info("Inside delete method....");
		String sql = "DELETE FROM items WHERE item_id = ?";

		PreparedStatement stmt2 = null, stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating statement...");

			// checking whether the input id is present in table
			String sql2 = "SELECT * FROM items WHERE item_id=?";
			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setInt(1, id);
			rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getInt("item_id");
			}

			if (check != 0) {
				stmt = hcp.prepareStatement(sql);

				LOGGER.info("Statement created. Executing delete query..." + check);
				stmt.setInt(1, id);
				stmt.executeUpdate();
				status = "operation successfull deleted item id :" + id;
				LOGGER.warning(status);
				Id = String.valueOf(check);
				message = status;
				Code = 001;
				res.setData(FLS_SUCCESS, Id, FLS_ITEMS_DELETE);
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

	private void Edit() {
		check = 0;
		id = im.getId();
		title = im.getTitle();
		description = im.getDescription();
		category = im.getCategory();
		userId = im.getUserId();
		leaseTerm = im.getLeaseTerm();
		leaseValue = im.getLeaseValue();
		status = im.getStatus();
		image = im.getImage();

		LOGGER.info("Inside edit method...");
		String sql = "UPDATE items SET item_name=?, item_category=?, item_desc=?, item_lease_value=?, item_lease_term=?, item_image=? WHERE item_id=? AND item_user_id=?";

		PreparedStatement stmt = null, stmt2 = null, s = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating statement...");

			String sql2 = "SELECT * FROM items WHERE item_id=? AND item_user_id=?";
			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setInt(1, id);
			stmt2.setString(2, userId);
			rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getInt("item_id");
			}

			if (check != 0) {
				stmt = hcp.prepareStatement(sql);

				LOGGER.info("Statement created. Executing edit query...");
				stmt.setString(1, title);
				stmt.setString(2, category);
				stmt.setString(3, description);
				stmt.setInt(4, leaseValue);
				stmt.setString(5, leaseTerm);
				stmt.setString(6, image);
				stmt.setInt(7, id);
				stmt.setString(8, userId);
				stmt.executeUpdate();
				
				String uid = title + " " + check;
				uid = uid.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();
				
				// updating the item_uid value of the last item inserted
				String sqlUpdateUID = "UPDATE items SET item_uid=? WHERE item_id=?";
				s = hcp.prepareStatement(sqlUpdateUID);
				s.setString(1, uid);
				s.setInt(2, check);
				s.executeUpdate();
				
				message = "operation successfull edited item id : " + id;
				LOGGER.warning(message);
				Id = String.valueOf(check);
				Code = 002;
				res.setData(FLS_SUCCESS, Id, FLS_ITEMS_EDIT);
				
				// logging item status to item edited
				LogItem li = new LogItem();
				li.addItemLog(id, "Item Edited", "", image);
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
				rs.close();
				
				stmt.close();
				stmt2.close();
				s.close();
				
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void EditStat() {
		check = 0;
		id = im.getId();
		status = im.getStatus();
		image = im.getImage();

		LOGGER.info("Inside edit stat method...");
		String sql = "UPDATE items SET item_status=? WHERE item_id=?";

		PreparedStatement stmt2 = null ,stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating statement...");

			String sql2 = "SELECT * FROM items WHERE item_id=?";
			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setInt(1, id);
			rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getInt("item_id");
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
				
				// logging item status
				LogItem li = new LogItem();
				li.addItemLog(id, status, "", image);
			} else {
				LOGGER.warning("Entry not found in database!!");
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}

		} catch (SQLException e) {
			LOGGER.warning("Couldnt create a statement");
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
				json.put("image", rs.getString("item_image"));
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
				json.put("image", rs.getString("item_image"));
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
		id = im.getId();
		userId = im.getUserId();
		String check2 = null;
		LOGGER.info("Inside delete posting method....");

		PreparedStatement stmt = null, stmt2 = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating statement...");

			// checking whether the input id is present in table
			String sql2 = "SELECT * FROM items WHERE item_id=? AND item_user_id=?";
			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setInt(1, id);
			stmt2.setString(2, userId);
			rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getInt("item_id");
				check2 = rs.getString("item_status");
				LOGGER.warning(check2);
			}

			if (check != 0) {

				switch (check2) {

				case "InStore":
					Store st = new Store();
					st.DeleteP(id);// deletes entry from store table

					String sql = "UPDATE `items` SET `item_status`='Archived' WHERE item_id = ? AND item_user_id = ?";
					stmt = hcp.prepareStatement(sql);

					// deletes entry from items table

					LOGGER.info("Statement created. Executing delete posting query..." + check);
					stmt.setInt(1, id);
					stmt.setString(2, userId);
					stmt.executeUpdate();
					status = "Your posted item is deleted!!";
					Id = String.valueOf(check);
					message = status;
					Code = 001;
					res.setData(FLS_SUCCESS, Id, FLS_ITEMS_DELETE_POSTING);
					
					// logging item status to archived
					LogItem li = new LogItem();
					li.addItemLog(id, "Archived", "", "");
					break;

				case "Leased":
					status = "Item is leased, close the lease first!!!";
					Id = String.valueOf(check);
					message = status;
					Code = 215;
					res.setData(FLS_ITEMS_DP_LEASED, Id, FLS_ITEMS_DP_LEASED_M);
					break;

				default:
					status = "Item is niether posted nor leased!!!";
					Id = String.valueOf(check);
					message = status;
					Code = 216;
					res.setData(FLS_ITEMS_DP_DEFAULT, Id, FLS_ITEMS_DP_DEFAULT_M);
					break;
				}
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
				Wishlist wish = new Wishlist();
				wish.DeleteW(id);

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
				json.put("image", rs.getString("item_image"));
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
	/*
	 * private void GetMax(){ //id = im.getId(); getConnection(); try {
	 * Statement stmt1 = connection.createStatement(); String sql =
	 * "SELECT MAX(itemId),title,description FROM items"; ResultSet rs =
	 * stmt1.executeQuery(sql); while(rs.next()) {
	 * System.out.println(rs.getInt("itemId")+"\t"+rs.getString("title")+"\t"+rs
	 * .getString("description")); id = rs.getInt("itemId"); }
	 * 
	 * } catch (SQLException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } }
	 * 
	 * /*private void updateCounter() { try { Statement stm =
	 * connection.createStatement(); String sql1 = "SET @num := 0;"; String sql2
	 * = "UPDATE items SET counter = @num := (@num+1);"; String sql3 =
	 * "ALTER TABLE items AUTO_INCREMENT = 1;";
	 * 
	 * stm.addBatch(sql1); stm.addBatch(sql2); stm.addBatch(sql3);
	 * 
	 * System.out.println("Executing Query..."); stm.executeBatch();
	 * 
	 * } catch (SQLException e) { System.out.println(
	 * "Couldn't update counter column.."); e.printStackTrace(); } }
	 * 
	 * private void Search(int id) {
	 * 
	 * String sql = "SELECT * FROM items WHERE id=?"; getConnection(); try {
	 * System.out.println("Searching for item in table..."); PreparedStatement
	 * stmt = connection.prepareStatement(sql); stmt.setInt(1, id); rs =
	 * stmt.executeQuery(); } catch (SQLException e) { System.out.println(
	 * "Couldnt create a statement"); e.printStackTrace(); } }
	 */

}
