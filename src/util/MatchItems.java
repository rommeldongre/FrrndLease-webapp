package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import connect.Connect;
import pojos.PostItemReqObj;

public class MatchItems extends Connect {

	private FlsLogger LOGGER = new FlsLogger(MatchItems.class.getName());

	PostItemReqObj itemObj;

	int wishedItemId;

	// being called from post items handler
	public MatchItems(PostItemReqObj rq) {
		this.itemObj = (PostItemReqObj) rq;
	}

	// being called when item is added to the wish list
	public MatchItems(int id) {
		this.wishedItemId = id;
	}

	public void checkWishlist() {

		// posted items title
		String userItemTitle[] = itemObj.getTitle().toLowerCase().split(" ");

		// posted items user id
		String itemUserId = itemObj.getUserId();
		
		String longestWord = getLongestString(userItemTitle);

		String sqlGetWishlistNames = "SELECT item_user_id FROM items WHERE item_name LIKE ? AND item_status='Wished' AND item_user_id<>? LIMIT 3";
		
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		Connection hcp = getConnectionFromPool();
		try {

			LOGGER.info("Creating getwishlistnames sql statement");
			ps1 = hcp.prepareStatement(sqlGetWishlistNames);
			ps1.setString(1, "%" + longestWord + "%");
			ps1.setString(2, itemUserId);
			rs1 = ps1.executeQuery();

			while (rs1.next()) {
				try {
					AwsSESEmail newE = new AwsSESEmail();
					newE.send(rs1.getString("item_user_id"), FlsEnums.Notification_Type.FLS_MAIL_MATCH_WISHLIST_ITEM,itemObj);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void checkPostedItems() {

		// getting the wished item title from items table
		String sqlGetWishedItemTitle = "SELECT item_name,item_user_id FROM items WHERE item_id=?";

		// getting matched posted items from the items table
		String sqlGetPostedItemObjs = "SELECT * FROM items WHERE item_name LIKE ? AND item_status='InStore' AND item_user_id<>? LIMIT 3";

		List<PostItemReqObj> listItems = new ArrayList<>();
		
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
					PostItemReqObj item = new PostItemReqObj();
					item.setId(rs2.getInt("item_id"));
					item.setTitle(rs2.getString("item_name"));
					item.setCategory(rs2.getString("item_category"));
					item.setDescription(rs2.getString("item_desc"));
					item.setUserId(rs2.getString("item_user_id"));
					item.setLeaseValue(rs2.getInt("item_lease_value"));
					item.setLeaseTerm(rs2.getString("item_lease_term"));
					item.setStatus(rs2.getString("item_status"));
					item.setImage(rs2.getString("item_image"));

					listItems.add(item);
				}

				if(!listItems.isEmpty()){
					try {
						AwsSESEmail newE = new AwsSESEmail();
						newE.send(rs1.getString("item_user_id"), FlsEnums.Notification_Type.FLS_MAIL_MATCH_POST_ITEM, listItems);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			LOGGER.warning(e.getMessage());
		}finally{
			if(rs1!=null)
				try {
					if(rs1!=null) rs1.close();
					if(rs2!=null) rs2.close();
					if(ps1!=null) ps1.close();
					if(ps2!=null) ps2.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
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

	private boolean compareTitles(String title1[], String title2[]) {

		// getting the array length of both the titles.
		int len1 = title1.length;
		int len2 = title2.length;

		if (len1 > 3)
			len1 = 3;
		if (len2 > 3)
			len2 = 3;

		for (int i = 0; i < len1; i++) {
			for (int j = 0; j < len2; j++) {
				if (title1[i].equals(title2[j]))
					return true;
			}
		}
		return false;
	}
}
