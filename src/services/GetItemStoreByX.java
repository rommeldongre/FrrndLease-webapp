package services;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.JSONPObject;
import org.json.JSONObject;

import pojos.GetItemStoreByXReqObj;
import pojos.GetItemStoreByXResObj;
import app.GetItemStoreByXHandler;
import app.NotImplementedException;


/**
 * Servlet implementation class GetItemStoreByX
 */
@WebServlet(description = "List Item Requests for a given UserId", urlPatterns = { "/GetItemStoreByX" })
public class GetItemStoreByX extends HttpServlet {
	private static final long serialVersionUID = 1L;
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {
		// TODO Auto-generated method stub
			System.out.println("Inside POST method");
			//HTTP request to Service request pojo 
			ObjectMapper mapper = new ObjectMapper();
			GetItemStoreByXReqObj request = mapper.readValue(httprequest.getInputStream(), GetItemStoreByXReqObj.class);
			httpresponse.setContentType("application/json");
			
			// application logic comes here --------		
			GetItemStoreByXResObj Response = null;
		
			try {
				//App handler to process request and create Service response pojo
				Response = (GetItemStoreByXResObj) GetItemStoreByXHandler.getInstance().process(request);
				
				//Service response pojo to JSON
				PrintWriter out = httpresponse.getWriter();
				httpresponse.setContentType("text/json");				
				httpresponse.setContentType("application/json; charset=UTF-8");	
				mapper.writeValue(out, Response);
				System.out.println("Finished POST method " + Response.getTitle());
					
			} catch (NotImplementedException e) {
				e.printStackTrace();
				System.out.println("GetItemStore process method not implemented");
				httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "GetItemStore process method not implemented");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
	}
}