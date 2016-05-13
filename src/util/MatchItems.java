package util;

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
		getConnection();
	}

	// being called when item is added to the wish list
	public MatchItems(int id) {
		this.wishedItemId = id;
		getConnection();
	}

	public void checkWishlist() {

		// posted items title
		String userItemTitle[] = itemObj.getTitle().toLowerCase().split(" ");

		// posted items user id
		String itemUserId = itemObj.getUserId();

		String sqlGetWishlistNames = "SELECT items.item_name,items.item_user_id FROM items INNER JOIN wishlist ON items.item_id=wishlist.wishlist_item_id  AND items.item_user_id<>'"
				+ itemUserId + "'";

		try {

			PreparedStatement ps1 = connection.prepareStatement(sqlGetWishlistNames);
			LOGGER.info("Creating getwishlistnames sql statement");
			ResultSet rs1 = ps1.executeQuery();

			while (rs1.next()) {
				String wlItemTitle[] = rs1.getString("item_name").toLowerCase().split(" ");

				// comparing the title of the wish list items and the posted
				// items.
				// If there is a match then an email is sent to the wish list
				// item's user.
				if (compareTitles(userItemTitle, wlItemTitle)) {
					try {
						AwsSESEmail newE = new AwsSESEmail();
						newE.send(rs1.getString("item_user_id"), FlsSendMail.Fls_Enum.FLS_MAIL_MATCH_WISHLIST_ITEM,
								itemObj);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void checkPostedItems() {

		// getting the wished item title from items table
		String sqlGetWishedItemTitle = "SELECT item_name,item_user_id FROM items WHERE item_id=?";

		// getting matched posted items from the items table
		String sqlGetPostedItemObjs = "SELECT * FROM items WHERE item_name LIKE ? AND item_status='InStore' AND item_user_id<>? LIMIT 3";

		List<PostItemReqObj> listItems = new ArrayList<>();

		try {

			PreparedStatement ps1 = connection.prepareStatement(sqlGetWishedItemTitle);
			LOGGER.info("creating get wishlist item title query");
			ps1.setInt(1, wishedItemId);
			ResultSet rs1 = ps1.executeQuery();

			if (rs1.next()) {
				String wishedItemTitle[] = rs1.getString("item_name").toLowerCase().split(" ");

				int len = wishedItemTitle.length;
				
				String longestWord = getLongestString(wishedItemTitle);

				PreparedStatement ps2 = connection.prepareStatement(sqlGetPostedItemObjs);
				LOGGER.info("creating get posted items obj query");
				ps2.setString(1, "%" + longestWord + "%");
				ps2.setString(2, rs1.getString("item_user_id"));
				ResultSet rs2 = ps2.executeQuery();

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

				try {
					AwsSESEmail newE = new AwsSESEmail();
					newE.send(rs1.getString("item_user_id"), FlsSendMail.Fls_Enum.FLS_MAIL_MATCH_POST_ITEM, listItems);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			LOGGER.warning(e.getMessage());
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
