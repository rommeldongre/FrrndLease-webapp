package service;

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

import pojos.GetRequestsByUserReqObj;
import pojos.GetRequestsByUserResObj;
import app.GetRequestsByUserHandler;
import app.NotImplementedException;


/**
 * Servlet implementation class GetRequestsByUser
 */
@WebServlet(description = "List Item Requests for a given UserId", urlPatterns = { "/GetRequestsByUser" })
public class GetRequestsByUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {
		// TODO Auto-generated method stub
				
			//HTTP request to Service request pojo 
			ObjectMapper mapper = new ObjectMapper();
			GetRequestsByUserReqObj request = mapper.readValue(httprequest.getInputStream(), GetRequestsByUserReqObj.class);
			httpresponse.setContentType("application/json");
			
			// application logic comes here --------		
			GetRequestsByUserResObj Response = null;
		
			try {
				//App handler to process request and create Service response pojo
				Response = (GetRequestsByUserResObj) GetRequestsByUserHandler.getInstance().process(request);
				
				//Service response pojo to JSON
				PrintWriter out = httpresponse.getWriter();
				httpresponse.setContentType("text/json");				
				httpresponse.setContentType("application/json; charset=UTF-8");	
				mapper.writeValue(out, Response);

					
			} catch (NotImplementedException e) {
				e.printStackTrace();
				System.out.println("GetRequestsByUser process method not implemented");
				httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "GetRequestsByUser process method not implemented");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
	}
}