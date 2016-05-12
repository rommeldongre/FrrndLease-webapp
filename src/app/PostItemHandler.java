package app;

//import com.mysql.jdbc.PreparedStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.MysqlErrorNumbers;

import connect.Connect;
import pojos.PostItemReqObj;
import pojos.PostItemResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.AwsSESEmail;
import util.FlsLogger;
import util.FlsSendMail;
import util.MatchItems;

public class PostItemHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(PostItemHandler.class.getName());

	private static PostItemHandler instance = null;

	public static PostItemHandler getInstance() {
		if (instance == null)
			instance = new PostItemHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		
		// TODO Auto-generated method stub
		PostItemReqObj rq = (PostItemReqObj) req;
		PostItemResObj rs = new PostItemResObj();
		LOGGER.info("Inside process method " + rq.getUserId() + ", " + rq.getId());
		
		// TODO: Core of the processing takes place here

		LOGGER.info("Inside process method of Post Item");
		
		final String userId;
		String desciption = null;
		if(rq.getDescription() == null){
			desciption = "";
		}
		userId = rq.getUserId();
		String sql = "insert into items (item_name, item_category, item_desc, item_user_id, item_lease_value, item_lease_term, item_status, item_image) values (?,?,?,?,?,?,?,?)";

		getConnection();
		try {
			LOGGER.info("Creating statement.....");
			PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			LOGGER.info("Statement created. Executing query.....");
			stmt.setString(1, rq.getTitle());
			stmt.setString(2, rq.getCategory());
			stmt.setString(3, desciption);
			stmt.setString(4, rq.getUserId());
			stmt.setInt(5, rq.getLeaseValue());
			stmt.setString(6, rq.getLeaseTerm());
			stmt.setString(7, rq.getStatus());
			stmt.setString(8, rq.getImage());
			stmt.executeUpdate();
			
			// getting the last item inserted id and appending it with the title to generate a uid
			ResultSet keys = stmt.getGeneratedKeys();
			keys.next();
			int itemId = keys.getInt(1);
			
			String uid= null;
			uid = rq.getTitle()+ " " + itemId;
			uid = uid.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();
			
			// updating the item_uid value of the last item inserted
			int uidAction=0,storeAction=0;
			String sqlUpdateUID = "UPDATE items SET item_uid=? WHERE item_id=?";
			PreparedStatement s = connection.prepareStatement(sqlUpdateUID);
			s.setString(1, uid);
			s.setInt(2, itemId);
			uidAction = s.executeUpdate();
			if(uidAction> 0){
				String sqlInsertStoreID = "insert into store (store_item_id) values (?)";
				PreparedStatement storeID = connection.prepareStatement(sqlInsertStoreID);
				storeID.setInt(1, itemId);
				storeAction =storeID.executeUpdate();
				if(storeAction> 0){
					LOGGER.info("Value in store table after UID query excecution: "+uidAction+" "+itemId);	
				}
			}
			String message;
			message = "Item added into table";
			LOGGER.warning(message);
			
			// checking the wish list if this posted item matches someone's requirements
			MatchItems matchItems = new MatchItems(rq);
			matchItems.checkWishlist();

			String status_W = rq.getStatus(); // To be used to check if Request
												// is from WishItem API.
			if (!FLS_WISHLIST_ADD.equals(status_W)) {
				try {
					AwsSESEmail newE = new AwsSESEmail();
					newE.send(userId, FlsSendMail.Fls_Enum.FLS_MAIL_POST_ITEM, rq);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// returning the new id
			int id=0;
			sql = "SELECT MAX(item_id) FROM items";
			Statement stmt1 = connection.createStatement();
			ResultSet resultset = stmt1.executeQuery(sql);
			while (resultset.next()) {
				id = resultset.getInt(1);
			}
			
			rs.setItemId(id);
			rs.setReturnCode(0);
			rs.setUid(uid);
			rs.setErrorString(message);
			
		} catch (SQLException e) {
			LOGGER.warning("Couldnt create a statement");
			if (e.getErrorCode() == MysqlErrorNumbers.ER_DATA_TOO_LONG
					&& e.getMessage().matches(".*\\bitem_image\\b.*")) {
				LOGGER.warning("The image size is too large. Please select image less than 16MB");
				rs.setItemId(FLS_SQL_EXCEPTION_I);
				rs.setReturnCode(FLS_SQL_EXCEPTION_I);
				rs.setUid("Error");
				rs.setErrorString(FLS_SQL_EXCEPTION_IMAGE);
				LOGGER.warning(e.getErrorCode() + " " + e.getMessage());
			} else {
				rs.setItemId(0);
				rs.setReturnCode(FLS_SQL_EXCEPTION);
				rs.setUid("Error");
				rs.setErrorString(FLS_SQL_EXCEPTION_M);
				e.printStackTrace();
			}
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
