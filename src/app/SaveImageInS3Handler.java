package app;

import connect.Connect;
import pojos.ReqObj;
import pojos.ResObj;
import pojos.SaveImageInS3ReqObj;
import pojos.SaveImageInS3ResObj;
import util.FlsLogger;
import util.FlsS3Bucket;
import util.FlsS3Bucket.Bucket_Name;
import util.FlsS3Bucket.File_Name;
import util.FlsS3Bucket.Path_Name;
import util.OAuth;

public class SaveImageInS3Handler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(SaveImageInS3Handler.class.getName());

	private static SaveImageInS3Handler instance = null;

	public static SaveImageInS3Handler getInstance() {
		if (instance == null)
			instance = new SaveImageInS3Handler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		SaveImageInS3ReqObj rq = (SaveImageInS3ReqObj) req;
		SaveImageInS3ResObj rs = new SaveImageInS3ResObj();

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
			String uid = rq.getUid();

			if (image == null || image.isEmpty()) {
				rs.setCode(FLS_INVALID_OPERATION);
				rs.setMessage("This image is not good!!");
				return rs;
			}

			if (uid == null || uid.isEmpty()) {
				rs.setCode(FLS_INVALID_OPERATION);
				rs.setMessage("This uid is invalid!!");
				return rs;
			}
			
			if (existingLink.equals("null") || existingLink.isEmpty()){
				existingLink = null;
			}

			FlsS3Bucket s3Bucket = new FlsS3Bucket(uid);

			String link = null;

			if (rq.isPrimary()){
				link = s3Bucket.uploadImage(Bucket_Name.ITEMS_BUCKET, Path_Name.ITEM_POST, File_Name.ITEM_PRIMARY, image, link);
				if(link != null){
					if(s3Bucket.deleteImage(Bucket_Name.ITEMS_BUCKET, existingLink) == 1)
						s3Bucket.savePrimaryImageLink(link);
				}
			}else{
				link = s3Bucket.uploadImage(Bucket_Name.ITEMS_BUCKET, Path_Name.ITEM_POST, File_Name.ITEM_NORMAL, image, link);
				if(link != null && !link.equals(existingLink))
						s3Bucket.saveNormalImageLink(link);
			}
			
			if (link != null) {
				LOGGER.info("Link generated for the image : " + link);
				rs.setImageLink(link);
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
			} else {
				LOGGER.warning("Not able to upload image");
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage("Please check your internet connection!!");
			}

		} catch (NullPointerException e) {
			LOGGER.warning("Null pointer exception in SaveImageInS3Handler");
			e.printStackTrace();
			rs.setCode(FLS_NULL_POINT);
			rs.setMessage(FLS_NULL_POINT_M);
		} catch (Exception e) {
			LOGGER.warning("Exception in SaveImageInS3Handler");
			e.printStackTrace();
			rs.setCode(FLS_INVALID_OPERATION);
			rs.setMessage(FLS_INVALID_OPERATION_M);
		}

		LOGGER.info("Finished process method");
		return rs;
	}

	@Override
	public void cleanup() {
	}

}
