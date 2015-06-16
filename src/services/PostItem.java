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
 * Servlet implementation class PostItem
 */
@WebServlet("/PostItem")
public class PostItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private AdminOpsHandler aoh1 = new AdminOpsHandler();
	private AdminOpsHandler aoh2 = new AdminOpsHandler();
	private AdminOpsHandler aoh3 = new AdminOpsHandler();
	private Response res1 = new Response();
	private Response res2 = new Response();
	private Response res3 = new Response();
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		System.out.println("Inside GET Method");
		
		doPost(request,response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Inside POST Method");
		String table1,table2,table3;
		String Id, Message, Code;
		PrintWriter out = response.getWriter();
		
		String str1 = request.getParameter("req1");
		String str2 = request.getParameter("req2");
		String str3 = request.getParameter("req3");
		
		try {
			JSONObject obj1 = new JSONObject(str1);
			table1 = obj1.getString("table");
			System.out.println(table1);
			
			JSONObject obj2 = new JSONObject(str2);
			table2 = obj2.getString("table");
			System.out.println(table2);
			
			JSONObject obj3 = new JSONObject(str3);
			table3 = obj3.getString("table");
			System.out.println(table3);
			
			res1 = aoh1.getInfo(table1, obj1);
			System.out.println(res1.getCode());
			if(res1.getIntCode() == 0){
				System.out.println("Item added to items table..");
				res2 = aoh2.getInfo(table3, obj3);
				
				if(res2.getIntCode() == 29) {
					System.out.println("Item added to store...");
					res3 = aoh3.getInfo(table2, obj2);
					
					Id = res3.getId();
					Message = "PostItem Performed successfully..";
					Code = "50";
				}
				else{
					Id = "0";
					Message = "PostItem couldn't be performed..";
					Code = "210";
				}
			}
			
			else{
				System.out.println("Couldn't perform postItem");
				Id = "0";
				Message = "PostItem couldn't be performed..";
				Code = "210";
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
