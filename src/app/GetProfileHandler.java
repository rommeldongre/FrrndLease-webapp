package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import adminOps.Response;
import connect.Connect;
import pojos.GetProfileReqObj;
import pojos.GetProfileResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.FlsS3Bucket;

public class GetProfileHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetProfileHandler.class.getName());

	private Response res = new Response();

	private static GetProfileHandler instance = null;

	public static GetProfileHandler getInstance() {
		if (instance == null)
			instance = new GetProfileHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub

		GetProfileReqObj rq = (GetProfileReqObj) req;
		GetProfileResObj rs = new GetProfileResObj();
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps = null;
		ResultSet result = null;

		LOGGER.info("Inside process method " + rq.getUserId());

		try {
			String sql = "SELECT * FROM users WHERE user_id=?";
			LOGGER.info("Creating Statement...");
			ps = hcp.prepareStatement(sql);
			ps.setString(1, rq.getUserId());

			LOGGER.info("statement created...executing select from users query");
			result = ps.executeQuery();

			LOGGER.info(result.toString());

			if (result.next()) {
				rs.setPlan(result.getString("user_plan"));
				rs.setUserUid(result.getString("user_uid"));
				rs.setFullName(result.getString("user_full_name"));
				rs.setMobile(result.getString("user_mobile"));
				rs.setEmail(result.getString("user_email"));
				rs.setLocation(result.getString("user_location"));
				rs.setCredit(result.getInt("user_credit"));
				rs.setAddress(result.getString("user_address"));
				rs.setLocality(result.getString("user_locality"));
				rs.setSublocality(result.getString("user_sublocality"));
				rs.setLat(result.getFloat("user_lat"));
				rs.setLng(result.getFloat("user_lng"));
				rs.setUserStatus(result.getString("user_status"));
				rs.setUserSecStatus(result.getString("user_sec_status"));
				rs.setUserNotification(result.getString("user_notification"));
				rs.setReferralCode(result.getString("user_referral_code"));
				rs.setPhotoId(result.getString("user_photo_id"));
				rs.setPhotoIdVerified(result.getBoolean("user_verified_flag"));
				rs.setProfilePic(result.getString("user_profile_picture"));
				rs.setLiveStatus(result.getInt("user_live_status"));
				rs.setUserFeeExpiry(result.getString("user_fee_expiry"));
				rs.setAbout(result.getString("about"));
				rs.setWebsite(result.getString("website"));
				rs.setMail(result.getString("email"));
				rs.setPhoneNo(result.getString("phone_no"));
				rs.setBusinessHours(result.getString("business_hours"));
				
				FlsS3Bucket s3Bucket = new FlsS3Bucket(result.getString("user_uid"));
				rs.setImageLinks(s3Bucket.getImagesLinks());
				
				rs.setCode(FLS_SUCCESS);

				LOGGER.info("Printing out ResultSet: " + rs.getFullName() + ", " + rs.getMobile() + ", "
						+ rs.getLocation() + ", " + rs.getCredit() + ", " + rs.getAddress());
			} else {
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
			}
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		} finally {
			if(result != null)
				result.close();
			if(ps != null)
				ps.close();
			if(hcp != null)
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
