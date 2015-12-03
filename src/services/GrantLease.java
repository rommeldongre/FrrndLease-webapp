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
 * Servlet implementation class GrantLease
 */
@WebServlet("/GrantLease")
public class GrantLease extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private AdminOpsHandler aoh1 = new AdminOpsHandler();
	private AdminOpsHandler aoh2 = new AdminOpsHandler();
	private AdminOpsHandler aoh3 = new AdminOpsHandler();
	private AdminOpsHandler aoh4 = new AdminOpsHandler();
	
	private Response res1 = new Response();
	private Response res2 = new Response();
	private Response res3 = new Response();
	private Response res4 = new Response();
	
	private ErrorCat e = new ErrorCat();
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		System.out.println("Inside GET Method");
		
		doPost(request,response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Inside POST Method");
		String table;
		String Id="0", Message=e.FLS_GRANT_LEASE_N_M, Code=String.valueOf(e.FLS_GRANT_LEASE_N);
		PrintWriter out = response.getWriter();
		
		String str = request.getParameter("req");
		
		try {
			JSONObject r = new JSONObject(str);
			JSONObject obj1 = new JSONObject();
			table = "requests";
			obj1.put("table", table);
			obj1.put("operation", "edits");
			
			JSONObject row1 = new JSONObject();
			row1.put("itemId", r.getString("itemId"));
			row1.put("userId", r.getString("userId"));
			obj1.put("row", row1);
			
			res1 = aoh1.getInfo(table, obj1); // goes to requests table - edits()
			System.out.println(res1.getCode());
			if(res1.getIntCode() == e.FLS_SUCCESS){ // 56 is the success code
				System.out.println("Request for that item archived in requests table.");
				JSONObject obj2 = new JSONObject();
				table = "store";
				obj2.put("table", table);
				obj2.put("operation", "delete");
				
				JSONObject row2 = new JSONObject();
				row2.put("itemId", Integer.parseInt(r.getString("itemId")));
				obj2.put("row", row2);
				
				res2 = aoh2.getInfo(table, obj2);// goes to store table - edits()
				
				if(res2.getIntCode() == e.FLS_SUCCESS) {
					System.out.println("Item entry deleted from store table");
					JSONObject obj3 = new JSONObject();
					table = "items";
					obj3.put("table", table);
					obj3.put("operation", "editstat");
					
					JSONObject row3 = new JSONObject();
					row3.put("id", Integer.parseInt(r.getString("itemId")));
					row3.put("title", "0");
					row3.put("description", "0");
					row3.put("category", "0");
					row3.put("userId", "0");
					row3.put("leaseValue", 0);
					row3.put("leaseTerm", "0");
					row3.put("status", "Leased");
					row3.put("image", "0");
					obj3.put("row", row3);
					
					res3 = aoh3.getInfo(table, obj3);// goes to items table - editstat()
					
					if(res3.getIntCode() == e.FLS_SUCCESS) {
						System.out.println("Item status updated to leased");
						JSONObject obj4 = new JSONObject();
						table = "leases";
						obj4.put("table", table);
						obj4.put("operation", "add");
						
						JSONObject row4 = new JSONObject();
						row4.put("reqUserId", r.getString("reqUserId"));
						row4.put("itemId", r.getString("itemId"));
						row4.put("userId", r.getString("userId"));
						row4.put("status", "0");
						
						obj4.putOnce("row", row4);
						
						res4 = aoh4.getInfo(table, obj4);// goes to leases table - add()
						
						if(res4.getIntCode() == e.FLS_SUCCESS) {
							Code = "FLS_SUCCESS";
							Message = e.FLS_GRANT_LEASE;
							Id = res4.getId();
						}
					}
					
				}
			}
			
			else{
				System.out.println("Couldn't perform Grant Lease");
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
