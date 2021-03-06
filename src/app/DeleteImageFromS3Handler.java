package app;

import connect.Connect;
import pojos.DeleteImageFromS3ReqObj;
import pojos.DeleteImageFromS3ResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.FlsS3Bucket;
import util.OAuth;
import util.FlsS3Bucket.Bucket_Name;

public class DeleteImageFromS3Handler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(DeleteImageFromS3Handler.class.getName());

	private static DeleteImageFromS3Handler instance = null;

	public static DeleteImageFromS3Handler getInstance() {
		if (instance == null)
			instance = new DeleteImageFromS3Handler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		
		DeleteImageFromS3ReqObj rq = (DeleteImageFromS3ReqObj) req;
		DeleteImageFromS3ResObj rs = new DeleteImageFromS3ResObj();
		
		try {
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if (!oauthcheck.equals(rq.getUserId())) {
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}

			String link = rq.getLink();
			String uid = rq.getUid();

			if (uid == null || uid.isEmpty()) {
				rs.setCode(FLS_INVALID_OPERATION);
				rs.setMessage("Please try refreshing the page!!");
				return rs;
			}
			
			if (link.equals("null") || link.isEmpty() || link == null || link.equals("")){
				rs.setCode(FLS_INVALID_OPERATION);
				rs.setMessage("Not able to delete this image!!");
				return rs;
			}

			FlsS3Bucket s3Bucket = new FlsS3Bucket(uid);
			
			int result = s3Bucket.deleteImage(Bucket_Name.ITEMS_BUCKET, link);
			
			if(result == 1){
				if (rq.isPrimary()){
					s3Bucket.deletePrimaryImageLink();
				}else{
					s3Bucket.deleteNormalImageLink(link);
				}
			}
			
			rs.setCode(FLS_SUCCESS);
			rs.setMessage(FLS_SUCCESS_M);

		} catch (NullPointerException e) {
			LOGGER.warning("Null pointer exception in DeleteImageFromS3Handler");
			e.printStackTrace();
			rs.setCode(FLS_NULL_POINT);
			rs.setMessage(FLS_NULL_POINT_M);
		} catch (Exception e) {
			LOGGER.warning("Exception in DeleteImageFromS3Handler");
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
