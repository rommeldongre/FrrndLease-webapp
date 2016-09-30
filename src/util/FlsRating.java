package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import connect.Connect;

public class FlsRating extends Connect{

	private FlsLogger LOGGER = new FlsLogger(FlsRating.class.getName());
	
	private int itemId;
	
	public FlsRating(int ItemId){
		this.itemId = ItemId;
	}
	
	public int addItemRating(String LeaseeId, int Rating, String Feedback){
		
		LOGGER.info("add Item Rating method of FlsRating class");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		int rs1 = 0;
		
		try{
			String sqlInsertRating = "INSERT INTO items_rating (item_id, leasee_id, item_rating, feedback) VALUES (?,?,?,?)";
			ps1 = hcp.prepareStatement(sqlInsertRating);
			ps1.setInt(1, itemId);
			ps1.setString(2, LeaseeId);
			ps1.setInt(3, Rating);
			ps1.setString(4, Feedback);
			
			rs1 = ps1.executeUpdate();
			
			if(rs1 == 1)
				LOGGER.info("Rating: " + Rating + " added for the item " + itemId);
			else
				LOGGER.warning("Rating not added for the item id " + itemId);
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(ps1 != null) ps1.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return rs1;
		
	}
	
	public int getItemsAvgRating(){
		
		LOGGER.info("Inside getItemsAvgRating");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		int avgRating = 0;
		
		try{
			
			String sqlSelectRatings = "SELECT item_rating FROM items_rating WHERE item_id=?";
			
			ps1 = hcp.prepareStatement(sqlSelectRatings);
			ps1.setInt(1, itemId);
			rs1 = ps1.executeQuery();
			
			int totalRating = 0;
			float totalRaters = 0;
			
			while(rs1.next()){
				totalRating = totalRating + rs1.getInt("item_rating");
				totalRaters++;
			}

			LOGGER.info("Got total ratings for item id " + itemId + " ---- " + totalRating);
			LOGGER.info("Total number of raters for this item id are " + totalRaters);
			
			if(totalRating != 0){
				avgRating = Math.round(totalRating/totalRaters);
			}
			
			LOGGER.info("AvgRating for itemId " + itemId + " : " + avgRating);
			
		}catch(Exception e){
			e.printStackTrace();
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
		
		return avgRating;
	}
	
	public int getItemsRaters(){
		
		LOGGER.info("Inside getItemsRaters");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		int totalRaters = 0;
		
		try{
			
			String sqlRatingsCount = "SELECT COUNT(*) AS raters FROM items_rating WHERE item_id=?";
			
			ps1 = hcp.prepareStatement(sqlRatingsCount);
			ps1.setInt(1, itemId);
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				totalRaters = rs1.getInt("raters");
			}

			LOGGER.info("Total number of raters for this item id " + itemId +  " are " + totalRaters);
			
		}catch(Exception e){
			e.printStackTrace();
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
		
		return totalRaters;
	}
	
}
