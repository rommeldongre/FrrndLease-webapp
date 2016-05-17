package app;

import java.sql.Connection;
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
import util.FlsLogger;
import util.BufferImage;

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

	private FlsLogger LOGGER = new FlsLogger(ImportWishlistHandler.class.getName());

	private String user_name, check = null, Id = null, token, message;
	private int Code;
	private int newItemCount = 0, execution_count=0;
	Response res = new Response();
	WishlistModel wm = new WishlistModel();
	ItemsModel im = new ItemsModel();
	BufferImage BI = new BufferImage();

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
		

		LOGGER.info("Inside process method " + rq.getUrl());
		// TODO: Core of the processing takes place here
		check = null;
		LOGGER.info("Inside ImportWishlist method");
		newItemCount = 0;
		int wishlist_pages=0;
		Document doc1 = Jsoup.connect(rq.getUrl()).timeout(10 * 1000).post();
		if(doc1.select("li[class*=a-last]").size() > 0){
				Element pages = doc1.select("li[data-action=pag-trigger]").last();
				wishlist_pages = Integer.parseInt(pages.text());
				System.out.println("Value of List Item: "+wishlist_pages);
			if(wishlist_pages > 9){
				wishlist_pages= 9;
				System.out.println("Value of List Item: "+wishlist_pages);
			}
			}else{
				wishlist_pages= 1;
				System.out.println("Value of List Item: "+wishlist_pages);
			}
		
		String url[] = rq.getUrl().split("/");
		String part1= "/ref=cm_wl_sortbar_v_page_";
		String part2 = "?ie=UTF8&page=";
		Integer num =1;
		System.out.println("http://"+url[2]+"/gp/registry/wishlist/"+url[6]+part1+num+part2+num);
		//rs.setTotalWishItemCount(10);
		//rs.setWishItemCount(10);
		for (int page= 1;page<=wishlist_pages; page++){
			String nthUrl = "http://"+url[2]+"/gp/registry/wishlist/"+url[6]+part1+page+part2+page;
			//ImportnthPage(nthUrl);
			
			Document doc = Jsoup.connect(nthUrl).timeout(10 * 1000).post();

			Elements links = doc.select("a[id*=itemName]");
			LOGGER.info("Total number of elements: " + links.size());
			LOGGER.info("Value before for loop: " + newItemCount);
			
			Elements imglink = doc.select("div[id*=itemImage] > a > img[src]");
			
			if (page ==1){
			Element links2 = doc.select("div[class*=selected] > a > span > span").first();
			LOGGER.info("selected Wishlist count: " + links2.text());
			Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(links2.text());
			while (m.find()) {
				LOGGER.info(m.group(1));
				int foo = Integer.parseInt(m.group(1));
				rs.setTotalWishItemCount(foo);
			}
			}
			
			for (int i = 0; i < links.size(); i++) {
				LOGGER.info("\ntext : " + links.get(i).text());
				String imgSrc= null, final_img = null;
				imgSrc = BI.URLtoImage(imglink.get(i).attr("src"));
				final_img = "data:image/png;base64,"+imgSrc;
				
				// Populate the response
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
					obj1.put("image", final_img);

					im.getData(obj1);
				} catch (JSONException e) {
					LOGGER.warning("Couldn't parse/retrieve JSON for FLS_MAIL_MAKE_REQUEST_TO");
					e.printStackTrace();
				}
				LOGGER.info("Calling Amazon Wishlist Function ");
				AmazonWishlist(links.get(i).text(), rq.getUserId());
			}
			LOGGER.info("Value after for loop: " + newItemCount);
		}
		
		// rs.setWishItemCount(rq.getUrl().length());
		rs.setWishItemCount(newItemCount);
		message = rs.getWishItemCount().toString();
		LOGGER.info("Printing out Resultset: " + message);
		Code = FLS_SUCCESS;
		Id = check;
	
		System.out.println("Total times executed: "+execution_count);
		LOGGER.info("Finished process method ");
		// return the response
		return rs;

	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
	}
	
	private void ImportnthPage(String nthURL){
		
		
	} 

	private void AmazonWishlist(String URL, String name) {

		String Iname, User, Image;
		Integer insertcount = 0;
		Iname = URL;
		User = name;
		Image = im.getImage();
		Connection hcp = getConnectionFromPool();

		try {
			String sql = "SELECT * FROM items WHERE item_name=? AND item_user_id=? AND item_status=? LIMIT 1";
			LOGGER.info("Creating a statement .....");
			PreparedStatement stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing ImportWishlistHandler select query...");
			stmt.setString(1, Iname);
			stmt.setString(2, User);
			stmt.setString(3, "Wished");

			ResultSet dbResponse = stmt.executeQuery();
			
			LOGGER.info("ImportWishlistHandler select query executed...");

			execution_count = execution_count + 1;
			if (dbResponse.next() == false) {
				LOGGER.info("Item: " + Iname + "for user: " + User + " does not exist");
				String sql1 = "insert into items (item_name, item_category, item_desc, item_user_id, item_lease_value, item_lease_term, item_status, item_image) values (?,?,?,?,?,?,?,?)";
				LOGGER.info("Creating Insert statement of ImportWishlistHandler.....");
				PreparedStatement stmt1 = hcp.prepareStatement(sql1);

				LOGGER.info("Statement created. Executing query.....");
				stmt1.setString(1, Iname);
				stmt1.setString(2, "");
				stmt1.setString(3, "");
				stmt1.setString(4, User);
				stmt1.setInt(5, 0);
				stmt1.setString(6, "");
				stmt1.setString(7, "Wished");
				stmt1.setString(8, Image);
				insertcount = stmt1.executeUpdate();
				stmt1.close();
				
				// System.out.println("Entry added into items table:
				// "+insertcount);
				
				// to add credit in user_credit
				String sqlAddCredit = "UPDATE users SET user_credit=user_credit+1 WHERE user_id=?";
				PreparedStatement s1 = hcp.prepareStatement(sqlAddCredit);
				s1.setString(1, User);
				s1.executeUpdate();

				// returning the new id
				String sql2 = "SELECT MAX(item_id) FROM items";
				PreparedStatement stmt2 = hcp.prepareStatement(sql2);
				ResultSet rs = stmt2.executeQuery();
				
				while (rs.next()) {
					try {
						JSONObject obj2 = new JSONObject();
						obj2.put("itemId", rs.getInt(1));
						wm.getData(obj2);

					} catch (JSONException e) {
						LOGGER.warning("Couldn't parse/retrieve JSON for FLS_MAIL_MAKE_REQUEST_TO");
						e.printStackTrace();
					}
				}
				stmt2.close();
			} else {
				// System.out.println("Amazon Wishlist Item Already exists");
				LOGGER.info("Amazon Wishlist Item Already exists");
				// System.out.println("Item: "+Iname+"for user: "+User+"
				// exists");
				LOGGER.info("Item: " + Iname + "for user: " + User + " exists");
				// Id = "0";
				// message = FLS_END_OF_DB_M;
				// Code = FLS_END_OF_DB;
				// rs.setErrorString("End of table reached");
			}
			
			// res.setData(Code,Id,message);
		    	if (insertcount == 1) {

				Integer itemId = wm.getItemId();

				String sql3 = "insert into wishlist (wishlist_item_id) values (?)"; //

				try {
					// System.out.println("Creating statement.....");
					LOGGER.info("Creating statement.....");
					PreparedStatement stmt3 = hcp.prepareStatement(sql3);

					// System.out.println("Statement created. Executing
					// query.....");
					LOGGER.info("Statement created. Executing query.....");
					stmt3.setInt(1, itemId);
					stmt3.executeUpdate();
					stmt3.close();
					// System.out.println("Entry added into wishlist table:
					// "+insertcount);
					LOGGER.info("Entry added into wishlist table: " + insertcount);
					newItemCount = newItemCount + 1;
					// message = "Entry added into wishlist table";
					// Code = 33;
					// Id = String.valueOf(itemId);
					// res.setData(FLS_SUCCESS,Id,FLS_SUCCESS_M);
				} catch (SQLException e) {
					LOGGER.warning("Couldn't create statement");
					res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
					e.printStackTrace();
				}
			} else {
				// System.out.println("Wishlist op not performed as Add item not
				// performed");
				LOGGER.info("Wishlist op not performed as Add item not performed");
			}
		    	
		    	stmt.close();
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		} finally {
			try { hcp.close(); } catch (SQLException e) {} 
		}
	}
}
