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
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		System.out.println("Inside GET Method");
		
		doPost(request,response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Inside POST Method");
		String table;
		String Id="0", Message="Lease Couldn't be granted", Code="220";
		PrintWriter out = response.getWriter();
		
		String str1 = request.getParameter("req1");
		String str2 = request.getParameter("req2");
		String str3 = request.getParameter("req3");
		String str4 = request.getParameter("req4");
		
		try {
			JSONObject obj1 = new JSONObject(str1);
			table = obj1.getString("table");
			System.out.println(table);
			
			res1 = aoh1.getInfo(table, obj1); // goes to leases table - add()
			System.out.println(res1.getCode());
			if(res1.getIntCode() == 15){ // 15 is the success code
				System.out.println("Lease added into lease table.");
				JSONObject obj2 = new JSONObject(str2);
				table = obj2.getString("table");
				System.out.println(table);
				
				res2 = aoh2.getInfo(table, obj2);// goes to requests table - edits()
				
				if(res2.getIntCode() == 56) {
					System.out.println("Request for that item archived in requests table.");
					JSONObject obj3 = new JSONObject(str3);
					table = obj3.getString("table");
					System.out.println(table);
					
					res3 = aoh3.getInfo(table, obj3);// goes to store table - delete()
					
					if(res3.getIntCode() == 30) {
						System.out.println("Item entry deleted from store table");
						JSONObject obj4 = new JSONObject(str4);
						table = obj4.getString("table");
						System.out.println(table);
						
						res4 = aoh4.getInfo(table, obj4);// goes to items table - editstat()
						
						if(res4.getIntCode() == 2) {
							Code = "58";
							Message = "Lease Granted";
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
