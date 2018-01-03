package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONObject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import util.Event.Event_Type;
import util.Event.Notification_Type;
import connect.Connect;

public class FlsWeeklyJob extends Connect implements org.quartz.Job {

	private FlsLogger LOGGER = new FlsLogger(FlsWeeklyJob.class.getName());

	private String URL = FlsConfig.prefixUrl;
	private int DIGEST_LIMIT = 2;

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub

		LOGGER.info("Starting FlsWeeklyJob...");
		sendWeeklyDigest();
	}

	private void sendWeeklyDigest() {

		LOGGER.info("Send Weekly Digest");

		String FRIENDLIST_URL, STORE_URL, POST_ITEM_URL;
		FRIENDLIST_URL = URL + "/myapp.html#/myfriendslist";
		STORE_URL = URL + "/myapp.html#/";
		POST_ITEM_URL = URL + "/myapp.html#/wizard";

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null, ps3 = null, ps4 = null;
		ResultSet rs1 = null, rs2 = null, rs3 = null;
		int userWishedItems=0;
		String next_reminder_date = null, signUpCheckBox = "", photoIdCheckBox = "", friendCheckBox = "",
				postItemCheckBox = "", wishitemCheckBox = "";
		List<JSONObject> recentWishes = null, wishedItems = null, postedItems = null, addedFriends = null;

		String  signedupCheckBoxTitle="<strong>Signed Up</strong>",
				photoIdCheckBoxTitle="<strong>Upload Photo Id</strong>",
				friendCheckBoxTitle="<strong>Invite 2 Friends</strong>",
				postItemCheckBoxTitle="<strong>Post 2 Items</strong>",
				wishitemCheckBoxtitle="<strong>Wish 2 Items</strong>";
		
		String signedupCheckBoxString = " - Welcome to FrrndLease!",
				photoIdCheckBoxString = " - You are in a Prime area. Just upload a Photo Id and avail of Prime Doorstep delivery!",
				friendCheckBoxString = " - Earn free credits by inviting your friends and increasing your trusted network",
				postItemCheckBoxString = " - Get 10 free credits to spend for every item that you offer other members",
				wishitemCheckBoxString = " - Get notified when items similar to your wishlist are available";

		Calendar currentCal = Calendar.getInstance();
		SimpleDateFormat currentSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentDate = currentSdf.format(currentCal.getTime());

		try {

			String sqlSelectUsersToRemind = "SELECT * FROM `users` WHERE user_email NOT IN('admin@frrndlease.com','ops@frrndlease.com') AND user_email IS NOT NULL AND user_email <> '' AND user_periodic_updates_flag=1";
			ps1 = hcp.prepareStatement(sqlSelectUsersToRemind);
			rs1 = ps1.executeQuery();

			String sqlSelectPrimePlaces = "SELECT * FROM `places`";
			ps2 = hcp.prepareStatement(sqlSelectPrimePlaces);
			rs2 = ps2.executeQuery();

			String sqlSelectPhotoIdDate = "SELECT * FROM `config` WHERE config.option=?";
			ps3 = hcp.prepareStatement(sqlSelectPhotoIdDate);
			ps3.setString(1, "photo_id_reminder");
			rs3 = ps3.executeQuery();

			while (rs3.next()) {
				next_reminder_date = rs3.getString("value");
			}

			LOGGER.info("Next date :" + next_reminder_date + " Current Date :" + currentDate);
			if (currentSdf.parse(currentDate).before(currentSdf.parse(next_reminder_date))) {
				LOGGER.warning("Current date is before the next time stamp date");
				return;
			}

			ArrayList<String> places = new ArrayList<String>();

			while (rs2.next()) {
				places.add(rs2.getString("locality").toUpperCase());
			}

			while (rs1.next()) {
				LOGGER.info("Sending a weekly digest to single user");
				recentWishes = getRecentWishItems();
				wishedItems = getWishItems(rs1.getString("user_id"), "Wished");
				userWishedItems = userWishedItems(rs1.getString("user_id"), "Wished");
				postedItems = getItems(rs1.getString("user_id"), "InStore");
				addedFriends = getFriends(rs1.getString("user_id"));

				if (places.contains(rs1.getString("user_locality").toUpperCase())) {
					if (!rs1.getBoolean("user_verified_flag")) {
						photoIdCheckBox = "<input type='" + "checkbox" + "' disabled='" + "disabled" + "' ><a href='"
								+ FRIENDLIST_URL + "'>" + photoIdCheckBoxTitle + "</a>"+photoIdCheckBoxString+"<br/>";
					} else {
						photoIdCheckBox = "<input type='" + "checkbox" + "' disabled='" + "disabled" + "' checked>"
								+ photoIdCheckBoxTitle+photoIdCheckBoxString + "<br/>";
					}

				}

				signUpCheckBox = "<input type='" + "checkbox" + "' disabled='" + "disabled" + "' checked>"
						+ signedupCheckBoxTitle+signedupCheckBoxString + "<br/>";

				if (addedFriends.size() < DIGEST_LIMIT) {
					friendCheckBox = "<input type='" + "checkbox" + "' disabled='" + "disabled" + "' ><a href='"
							+ FRIENDLIST_URL + "'>" + friendCheckBoxTitle + "</a>"+friendCheckBoxString+"<br/>";
				} else {
					friendCheckBox = "<input type='" + "checkbox" + "' disabled='" + "disabled" + "' checked>"
							+ friendCheckBoxTitle+friendCheckBoxString + "<br/>";
				}

				if (postedItems.size() < DIGEST_LIMIT) {
					postItemCheckBox = "<input type='" + "checkbox" + "' disabled='" + "disabled" + "' ><a href='"
							+ POST_ITEM_URL + "'>" + postItemCheckBoxTitle + "</a>"+postItemCheckBoxString+"<br/>";
				} else {
					postItemCheckBox = "<input type='" + "checkbox" + "' disabled='" + "disabled" + "' checked>"
							+ postItemCheckBoxTitle+postItemCheckBoxString + "<br/>";
				}

				if (userWishedItems < DIGEST_LIMIT) {
					wishitemCheckBox = "<input type='" + "checkbox" + "' disabled='" + "disabled" + "' ><a href='"
							+ STORE_URL + "'>" + wishitemCheckBoxtitle + "</a>"+wishitemCheckBoxString+"<br/>";
				} else {
					wishitemCheckBox = "<input type='" + "checkbox" + "' disabled='" + "disabled" + "' checked>"
							+ wishitemCheckBoxtitle+wishitemCheckBoxString + "<br/>";
				}

				String BODY = "<body><span style='" + "font-weight:bold;font-size:20px;"
						+ "'>Earn another Stripe - Complete your profile today!</span><br/> <br/><br/><br/>" + "<span style='"
						+ "font-weight:bold;font-size:18px;" + "'>Profile Completeness</span><br/> <br/><div align='"
						+ "left" + "' style='" + "padding-left: 20px;" + "'>" + signUpCheckBox + friendCheckBox
						+ postItemCheckBox + wishitemCheckBox + photoIdCheckBox + "<br/><br/><br></div>"
						+ "<span style='" + "font-weight:bold;font-size:18px;"
						+ "'>People Are Wishing For</span><br/><br/><table style='" + "width:100%;"
						+ "'><tbody><tr> ";

				if (recentWishes.size() == 0) {
					BODY = BODY + "No Items Wished";
					LOGGER.info("Wish Items null");
				} else {
					LOGGER.info("Wish Items not null");
					for (JSONObject l : recentWishes) {
						BODY = BODY + "<td align='" + "center;" + "'>" + l.getString("title") + "<br/><br/> </td>";
					}
				}

				BODY = BODY + " </tr></tbody></table><br/><br/><a href='" + POST_ITEM_URL + "'><button type='" + "button"
						+ "'>Post Item</button></a><br/><br/><br/><br/>" + "<span style='"
						+ "font-weight:bold;font-size:18px;"
						+ "'>Items Matching Wish List</span><br/><br/> <table style='" + "width:100%;"
						+ "'><tbody><tr>";
				
				if (wishedItems.size() == 0) {
					BODY = BODY + "No Items Matching WishList";
					LOGGER.info("Wish Items null");
				} else {
					LOGGER.info("Wish Items not null");
					for (JSONObject l : wishedItems) {
						BODY = BODY + "<td align='" + "center;" + "'>" + l.getString("title") + "<br/>"
								+ "<img width=\"100\" src='" + l.getString("imageLinks") + "' ></img><br/><br/> </td>";
					}
				}

				BODY = BODY + " </tr></tbody></table><br/><br/><a href='" + STORE_URL + "'><button type='" + "button"
						+ "'>Go To Store</button></a><br/><br/><br/><br/>" + "<span style='"
						+ "font-weight:bold;font-size:18px;"
						+ "'>Your Recently Posted Items</span><br/><br/> <table style='" + "width:100%;"
						+ "'><tbody><tr>";

				if (postedItems.size() == 0) {
					BODY = BODY + "No Items Posted";
				} else {
					LOGGER.info("Post Items not null");
					for (JSONObject l : postedItems) {
						BODY = BODY + "<td align='" + "center;" + "'>" + l.getString("title") + "<br/>"
								+ l.getString("desc") + "<br/>" + "<img width=\"100\" src='" + l.getString("imageLinks")
								+ "' ></img><br/><br/> </td>";
					}
				}

				BODY = BODY + "</tr></tbody></table><br/><br/><a href='" + POST_ITEM_URL + "'><button type='" + "button"
						+ "'>Post item</button></a><br/><br/><br/><br/> " + "<span style='"
						+ "font-weight:bold;font-size:18px;"
						+ "'>Your recently Added Friends</span><br/><br/>  <table style='" + "width:100%;"
						+ "'><tbody><tr>";

				if (addedFriends.size() == 0) {
					BODY = BODY + "No Friends Added";
				} else {
					LOGGER.info("Friends Added not null");
					for (JSONObject l : addedFriends) {
						BODY = BODY + "<td align='" + "center;" + "'>" + l.getString("title") + "<br/>"
								+ "<img width=\"100\" src='" + l.getString("imageLinks") + "' ></img><br/><br/> </td>";
					}
				}
				BODY = BODY + " </tr></tbody></table><br/><br/><a href='" + FRIENDLIST_URL + "'><button type='"
						+ "button" + "'>Add Friend</button></a></body>";

				LOGGER.info("HTML BODY is  :" + BODY);
				try {

					Event event = new Event();
					event.createEvent(rs1.getString("user_id"), rs1.getString("user_id"),
							Event_Type.FLS_EVENT_NOT_NOTIFICATION, Notification_Type.FLS_MAIL_WEEKLY_DIGEST, 0, BODY);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			String sqlAddNextPhotoIdDate = "UPDATE `config` SET `value`=(CURRENT_TIMESTAMP + INTERVAL 30 DAY) WHERE config.option=?";
			ps4 = hcp.prepareStatement(sqlAddNextPhotoIdDate);
			ps4.setString(1, "photo_id_reminder");
			ps4.executeUpdate();

		} catch (SQLException e) {
			LOGGER.warning("Error with the mysql operation in checkIdProof");
			e.printStackTrace();
		} catch (Exception e) {
			LOGGER.warning("Exception Occured");
			e.printStackTrace();
		} finally {
			try {
				if (rs1 != null)
					rs1.close();
				if (rs2 != null)
					rs2.close();
				if (rs3 != null)
					rs3.close();
				if (ps1 != null)
					ps1.close();
				if (ps2 != null)
					ps2.close();
				if (ps3 != null)
					ps3.close();
				if (ps4 != null)
					ps4.close();
				if (hcp != null)
					hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String leaderBoardString(){
		String leaderBoard="",comma=",   ";
		int limit =3;
		
		LOGGER.info("Inside leaderBoardString to fetch top 3 Monthly users");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		try {
			
			LOGGER.info("Getting monthly credit users");
			String GetUsersMonthlySql = "SELECT tb1.credit_user_id AS credit_monthly_user, SUM(tb1.credit_amount) AS totalCredit_monthly, tb2.user_full_name AS monthly_credit_user FROM `credit_log` tb1 LEFT JOIN users tb2 ON tb1.credit_user_id = tb2.user_id WHERE tb1.credit_date BETWEEN (CURDATE() - INTERVAL 30 DAY) AND CURDATE() GROUP BY credit_user_id ORDER BY totalCredit_monthly DESC LIMIT ?";
			ps1 = hcp.prepareStatement(GetUsersMonthlySql);
			ps1.setInt(1, limit);
			rs1 = ps1.executeQuery();
			
			while (rs1.next()) {
				leaderBoard = leaderBoard +"<b>"+rs1.getString("totalCredit_monthly")+"</b>&nbsp;"+rs1.getString("monthly_credit_user")+comma;
			}
			leaderBoard = leaderBoard.substring(0, leaderBoard.length() - comma.length());
			
			 
		} catch (Exception e) {
			// TODO: handle exception
			LOGGER.warning("Exception Occured while fetching the users wish item count!!");
			e.printStackTrace();
		}finally{
				try {
					if (rs1 != null)
						rs1.close();
					if (ps1 != null)
						ps1.close();
					if (hcp != null)
						hcp.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return leaderBoard;
		
	}
	private int userWishedItems(String userId, String status){
		int userWishItemCount =0;
		
		LOGGER.info("Inside userWishedItems to fetch users recent wish items count");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		try {
			
			String sqlGetUserWishItems = "SELECT * FROM `items` WHERE item_user_id=? AND item_status=? ORDER BY item_id DESC LIMIT "
					+ DIGEST_LIMIT;
			ps1 = hcp.prepareStatement(sqlGetUserWishItems);
			ps1.setString(1, userId);
			ps1.setString(2, status);
			rs1 = ps1.executeQuery();

			while (rs1.next()) {
				userWishItemCount = userWishItemCount+1;
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			LOGGER.warning("Exception Occured while fetching the users wish item count!!");
			e.printStackTrace();
		}finally{
				try {
					if (rs1 != null)
						rs1.close();
					if (ps1 != null)
						ps1.close();
					if (hcp != null)
						hcp.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return userWishItemCount;
	}

	private List<JSONObject> getRecentWishItems() {

		LOGGER.info("Inside getRecentWishItems to fetch recent wish items");

		List<JSONObject> wishItems = new ArrayList<>();

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;

		try {
			String sqlGetWishItems = "SELECT DISTINCT item_name FROM `items` WHERE item_status='Wished' AND item_user_id NOT IN ('anonymous') ORDER BY item_id DESC LIMIT 5";
			ps1 = hcp.prepareStatement(sqlGetWishItems);
			rs1 = ps1.executeQuery();

			while (rs1.next()) {
				JSONObject item = new JSONObject();
				item.put("title", rs1.getString("item_name"));
				wishItems.add(item);
			}
		} catch (Exception e) {
			LOGGER.warning("Exception Occured while fetching the recent wish items!!");
			e.printStackTrace();
		} finally {
			try {
				if (rs1 != null)
					rs1.close();
				if (hcp != null)
					hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return wishItems;
	}

	private List<JSONObject> getItems(String userId, String status) {
		List<JSONObject> listItems = new ArrayList<>();
		LOGGER.info("Inside getItems to fetch " + status + " items");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;

		try {

			String sqlGetItems = "SELECT * FROM `items` WHERE item_user_id=? AND item_status=? ORDER BY item_id DESC LIMIT "
					+ DIGEST_LIMIT;
			ps1 = hcp.prepareStatement(sqlGetItems);
			ps1.setString(1, userId);
			ps1.setString(2, status);
			rs1 = ps1.executeQuery();

			while (rs1.next()) {
				JSONObject item = new JSONObject();
				item.put("title", rs1.getString("item_name"));
				item.put("desc", rs1.getString("item_desc"));
				if (rs1.getString("item_primary_image_link") == null
						|| rs1.getString("item_primary_image_link").equals(""))
					item.put("imageLinks", "");
				else
					item.put("imageLinks", rs1.getString("item_primary_image_link"));
				listItems.add(item);
			}

			LOGGER.info("To fetch " + status + " items the string is " + listItems);
		} catch (SQLException e) {
			LOGGER.warning("Error with the mysql operation in checkIdProof");
			e.printStackTrace();
		} catch (Exception e) {
			LOGGER.warning("Exception Occured");
			e.printStackTrace();
		} finally {
			try {
				if (rs1 != null)
					rs1.close();
				if (hcp != null)
					hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return listItems;
	}

	private List<JSONObject> getWishItems(String userId, String status) {
		List<JSONObject> listWishItems = new ArrayList<>();
		LOGGER.info("Inside getFriends to fetch " + userId + "'s friends");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;

		try {

			String sqlGetItems = "SELECT * FROM `items` WHERE item_status=? AND item_user_id <> ? AND item_name IN (SELECT item_name FROM items WHERE item_user_id=? AND item_status=?) ORDER BY item_id DESC LIMIT "
					+ DIGEST_LIMIT;
			ps1 = hcp.prepareStatement(sqlGetItems);
			ps1.setString(1, "InStore");
			ps1.setString(2, userId);
			ps1.setString(3, userId);
			ps1.setString(4, status);

			rs1 = ps1.executeQuery();

			while (rs1.next()) {
				JSONObject WishItems = new JSONObject();
				WishItems.put("title", rs1.getString("item_name"));
				if (rs1.getString("item_primary_image_link") == null
						|| rs1.getString("item_primary_image_link").equals(""))
					WishItems.put("imageLinks", "");
				else
					WishItems.put("imageLinks", rs1.getString("item_primary_image_link"));
				listWishItems.add(WishItems);
			}

			LOGGER.info("To fetch " + userId + " items the string is " + listWishItems);
		} catch (SQLException e) {
			LOGGER.warning("Error with the mysql operation in checkIdProof");
			e.printStackTrace();
		} catch (Exception e) {
			LOGGER.warning("Exception Occured");
			e.printStackTrace();
		} finally {
			try {
				if (rs1 != null)
					rs1.close();
				if (hcp != null)
					hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return listWishItems;
	}

	private List<JSONObject> getFriends(String userId) {
		List<JSONObject> listFriends = new ArrayList<>();
		LOGGER.info("Inside getFriends to fetch " + userId + "'s friends");

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;

		try {

			String sqlGetItems = "SELECT tb_friends.friend_full_name, tb_users.user_profile_picture FROM `friends` tb_friends INNER JOIN users tb_users ON tb_friends.friend_id=tb_users.user_id WHERE tb_friends.friend_user_id=? AND tb_friends.friend_status=? ORDER BY tb_friends.friend_date DESC LIMIT "
					+ DIGEST_LIMIT;
			ps1 = hcp.prepareStatement(sqlGetItems);
			ps1.setString(1, userId);
			ps1.setString(2, "signedup");
			rs1 = ps1.executeQuery();

			while (rs1.next()) {
				JSONObject friend = new JSONObject();
				friend.put("title", rs1.getString("friend_full_name"));
				if (rs1.getString("user_profile_picture") == null || rs1.getString("user_profile_picture").equals(""))
					friend.put("imageLinks", "");
				else
					friend.put("imageLinks", rs1.getString("user_profile_picture"));
				listFriends.add(friend);
			}

			LOGGER.info("To fetch " + userId + " friends the string is " + listFriends);
		} catch (SQLException e) {
			LOGGER.warning("Error with the mysql operation in checkIdProof");
			e.printStackTrace();
		} catch (Exception e) {
			LOGGER.warning("Exception Occured");
			e.printStackTrace();
		} finally {
			try {
				if (rs1 != null)
					rs1.close();
				if (hcp != null)
					hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return listFriends;
	}
}
