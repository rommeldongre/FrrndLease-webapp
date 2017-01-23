package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import connect.Connect;
import pojos.ReqObj;
import pojos.ResObj;
import pojos.UserProfileReqObj;
import pojos.UserProfileResObj;
import util.FlsLogger;

public class UserProfileHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(UserProfileHandler.class.getName());

	private static UserProfileHandler instance = null;

	public static UserProfileHandler getInstance() {
		if (instance == null)
			instance = new UserProfileHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside Process Method of UserProfileHandler");

		UserProfileReqObj rq = (UserProfileReqObj) req;
		UserProfileResObj rs = new UserProfileResObj();

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null, ps3 = null;
		ResultSet rs1 = null, rs2 = null, rs3 = null;

		try {

			String sqlGetUserData = "SELECT * FROM users WHERE user_uid=?";
			ps1 = hcp.prepareStatement(sqlGetUserData);
			ps1.setString(1, rq.getUserUid());
			rs1 = ps1.executeQuery();
			
			if (rs1.next()) {
				
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
				rs.setUserFullName(rs1.getString("user_full_name"));
				rs.setUserId(rs1.getString("user_id"));
				rs.setSublocality(rs1.getString("user_sublocality"));
				rs.setLocality(rs1.getString("user_locality"));
				rs.setUserProfilePic(rs1.getString("user_profile_picture"));

				// Getting user profile details
				rs.setAddress(rs1.getString("user_address"));
				rs.setAbout(rs1.getString("about"));
				rs.setWebsite(rs1.getString("website"));
				rs.setEmail(rs1.getString("email"));
				rs.setPhoneNo(rs1.getString("phone_no"));
				rs.setBusinessHours(rs1.getString("business_hours"));
				
				// Getting Wished Items List
				String sqlGetWishedItems = "SELECT item_name FROM items WHERE item_user_id=? AND item_status='Wished' ORDER BY item_lastmodified DESC LIMIT 20";
				ps2 = hcp.prepareStatement(sqlGetWishedItems);
				ps2.setString(1, rs1.getString("user_id"));
				rs2 = ps2.executeQuery();

				String wishes = "";
				
				while (rs2.next()) {
					if(rs2.getString("item_name") != null)
						if(wishes.equals(""))
							wishes = rs2.getString("item_name");
						else
							wishes = wishes + "," + rs2.getString("item_name");
				}
				
				rs.setWishedList(wishes);
				
				// Getting list of friends
				String sqlGetFriends = "SELECT tb2.user_profile_picture, tb2.user_full_name, tb2.user_uid FROM friends tb1 LEFT JOIN users tb2 ON tb1.friend_id=tb2.user_id WHERE tb1.friend_user_id=? AND tb1.friend_status='signedup'";
				ps3 = hcp.prepareStatement(sqlGetFriends);
				ps3.setString(1, rs1.getString("user_id"));
				rs3 = ps3.executeQuery();
				
				JSONArray friends = new JSONArray();
				
				while(rs3.next()){
					JSONObject friend = new JSONObject();
					friend.put("userUid", rs3.getString("user_uid"));
					friend.put("userFullName", rs3.getString("user_full_name"));
					friend.put("userProfilePic", rs3.getString("user_profile_picture"));
					friends.put(friend);
				}
				
				rs.setFriends(friends);
				
			} else {
				LOGGER.warning("Not able to find user id for user uid - " + rq.getUserUid());
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				return rs;
			}

		} catch (SQLException e) {
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch(JSONException e) {
			rs.setCode(FLS_JSON_EXCEPTION);
			rs.setMessage(FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
		} catch (Exception e) {
			rs.setCode(FLS_INVALID_OPERATION);
			rs.setMessage(FLS_INVALID_OPERATION_M);
			e.printStackTrace();
		} finally {
			try {
				if(rs3 != null) rs3.close();
				if(ps3 != null) ps3.close();
				if(rs2 != null) rs2.close();
				if(ps2 != null) ps2.close();
				if(rs1 != null) rs1.close();
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		LOGGER.info("Finished process method ");
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
	}

}
