package app;

//import com.mysql.jdbc.PreparedStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONException;
import org.json.JSONObject;

import com.mysql.jdbc.MysqlErrorNumbers;

import connect.Connect;
import pojos.ItemsModel;
import pojos.PostItemReqObj;
import pojos.PostItemResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.AwsSESEmail;
import util.FlsLogger;
import util.FlsSendMail;
import adminOps.Response;

public class PostItemHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(PostItemHandler.class.getName());

	private String user_name,status, check = null, Id = null, token, message;
	static String userId;
	private int Code;
	private Response res = new Response();
	ItemsModel im = new ItemsModel();

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
		check = null;
		LOGGER.info("Inside process method of Post Item");
		
		// Populate the input
					try {
						JSONObject obj1 = new JSONObject();
						obj1.put("title", rq.getTitle());
						obj1.put("description", rq.getDescription());
						obj1.put("category", rq.getCategory());
						obj1.put("userId", rq.getUserId());
						obj1.put("leaseTerm", rq.getLeaseTerm());
						obj1.put("id", rq.getId());
						obj1.put("leaseValue", rq.getLeaseValue());
						obj1.put("status", rq.getStatus());
						obj1.put("image", rq.getImage());

						im.getData(obj1);
					} catch (JSONException e) {
						LOGGER.warning("Couldn't parse/retrieve JSON for FLS_MAIL_MAKE_REQUEST_TO");
						e.printStackTrace();
					}

		/*try {
			getConnection();
			String sql = "SELECT tb1.request_date, tb1.request_item_id, tb1.request_id, tb1.request_status, tb2.item_name, tb2.item_desc, tb2.item_user_id, tb3.user_full_name FROM requests tb1 INNER JOIN items tb2 on tb1.request_item_id = tb2.item_id INNER JOIN users tb3 on tb2.item_user_id = tb3.user_id WHERE tb1.request_requser_id=? AND tb1.request_id>? HAVING tb1.request_status=? ORDER by tb1.request_id ASC LIMIT 1";
			LOGGER.info("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);

			LOGGER.info("Statement created. Executing GetOutgoingrequests query...");
			stmt.setString(1, rq.getUserId());
			stmt.setInt(2, rq.getCookie());
			stmt.setString(3, "Active");

			ResultSet dbResponse = stmt.executeQuery();

			if (dbResponse.next()) {
				check = dbResponse.getString("request_item_id");

				if (check != null) {
					// Populate the response
					rs.setTitle(dbResponse.getString("item_name"));
					rs.setDesc(dbResponse.getString("item_desc"));
					rs.setOwner_Id(dbResponse.getString("item_user_id"));
					rs.setRequest_status(dbResponse.getString("request_status"));
					rs.setRequest_id(dbResponse.getInt("request_id"));
					rs.setRequest_item_id(dbResponse.getInt("request_item_id"));
					rs.setRequest_date(dbResponse.getString("request_date"));
					rs.setOwner_name(dbResponse.getString("user_full_name"));

					message = rs.getTitle() + ", " + rs.getDesc() + ", " + rs.getOwner_Id() + ", "
							+ rs.getRequest_status() + ", " + rs.getRequest_item_id() + ", " + rs.getRequest_date();
					LOGGER.info("Printing out Resultset: " + message);
					Code = FLS_SUCCESS;
					Id = check;
				} else {
					Id = "0";
					message = FLS_END_OF_DB_M;
					Code = FLS_END_OF_DB;
					rs.setErrorString("End of table reached");
				}
			}

			// res.setData(Code,Id,message);
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		}*/
		String desciption = null,uid=null;
		if(im.getDescription() == null){
			desciption = "";
		}
		int id=0,uidAction=0,storeAction=0;
		userId = im.getUserId();
		String sql = "insert into items (item_name, item_category, item_desc, item_user_id, item_lease_value, item_lease_term, item_status, item_image) values (?,?,?,?,?,?,?,?)";

		getConnection();
		try {
			LOGGER.info("Creating statement.....");
			PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			LOGGER.info("Statement created. Executing query.....");
			stmt.setString(1, im.getTitle());
			stmt.setString(2, im.getCategory());
			stmt.setString(3, desciption);
			stmt.setString(4, im.getUserId());
			stmt.setInt(5, im.getLeaseValue());
			stmt.setString(6, im.getLeaseTerm());
			stmt.setString(7, im.getStatus());
			stmt.setString(8, im.getImage());
			stmt.executeUpdate();
			
			// getting the last item inserted id and appending it with the title to generate a uid
			ResultSet keys = stmt.getGeneratedKeys();
			keys.next();
			int itemId = keys.getInt(1);
			
			uid = rq.getTitle()+ " " + itemId;
			uid = uid.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();
			
			// updating the item_uid value of the last item inserted
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
			status = "operation successfull!!!";
			message = "Item added into table";
			LOGGER.warning(message);

			Code = 000;

			String status_W = im.getStatus(); // To be used to check if Request
												// is from WishItem API.
			if (!FLS_WISHLIST_ADD.equals(status_W)) {
				try {
					AwsSESEmail newE = new AwsSESEmail();
					newE.send(userId, FlsSendMail.Fls_Enum.FLS_MAIL_POST_ITEM, im);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// returning the new id
			sql = "SELECT MAX(item_id) FROM items";
			Statement stmt1 = connection.createStatement();
			ResultSet resultset = stmt1.executeQuery(sql);
			while (resultset.next()) {
				id = resultset.getInt(1);
			}
			Id = String.valueOf(id);
			res.setData(FLS_SUCCESS, Id, FLS_ITEMS_ADD);

		} catch (SQLException e) {
			LOGGER.warning("Couldnt create a statement");
			if (e.getErrorCode() == MysqlErrorNumbers.ER_DATA_TOO_LONG
					&& e.getMessage().matches(".*\\bitem_image\\b.*")) {
				LOGGER.warning("The image size is too large. Please select image less than 16MB");
				res.setData(FLS_SQL_EXCEPTION_I, String.valueOf(FLS_SQL_EXCEPTION_I), FLS_SQL_EXCEPTION_IMAGE);
				LOGGER.warning(e.getErrorCode() + " " + e.getMessage());
			} else {
				res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
				e.printStackTrace();
			}
		}
		rs.setItemId(id);
		rs.setReturnCode(0);
		rs.setUid(uid);
		LOGGER.info("Finished process method ");
		// return the response
		return rs;

	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
	}
}
