package app;

//import com.mysql.jdbc.PreparedStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.*;

import connect.Connect;
import pojos.ImportWishlistReqObj;
import pojos.ImportWishlistResObj;
import pojos.ItemsModel;
import pojos.WishlistModel;
import pojos.ReqObj;
import pojos.ResObj;
import adminOps.Response;
import tableOps.Items;
import tableOps.Wishlist;

import org.json.JSONException;
import org.json.JSONObject;
import errorCat.ErrorCat;

import java.io.IOException;  
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class ImportWishlistHandler extends Connect implements AppHandler {
	
	private String user_name, check=null,Id=null,token, message;
	private int Code;
	private int newItemCount=0;
	Response res = new Response();
	WishlistModel wm = new WishlistModel();

	private static ImportWishlistHandler instance = null;

	public static ImportWishlistHandler getInstance() {
		if (instance == null)
			instance = new ImportWishlistHandler();
		return instance;
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		ImportWishlistReqObj rq = (ImportWishlistReqObj) req;
		ImportWishlistResObj rs = new ImportWishlistResObj();
		ItemsModel im = new ItemsModel();
		//Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		//System.out.println("Inside process method "+ rq.getUrl());
		LOGGER.fine("Inside process method "+ rq.getUrl());
		//TODO: Core of the processing takes place here
		check = null;
		//System.out.println("Inside ImportWishlist method");
		LOGGER.fine("Inside ImportWishlist method");
		newItemCount=0;
         Document doc = Jsoup.connect(rq.getUrl()).timeout(10*1000).post();

         Elements links = doc.select("a[id*=itemName]");
         //System.out.println("Total number of elements: " + links.size()); 
         LOGGER.fine("Total number of elements: " + links.size());
         //System.out.println("Value before for loop: "+newItemCount);
         LOGGER.fine("Value before for loop: "+newItemCount);
         
         Element links2 = doc.select("div[class*=selected] > a > span > span").first();
        //System.out.println("selected Wishlist count: " +links2.text());
         LOGGER.fine("selected Wishlist count: " +links2.text());
         Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(links2.text());
         while(m.find()) {
           //System.out.println(m.group(1));
           LOGGER.fine(m.group(1));
           int foo = Integer.parseInt(m.group(1));
           rs.setTotalWishItemCount(foo);
         }
         
         for (int i = 0;i<links.size();i++) {  
             //System.out.println("\ntext : " + links.get(i).text()); 
             LOGGER.fine("\ntext : " + links.get(i).text());
           //Populate the response
				try {
					JSONObject obj1 = new JSONObject();
					obj1.put("title", links.get(i).text());
					obj1.put("description", "");
					obj1.put("category", "");
					obj1.put("userId", rq.getUserId());
					obj1.put("leaseTerm", "");
					obj1.put("id", 0);
					obj1.put("leaseValue", 0);
					obj1.put("status", "Wished");
					obj1.put("image", " ");
					
					im.getData(obj1);
			}catch (JSONException e) {
				System.out.println("Couldn't parse/retrieve JSON for FLS_MAIL_MAKE_REQUEST_TO");
				e.printStackTrace();
			}
				 //System.out.println("Calling Amazon Wishlist Function "); 
				 LOGGER.fine("Calling Amazon Wishlist Function ");
				AmazonWishlist(links.get(i).text(),rq.getUserId());
         }
        // System.out.println("Value after for loop: "+newItemCount);
         LOGGER.fine("Value after for loop: "+newItemCount);
         
       //rs.setWishItemCount(rq.getUrl().length());
        rs.setWishItemCount(newItemCount);
		message = rs.getWishItemCount().toString();
		//System.out.println("Printing out Resultset: "+message);
		LOGGER.fine("Printing out Resultset: "+message);
		Code = FLS_SUCCESS;
		Id = check;
		//System.out.println("Finished process method ");
		LOGGER.fine("Finished process method ");
		//return the response
		return rs;
		
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
	}
	
	private void AmazonWishlist(String URL, String name){
		
		String Iname, User;
		Integer insertcount=0;
		Iname = URL;
		User = name;
		
		 
		try {
			getConnection();
			String sql = "SELECT * FROM items WHERE item_name=? AND item_user_id=? AND item_status=? LIMIT 1";
			//System.out.println("Creating a statement .....");
			LOGGER.fine("Creating a statement .....");
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			//System.out.println("Statement created. Executing ImportWishlistHandler select query...");
			LOGGER.fine("Statement created. Executing ImportWishlistHandler select query...");
			stmt.setString(1, Iname);
			stmt.setString(2, User);
			stmt.setString(3, "Wished");
			
			ResultSet dbResponse = stmt.executeQuery();
			//System.out.println("ImportWishlistHandler select query executed...");
			LOGGER.fine("ImportWishlistHandler select query executed...");
			
			if(dbResponse.next() == false){
					//System.out.println("Item: "+Iname+"for user: "+User+" does not exist");
					LOGGER.fine("Item: "+Iname+"for user: "+User+" does not exist");
					String sql1 = "insert into items (item_name, item_category, item_desc, item_user_id, item_lease_value, item_lease_term, item_status, item_image) values (?,?,?,?,?,?,?,?)";
					//System.out.println("Creating Insert statement of ImportWishlistHandler.....");
					LOGGER.fine("Creating Insert statement of ImportWishlistHandler.....");
					PreparedStatement stmt1 = connection.prepareStatement(sql1);
					
					//System.out.println("Statement created. Executing query.....");
					LOGGER.fine("Statement created. Executing query.....");
					stmt1.setString(1, Iname);
					stmt1.setString(2, "");
					stmt1.setString(3, "");
					stmt1.setString(4, User);
					stmt1.setInt(5, 0);
					stmt1.setString(6, "");
					stmt1.setString(7, "Wished");
					stmt1.setString(8, "");
					insertcount = stmt1.executeUpdate();
					//System.out.println("Entry added into items table: "+insertcount);
					
					
					//returning the new id
					String sql2 = "SELECT MAX(item_id) FROM items";
					PreparedStatement stmt2 = connection.prepareStatement(sql2);
					ResultSet rs = stmt2.executeQuery();
					while(rs.next()) {
						try {
							JSONObject obj2 = new JSONObject();
							obj2.put("itemId", rs.getInt(1));
							wm.getData(obj2);
							
						}catch (JSONException e) {
							System.out.println("Couldn't parse/retrieve JSON for FLS_MAIL_MAKE_REQUEST_TO");
							e.printStackTrace();
						}
					}
					
				}else {
					//System.out.println("Amazon Wishlist Item Already exists");
					LOGGER.fine("Amazon Wishlist Item Already exists");
					//System.out.println("Item: "+Iname+"for user: "+User+" exists");
					LOGGER.fine("Item: "+Iname+"for user: "+User+" exists");
					//Id = "0";
					//message = FLS_END_OF_DB_M;
					//Code = FLS_END_OF_DB;
					//rs.setErrorString("End of table reached");
				}
			
			//res.setData(Code,Id,message);
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			System.out.println("Error Check Stacktrace");
			e.printStackTrace();
		}

		if(insertcount== 1){
			
			Integer itemId = wm.getItemId();
			
			String sql3 = "insert into wishlist (wishlist_item_id) values (?)";		 //
			
			
			try {
				getConnection();
				//System.out.println("Creating statement.....");
				LOGGER.fine("Creating statement.....");
				PreparedStatement stmt3 = connection.prepareStatement(sql3);
				
				//System.out.println("Statement created. Executing query.....");
				LOGGER.fine("Statement created. Executing query.....");
				stmt3.setInt(1, itemId);
				stmt3.executeUpdate();
				//System.out.println("Entry added into wishlist table: "+insertcount);
				LOGGER.fine("Entry added into wishlist table: "+insertcount);
				newItemCount = newItemCount+ 1;
				//message = "Entry added into wishlist table";
				//Code = 33;
				//Id = String.valueOf(itemId);
				//res.setData(FLS_SUCCESS,Id,FLS_SUCCESS_M);
			} catch (SQLException e) {
				System.out.println("Couldn't create statement");
				res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
				e.printStackTrace();
			}
		}else{
			//System.out.println("Wishlist op not performed as Add item not performed");
			LOGGER.fine("Wishlist op not performed as Add item not performed");
		}
		
	}
}
