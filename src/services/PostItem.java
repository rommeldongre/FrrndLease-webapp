package services;

import errorCat.ErrorCat;
import util.FlsLogger;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import adminOps.Response;
import adminOps.AdminOpsHandler;

/**
 * Servlet implementation class PostItem
 */
@WebServlet("/PostItem")
public class PostItem extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(PostItem.class.getName());

	private static final long serialVersionUID = 1L;
	private AdminOpsHandler aoh1 = new AdminOpsHandler();
	private AdminOpsHandler aoh2 = new AdminOpsHandler();
	private Response res1 = new Response();
	private Response res2 = new Response();
	private ErrorCat e = new ErrorCat();

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		/*
		 * response.setContentType("application/json"); /*System.out.println(
		 * "Inside GET Method");
		 * 
		 * doPost(request,response);
		 */
		LOGGER.info("Inside POST Method");
		String table;
		String Id = "0", Message = e.FLS_POST_ITEM_F_M, Code = String.valueOf(e.FLS_POST_ITEM_F);
		PrintWriter out = response.getWriter();

		String str = request.getParameter("req");
		
		LOGGER.info(str);
		// String str2 = request.getParameter("req2");
		
		try {
			JSONObject row = new JSONObject(str);
			JSONObject obj1 = new JSONObject();
			table = "items";
			obj1.put("table", table);
			obj1.put("operation", "add");
			obj1.put("row", row);

			/*
			 * JSONObject obj2 = new JSONObject(str2); table2 =
			 * obj2.getString("table"); System.out.println(table2);
			 */

			res1 = aoh1.getInfo(table, obj1);
			LOGGER.info(res1.getCode());
			LOGGER.info(res1.getId());
			if (res1.getIntCode() == e.FLS_SUCCESS) {
				LOGGER.warning("Item added to items table..");
				JSONObject obj2 = new JSONObject();
				row.put("itemId", Integer.parseInt(res1.getId()));
				table = "store";
				obj2.put("table", table);
				obj2.put("operation", "add");
				obj2.put("row", row);
				res2 = aoh2.getInfo(table, obj2);

				if (res2.getIntCode() == e.FLS_SUCCESS) {
					Id = res2.getId();
					Message = e.FLS_POST_ITEM;
					Code = "FLS_SUCCESS";
				}
			} else if (res1.getIntCode() == e.FLS_SQL_EXCEPTION_I) {
				LOGGER.warning("The error code in case of large image is " + res1.getIntCode());
				Message = res1.getMessage();
			} else {
				LOGGER.warning("Couldn't perform postItem");
			}

			JSONObject json = new JSONObject();
			json.put("Code", Code);
			json.put("Message", Message);
			json.put("Id", Id);
			out.print(json);

		} catch (JSONException e) {
			LOGGER.warning("Couldn't parse/retrieve JSON");
			e.printStackTrace();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("application/json");
		LOGGER.info("Inside GET Method");

		doGet(request, response);

		/*
		 * System.out.println("Inside POST Method"); String table; String
		 * Id="0", Message="PostItem couldn't be performed..", Code="210";
		 * PrintWriter out = response.getWriter();
		 * 
		 * String str = request.getParameter("req"); //String str2 =
		 * request.getParameter("req2");
		 * 
		 * try { JSONObject row = new JSONObject(str); JSONObject obj1 = new
		 * JSONObject(); table = "items"; obj1.put("table", table);
		 * obj1.put("operation", "add"); obj1.put("row", row);
		 * 
		 * //JSONObject obj2 = new JSONObject(str2); //table2 =
		 * obj2.getString("table"); //System.out.println(table2);
		 * 
		 * res1 = aoh1.getInfo(table, obj1); System.out.println(res1.getCode());
		 * System.out.println(res1.getId()); if(res1.getIntCode() == 0){
		 * System.out.println("Item added to items table.."); JSONObject obj2 =
		 * new JSONObject(); row.put("itemId", Integer.parseInt(res1.getId()));
		 * table = "store"; obj2.put("table", table); obj2.put("operation",
		 * "add"); obj2.put("row", row); res2 = aoh2.getInfo(table, obj2);
		 * 
		 * if(res2.getIntCode() == 29) { Id = res2.getId(); Message =
		 * "PostItem Performed successfully.."; Code = "FLS_SUCCESS"; } }
		 * 
		 * else{ System.out.println("Couldn't perform postItem"); }
		 * 
		 * JSONObject json = new JSONObject(); json.put("Code", Code);
		 * json.put("Message", Message); json.put("Id", Id); out.print(json);
		 * 
		 * 
		 * } catch (JSONException e) { System.out.println(
		 * "Couldn't parse/retrieve JSON"); e.printStackTrace(); }
		 */
	}

}
