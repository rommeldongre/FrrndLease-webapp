package services;

import errorCat.ErrorCat;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import adminOps.AdminOpsHandler;
import adminOps.Response;

/**
 * Servlet implementation class DeleteWishlist
 */
@WebServlet("/DeleteWishlist")
public class DeleteWishlist extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private AdminOpsHandler aoh = new AdminOpsHandler();
	private Response res = new Response();
	private ErrorCat e = new ErrorCat();
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		System.out.println("Inside GET Method");
		
		doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Inside POST Method");
		String table;
		PrintWriter out = response.getWriter();

		String str = request.getParameter("req");
		
		try {
			JSONObject obj = new JSONObject();
			table = "items";
			obj.put("table", table);
			obj.put("operation", "deleteWish");
			
			JSONObject row = new JSONObject(str);
			row.put("leaseValue", 0);
			row.put("title", "0");
			row.put("description", "0");
			row.put("category", "0");
			row.put("leaseTerm", "0");
			row.put("status", "InStore");
			row.put("image", "0");
			obj.put("row", row);
			
			
			System.out.println(table);
			
			//Sending data to Admin-Ops-Handler
			res = aoh.getInfo(table, obj);
			
			JSONObject json = new JSONObject();
			if(res.getIntCode() == e.FLS_SUCCESS) {
				json.put("Code", "FLS_SUCCESS");
			}
			
			else {
				json.put("Code", res.getCode());
			}
			
			json.put("Message", res.getMessage());
			json.put("Id", res.getId());
			out.print(json);
			
		} catch (JSONException e) {
			System.out.println("Couldn't parse/retrieve JSON");
			res.setData(204, "0", "JSON request couldn't be parsed/retrieved (JSON Exception)");
			e.printStackTrace();
		}
	}

}
