package util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.PostItemReqObj;

public class MatchItems extends Connect{

	private FlsLogger LOGGER = new FlsLogger(MatchItems.class.getName());
	
	PostItemReqObj itemObj;
	String userItemTitle[], itemUserId;
	
	public MatchItems(PostItemReqObj rq){
		this.itemObj = (PostItemReqObj) rq;
		this.userItemTitle = rq.getTitle().split(" ");
		this.itemUserId = rq.getUserId();
		getConnection();
	}
	
	public void checkWishlist(){

		String sqlGetWishlistNames = "SELECT items.item_name,items.item_user_id FROM items INNER JOIN wishlist ON items.item_id=wishlist.wishlist_item_id  AND items.item_user_id<>'"+itemUserId+"'";
		
		try{
			
			PreparedStatement ps1 = connection.prepareStatement(sqlGetWishlistNames);
			LOGGER.info("Creating getwishlistnames sql statement");
			ResultSet rs1 = ps1.executeQuery();
			
			while(rs1.next()){
				String wlItemTitle[] = rs1.getString("item_name").split(" ");
				if(compareTitles(userItemTitle, wlItemTitle)){
					try {
						AwsSESEmail newE = new AwsSESEmail();
						newE.send(rs1.getString("item_user_id"), FlsSendMail.Fls_Enum.FLS_MAIL_MATCH_POST_WISHLIST_ITEM, itemObj);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
		}catch(SQLException e){
			e.printStackTrace();
		}
		
	}
	
	private boolean compareTitles(String title1[], String title2[]){
		return true;
	}
	
}
