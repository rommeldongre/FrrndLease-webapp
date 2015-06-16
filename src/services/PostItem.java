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
	private Response res1 = new Response();
	private Response res2 = new Response();
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		System.out.println("Inside GET Method");
		
		doPost(request,response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Inside POST Method");
		String table1,table2;
		String Id="0", Message="PostItem couldn't be performed..", Code="210";
		PrintWriter out = response.getWriter();
		
		String str1 = request.getParameter("req1");
		String str2 = request.getParameter("req2");
		
		try {
			JSONObject obj1 = new JSONObject(str1);
			table1 = obj1.getString("table");
			System.out.println(table1);
			
			JSONObject obj2 = new JSONObject(str2);
			table2 = obj2.getString("table");
			System.out.println(table2);
			
			res1 = aoh1.getInfo(table1, obj1);
			System.out.println(res1.getCode());
			System.out.println(res1.getId());
			if(res1.getIntCode() == 0){
				System.out.println("Item added to items table..");
				JSONObject row = new JSONObject();
				row.put("itemId", Integer.parseInt(res1.getId()));
				obj2.put("row", row);
				res2 = aoh2.getInfo(table2, obj2);
				
				if(res2.getIntCode() == 29) {
					Id = res2.getId();
					Message = "PostItem Performed successfully..";
					Code = "50";
				}
			}
			
			else{
				System.out.println("Couldn't perform postItem");
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
