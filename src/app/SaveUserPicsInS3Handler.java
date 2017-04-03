package app;

import java.sql.Connection;
import java.sql.PreparedStatement;

import connect.Connect;
import pojos.ReqObj;
import pojos.ResObj;
import pojos.SaveUserPicsInS3ReqObj;
import pojos.SaveUserPicsInS3ResObj;
import util.Event;
import util.FlsLogger;
import util.FlsS3Bucket;
import util.FlsTicket;
import util.FlsS3Bucket.Bucket_Name;
import util.FlsS3Bucket.File_Name;
import util.FlsS3Bucket.Path_Name;
import util.OAuth;
import util.Event.Event_Type;
import util.Event.Notification_Type;

public class SaveUserPicsInS3Handler extends Connect implements AppHandler{

	private FlsLogger LOGGER = new FlsLogger(SaveUserPicsInS3Handler.class.getName());

	private static SaveUserPicsInS3Handler instance = null;

	public static SaveUserPicsInS3Handler getInstance() {
		if (instance == null)
			instance = new SaveUserPicsInS3Handler();
		return instance;
	}
	
	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		
		LOGGER.info("Inside process method of SaveUserPicsInS3Handler");
		
		SaveUserPicsInS3ReqObj rq = (SaveUserPicsInS3ReqObj) req;
		SaveUserPicsInS3ResObj rs = new SaveUserPicsInS3ResObj();
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		
		try {
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if (!oauthcheck.equals(rq.getUserId())) {
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}
			
			String image = rq.getImage();
			String existingLink = rq.getExistingLink();

			if (image == null || image.isEmpty()) {
				rs.setCode(FLS_INVALID_OPERATION);
				rs.setMessage("This image is not good!!");
				return rs;
			}
			
			if(existingLink != null){
				if(existingLink.isEmpty() || existingLink.equals("null") || !existingLink.contains(rq.getUserId()))
					existingLink = null;
			}

			FlsS3Bucket s3Bucket = new FlsS3Bucket(rq.getUserUid(), true);

			String link = null;
			
			if(rq.isMultiple()){
				link = s3Bucket.uploadImage(Bucket_Name.USERS_BUCKET, Path_Name.USER_PROFILE_PIC, File_Name.PROFILE_NORMAL, image, null);
				if(link != null){
					if(existingLink != null){
						s3Bucket.deleteImage(Bucket_Name.USERS_BUCKET, existingLink);
						s3Bucket.replaceNormalImageLink(link, existingLink);
					}else{
						s3Bucket.saveNormalImageLink(link);
					}
				}
			} else {
				if(rq.isProfile()){
					link = s3Bucket.uploadImage(Bucket_Name.USERS_BUCKET, Path_Name.USER_PROFILE_PIC, File_Name.PROFILE_PIC, image, null);
					if(link != null){
						if(existingLink != null)
							s3Bucket.deleteImage(Bucket_Name.USERS_BUCKET, existingLink);
						
						s3Bucket.saveUserPics(link, rq.isProfile());
					}
				}else{
					link = s3Bucket.uploadImage(Bucket_Name.USERS_BUCKET, Path_Name.USER_PHOTO_ID, File_Name.PHOTO_ID, image, null);
					if(link != null){
						if(existingLink != null)
							s3Bucket.deleteImage(Bucket_Name.USERS_BUCKET, existingLink);
						
						s3Bucket.saveUserPics(link, rq.isProfile());
						
						// Changing verification status of the user
						String sqlUpdateVerification = "UPDATE users SET user_verified_flag=?  WHERE user_id=?";
						ps1 = hcp.prepareStatement(sqlUpdateVerification);
						ps1.setInt(1, 0);
						ps1.setString(2, rq.getUserId());
						
						int rs1 = ps1.executeUpdate();
						
						if(rs1 == 1){
							FlsTicket ticket = new FlsTicket();
							ticket.addTicket(rq.getUserId(), null, "VERIFY_ID");
							
							LOGGER.info("User Verficiation changed to unverified and sending a notification to admin");
							try{
								Event event = new Event();
								event.createEvent(rq.getUserId(), "admin@frrndlease.com", Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_ADMIN_PHOTO_ID_UPLOAD, 0, "User Id - " + rq.getUserId() + " has uploaded the photoid. Please Verifiy!!");
							}catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			
			if (link != null) {
				LOGGER.info("Link generated for the user : " + link);
				rs.setImageLink(link);
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
			} else {
				LOGGER.warning("Not able to upload image");
				rs.setCode(FLS_INVALID_OPERATION);
				rs.setMessage(FLS_INVALID_OPERATION_M);
			}
				
		} catch (NullPointerException e) {
			LOGGER.warning("Null pointer exception in SaveUserPicsInS3Handler");
			e.printStackTrace();
			rs.setCode(FLS_NULL_POINT);
			rs.setMessage(FLS_NULL_POINT_M);
		} catch (Exception e) {
			LOGGER.warning("Exception in SaveUserPicsInS3Handler");
			e.printStackTrace();
			rs.setCode(FLS_INVALID_OPERATION);
			rs.setMessage(FLS_INVALID_OPERATION_M);
		} finally {
			try {
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		LOGGER.info("Finished process method");
		return rs;
	}

	@Override
	public void cleanup() {
	}

}
