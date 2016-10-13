package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import connect.Connect;
import pojos.EditProfileReqObj;
import pojos.EditProfileResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.FlsPlan;
import util.OAuth;

public class EditProfileHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(EditProfileHandler.class.getName());

	private static EditProfileHandler instance = null;

	public static EditProfileHandler getInstance() {
		if (instance == null)
			instance = new EditProfileHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub

		EditProfileReqObj rq = (EditProfileReqObj) req;
		EditProfileResObj rs = new EditProfileResObj();
		Connection hcp = getConnectionFromPool();

		LOGGER.info("Inside Process Method " + rq.getUserId());

		try {
			
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if(!oauthcheck.equals(rq.getUserId())){
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}
			
			String sql = "UPDATE users SET user_full_name=?, user_location=?, user_address=?, user_locality=?, user_sublocality=?, user_lat=?, user_lng=? , user_photo_id=? WHERE user_id=?";
			LOGGER.info("Creating Statement...");
			PreparedStatement ps = hcp.prepareStatement(sql);
			ps.setString(1, rq.getFullName());
			ps.setString(2, rq.getLocation());
			ps.setString(3, rq.getAddress());
			ps.setString(4, rq.getLocality());
			ps.setString(5, rq.getSublocality());
			ps.setFloat(6, rq.getLat());
			ps.setFloat(7, rq.getLng());
			ps.setString(8, rq.getPhotoId());
			ps.setString(9, rq.getUserId());

			LOGGER.info("statement created...executing update to users query");
			int result = ps.executeUpdate();
			ps.close();

			LOGGER.info("Update Query Result : " + result);
			
			// updating items table with lat lng
			String updateItemsLatLng = "UPDATE items SET item_lat=?, item_lng=? WHERE item_user_id=?";
			LOGGER.info("Creating statement for updating items table with lat lng.....");
			
			PreparedStatement ps1 = hcp.prepareStatement(updateItemsLatLng);
			ps1.setFloat(1, rq.getLat());
			ps1.setFloat(2, rq.getLng());
			ps1.setString(3, rq.getUserId());
			
			LOGGER.info("statement created...executing update to items query");
			ps1.executeUpdate();
			ps1.close();

			FlsPlan plan = new FlsPlan();
			plan.checkPlan(rq.getUserId());
			
			if (result == 1) {
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
				LOGGER.warning("Profile Updated!!");
			} else {
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
			}

		} catch (SQLException e) {
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		} catch (NullPointerException e) {
			rs.setCode(FLS_NULL_POINT);
			rs.setMessage(FLS_NULL_POINT_M);
		} finally {
			hcp.close();
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
