package app;

import java.sql.Connection;
import java.sql.PreparedStatement;

import connect.Connect;
import pojos.AddItemRatingReqObj;
import pojos.AddItemRatingResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
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
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int rs1;
		
		try{
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if (!oauthcheck.equals(rq.getUserId())) {
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}
			
			int itemId = rq.getItemId();
			String leaseeId = rq.getLeaseeId();
			int rating = rq.getRating();
			String feedback = rq.getFeedback();
			
			if(leaseeId.isEmpty())
				leaseeId = null;
			
			if(feedback.isEmpty())
				feedback = null;
			
			String sqlInsertRating = "INSERT INTO items_rating (item_id, leasee_id, item_rating, feedback) VALUES (?,?,?,?)";
			ps1 = hcp.prepareStatement(sqlInsertRating);
			ps1.setInt(1, itemId);
			ps1.setString(2, leaseeId);
			ps1.setInt(3, rating);
			ps1.setString(4, feedback);
			
			rs1 = ps1.executeUpdate();
			
			if(rs1 == 1){
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
			}else{
				rs.setCode(FLS_INVALID_OPERATION);
				rs.setMessage(FLS_INVALID_OPERATION_M);
			}
			
		}catch(Exception e){
			e.printStackTrace();
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
		}finally{
			try{
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return rs;
	}

	@Override
	public void cleanup() {
	}

}
