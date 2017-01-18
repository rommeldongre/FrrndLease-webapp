package app;

import connect.Connect;
import pojos.DeleteUserPicsFromS3ReqObj;
import pojos.DeleteUserPicsFromS3ResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.FlsS3Bucket;
import util.OAuth;
import util.FlsS3Bucket.Bucket_Name;

public class DeleteUserPicsFromS3Handler extends Connect implements AppHandler{

	private FlsLogger LOGGER = new FlsLogger(DeleteUserPicsFromS3Handler.class.getName());
	
	private static DeleteUserPicsFromS3Handler instance = null;
	
	public static DeleteUserPicsFromS3Handler getInstance(){
		if(instance == null)
			instance = new DeleteUserPicsFromS3Handler();
		return instance;
	}
	
	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		
		LOGGER.info("Inside Process Method of DeleteUserPicsFromS3Handler");
		
		DeleteUserPicsFromS3ReqObj rq = (DeleteUserPicsFromS3ReqObj) req;
		DeleteUserPicsFromS3ResObj rs = new DeleteUserPicsFromS3ResObj();
		
		try {
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if (!oauthcheck.equals(rq.getUserId())) {
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}

			String link = rq.getLink();
			
			if (link.equals("null") || link.isEmpty() || link == null || link.equals("")){
				rs.setCode(FLS_INVALID_OPERATION);
				rs.setMessage("Not able to delete this image!!");
				return rs;
			}
			
			FlsS3Bucket s3Bucket = new FlsS3Bucket(rq.getUserUid(), rq.isProfile());
			
			int result = s3Bucket.deleteImage(Bucket_Name.USERS_BUCKET, link);
			
			if(result == 1){
				s3Bucket.deleteUserPics();
			}
			
			rs.setCode(FLS_SUCCESS);
			rs.setMessage(FLS_SUCCESS_M);
		
		} catch (NullPointerException e) {
			LOGGER.warning("Null pointer exception in DeleteUserPicsFromS3Handler");
			e.printStackTrace();
			rs.setCode(FLS_NULL_POINT);
			rs.setMessage(FLS_NULL_POINT_M);
		} catch (Exception e) {
			LOGGER.warning("Exception in DeleteUserPicsFromS3Handler");
			e.printStackTrace();
			rs.setCode(FLS_INVALID_OPERATION);
			rs.setMessage(FLS_INVALID_OPERATION_M);
		}

		LOGGER.info("process method completed");
		return rs;
	}

	@Override
	public void cleanup() {
	}

	
	
}
