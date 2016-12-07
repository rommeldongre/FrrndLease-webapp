package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.ReqObj;
import pojos.ResObj;
import pojos.UserItemObj;
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
				
				// Getting Wished Items List
				String sqlGetWishedItems = "SELECT item_name FROM items WHERE item_user_id=? AND item_status='Wished' ORDER BY item_lastmodified DESC LIMIT 20";
				ps2 = hcp.prepareStatement(sqlGetWishedItems);
				ps2.setString(1, rs1.getString("user_id"));
				rs2 = ps2.executeQuery();
				
				String[] wishedList = {};
				String wishes = null;
				
				while (rs2.next()) {
					if(rs2.getString("item_name") != null)
						if(wishes == null)
							wishes = rs2.getString("item_name");
						else
							wishes = wishes + "," + rs2.getString("item_name");
				}
				
				if(wishes != null)
					wishedList = wishes.split(",");
				
				rs.setWishedList(wishedList);
				
				// Getting InStore Items List
				String sqlGetInStoreItems = "SELECT item_name, item_uid, item_lease_value, item_lease_term, item_primary_image_link, item_lastmodified FROM items WHERE item_user_id=? AND item_status='InStore'";
				ps3 = hcp.prepareStatement(sqlGetInStoreItems);
				ps3.setString(1, rs1.getString("user_id"));
				rs3 = ps3.executeQuery();
				
				while(rs3.next()){
					UserItemObj item = new UserItemObj();
					item.setTitle(rs3.getString("item_name"));
					item.setUid(rs3.getString("item_uid"));
					item.setPrimaryImageLink(rs3.getString("item_primary_image_link"));
					item.setLeaseValue(rs3.getInt("item_lease_value"));
					item.setLeaseTerm(rs3.getString("item_lease_term"));
					rs.addItem(item);
				}
				
			} else {
				LOGGER.warning("Not ablt to find user id for user uid - " + rq.getUserUid());
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
			}

		} catch (SQLException e) {
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
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
