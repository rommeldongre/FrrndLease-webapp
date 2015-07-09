package services;

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
 * Servlet implementation class WishItem
 */
@WebServlet("/WishItem")
public class WishItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private AdminOpsHandler aoh1 = new AdminOpsHandler();
	private AdminOpsHandler aoh2 = new AdminOpsHandler();
	private Response res1 = new Response();
	private Response res2 = new Response();
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		System.out.println("Inside GET Method");
		
		doPost(request,response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Inside POST Method");
		String table;
		String Id="0", Message="WishItem couldn't be performed..", Code="211";
		PrintWriter out = response.getWriter();
		
		String str = request.getParameter("req");
		
		try {
			JSONObject row = new JSONObject(str);
			JSONObject obj1 = new JSONObject();
			table = "items";
			obj1.put("table", table);
			obj1.put("operation", "add");
			obj1.put("row", row);
			
			res1 = aoh1.getInfo(table, obj1);
			System.out.println(res1.getCode());
			if(res1.getIntCode() == 0){
				System.out.println("Item added to items table..");
				JSONObject obj2 = new JSONObject();
				row.put("itemId", Integer.parseInt(res1.getId()));
				table = "wishlist";
				obj2.put("table", table);
				obj2.put("operation", "add");
				obj2.put("row", row);
				res2 = aoh2.getInfo(table, obj2);
				
				if(res2.getIntCode() == 33) { 
					System.out.println("Item added to store...");
					Id = res2.getId();
					Message = "WishItem Performed successfully..";
					Code = "FLS_SUCCESS";
				}
				else{
					System.out.println("Couldn't perform WishItem");
				}
			}
			
			else{
				System.out.println("Couldn't perform WishItem");
			}
			
			JSONObject json = new JSONObject();
			json.put("Code", Code);
			json.put("Message", Message);
			json.put("Id", Id);
			out.print(json);
			
			
		} catch (JSONException e) {
			System.out.println("Couldn't parse/retrieve JSON");
			e.printStackTrace();
		}
	}
}


