package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import connect.Connect;
import pojos.GetItemRatingReqObj;
import pojos.GetItemRatingResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetItemRatingHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetItemRatingHandler.class.getName());

	private static GetItemRatingHandler instance = null;

	public static GetItemRatingHandler getInstance() {
		if (instance == null)
			instance = new GetItemRatingHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside Process method of GetItemRatingHandler");
		
		GetItemRatingReqObj rq = (GetItemRatingReqObj) req;
		GetItemRatingResObj rs = new GetItemRatingResObj();

		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		try{
			
			String sqlSelectRatings = "SELECT item_rating FROM items_rating WHERE item_id=?";
			
			if(rq.getFromDate() != null)
				sqlSelectRatings = sqlSelectRatings + " AND datetime > " + rq.getFromDate();
			
			ps1 = hcp.prepareStatement(sqlSelectRatings);
			ps1.setInt(1, rq.getItemId());
			rs1 = ps1.executeQuery();
			
			int totalRating = 0;
			int totalRaters = 0;
			
			while(rs1.next()){
				totalRating = totalRating + rs1.getInt("item_rating");
				totalRaters++;
			}

			LOGGER.info("Got total ratings for item id " + rq.getItemId() + " ---- " + totalRating);
			LOGGER.info("Total number of raters for this item id are " + totalRaters);
			
			rs.setCode(FLS_SUCCESS);
			rs.setMessage(FLS_SUCCESS_M);
			rs.setTotalRating(totalRating);
			rs.setTotalRaters(totalRaters);
			
		}catch(Exception e){
			e.printStackTrace();
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			LOGGER.warning(FLS_SQL_EXCEPTION_M);
		}finally{
			try{
				if(rs1 != null) rs1.close();
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
