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

import app.GetRequestsByUserHandler;
import app.NotImplementedException;
import pojos.GetRequestsByUserReqObj;
import pojos.GetRequestsByUserResObj;


/**
 * Servlet implementation class GetRequestsByUser
 */
@WebServlet(description = "List Items from the Store by search criterion.", urlPatterns = { "/GetRequestsByUser" })
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
			GetRequestsByUserResObj getResponse = null;
		
			try {
				//App handler to process request and create Service response pojo
				getResponse = (GetRequestsByUserResObj) GetRequestsByUserHandler.getInstance().process(request);
				
				//Service response pojo to JSON
				PrintWriter out = httpresponse.getWriter();
				JSONObject ResponseObj=new JSONObject();
				
				/* sample code. replace with actual mapping from response pojo to JSON 
				ResponseObj.put("title", getResponse.getTitle());
				ResponseObj.put("desc", getResponse.getDesc());
				ResponseObj.put("ReturnCode", getResponse.getReturnCode());
				ResponseObj.put("ErrorString", getResponse.getErrorString());
				ResponseObj.put("userId", getResponse.getUserId());
				ResponseObj.put("categoryId", getResponse.getCategoryId());
				ResponseObj.put("quantity", getResponse.getQuantity());
				ResponseObj.put("leaseTerm", getResponse.getLeaseTerm());
				ResponseObj.put("leaseValue", getResponse.getLeaseValue());
				ResponseObj.put("cookie",getResponse.getCookie());
				*/
				
				httpresponse.setContentType("text/json");				
				httpresponse.setContentType("application/json; charset=UTF-8");
				PrintWriter printout = httpresponse.getWriter();
				printout.print(ResponseObj.toString());		
					
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
