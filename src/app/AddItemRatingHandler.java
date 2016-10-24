package app;

import connect.Connect;
import pojos.AddItemRatingReqObj;
import pojos.AddItemRatingResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.FlsRating;
import util.OAuth;

public class AddItemRatingHandler extends Connect implements AppHandler	{

	private FlsLogger LOGGER = new FlsLogger(AddItemRatingHandler.class.getName());
	
	private static AddItemRatingHandler instance = null;
	
	public static AddItemRatingHandler getInstance(){
		if(instance == null)
			instance = new AddItemRatingHandler();
		return instance;
	}
	
	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		
		LOGGER.info("Inside process method of AddItemRatingHandler");
		
		AddItemRatingReqObj rq = (AddItemRatingReqObj) req;
		AddItemRatingResObj rs = new AddItemRatingResObj();
		
		try{
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if (!oauthcheck.equals(rq.getUserId())) {
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}
			
			FlsRating rating = new FlsRating(rq.getItemId());
			int response = rating.addItemRating(rq.getLeaseeId(), rq.getRating(), rq.getFeedback());
			
			if(response == 1){
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_ADD_ITEM_RATING);
			}else{
				rs.setCode(FLS_INVALID_OPERATION);
				rs.setMessage(FLS_INVALID_OPERATION_M);
			}
			
		}catch(Exception e){
			e.printStackTrace();
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
		}
		
		LOGGER.info("Completion of AddItemRatingHandler");
		return rs;
	}

	@Override
	public void cleanup() {
	}

}
