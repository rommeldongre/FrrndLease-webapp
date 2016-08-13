package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import connect.Connect;
import util.Event.Event_Type;
import util.Event.Notification_Type;

public class MatchItems extends Connect {

	private FlsLogger LOGGER = new FlsLogger(MatchItems.class.getName());

	// being called when item is posted
	public void checkWishlist(String title, String userId, String uid, int itemId) {

		// posted items title
		String userItemTitle[] = title.toLowerCase().split(" ");

		// posted items user id
		String itemUserId = userId;
		
		String longestWord = getLongestString(userItemTitle);

		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating getwishlistnames sql statement");
			String sqlGetWishlistNames = "SELECT item_user_id,item_name FROM items WHERE item_name LIKE ? AND item_status='Wished' AND item_user_id<>? LIMIT 3";
			ps1 = hcp.prepareStatement(sqlGetWishlistNames);
			ps1.setString(1, "%" + longestWord + "%");
			ps1.setString(2, itemUserId);
			rs1 = ps1.executeQuery();

			while (rs1.next()) {
				try {
					Event event = new Event();
					event.createEvent(rs1.getString("item_user_id"), rs1.getString("item_user_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_MATCH_WISHLIST_ITEM, itemId, "An item posted to the frrndlease <a href=\"/flsv2/ItemDetails?uid=" + uid + "\">" + title + "</a> match your wished item <strong>'" + rs1.getString("item_name") + "'</strong>");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				if(rs1!=null) rs1.close();
				if(ps1!=null) ps1.close();
				if(hcp!=null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// being called when item is added to the wish list
	public List<JSONObject> checkPostedItems(int wishedItemId) {

		// getting the wished item title from items table
		String sqlGetWishedItemTitle = "SELECT item_name,item_user_id FROM items WHERE item_id=?";

		// getting matched posted items from the items table
		String sqlGetPostedItemObjs = "SELECT * FROM items WHERE item_name LIKE ? AND item_status='InStore' AND item_user_id<>? LIMIT 3";

		List<JSONObject> listItems = new ArrayList<>();
		
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null, rs2 = null;
		Connection hcp = getConnectionFromPool();
		try {

			LOGGER.info("creating get wishlist item title query");
			ps1 = hcp.prepareStatement(sqlGetWishedItemTitle);
			ps1.setInt(1, wishedItemId);
			rs1 = ps1.executeQuery();

			if (rs1.next()) {
				String wishedItemTitle[] = rs1.getString("item_name").toLowerCase().split(" ");
				
				String longestWord = getLongestString(wishedItemTitle);

				ps2 = hcp.prepareStatement(sqlGetPostedItemObjs);
				LOGGER.info("creating get posted items obj query");
				ps2.setString(1, "%" + longestWord + "%");
				ps2.setString(2, rs1.getString("item_user_id"));
				rs2 = ps2.executeQuery();

				while (rs2.next()) {
					JSONObject item = new JSONObject();
					item.put("itemId", rs2.getInt("item_id"));
					item.put("title", rs2.getString("item_name"));
					item.put("category", rs2.getString("item_category"));
					if(rs2.getString("item_desc") == null || rs2.getString("item_desc").equals(""))
						item.put("description", "");
					else
						item.put("description", rs2.getString("item_desc"));
					item.put("itemUserId", rs2.getString("item_user_id"));
					item.put("leaseValue", rs2.getInt("item_lease_value"));
					item.put("leaseTerm", rs2.getString("item_lease_term"));
					item.put("status", rs2.getString("item_status"));
					if(rs2.getString("item_image") == null || rs2.getString("item_image").equals(""))
						item.put("image", "");
					else
						item.put("image", rs2.getString("item_image"));
					item.put("uid", rs2.getString("item_uid"));
					
					listItems.add(item);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			LOGGER.warning(e.getMessage());
		} catch (JSONException e) {
			e.printStackTrace();
			LOGGER.warning(e.getMessage());
		}finally{
			try {
				if(rs1!=null) rs1.close();
				if(rs2!=null) rs2.close();
				if(ps1!=null) ps1.close();
				if(ps2!=null) ps2.close();
				if(hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return listItems;
	}
	
	private String getLongestString(String[] array) {
	      int maxLength = 0;
	      String longestString = null;
	      for (String s : array) {
	          if (s.length() > maxLength) {
	              maxLength = s.length();
	              longestString = s;
	          }
	      }
	      return longestString;
	  }

}
