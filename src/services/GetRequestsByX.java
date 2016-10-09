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

import pojos.GetRequestsByXReqObj;
import pojos.GetRequestsByXResObj;
import util.FlsLogger;
import app.GetRequestsByXHandler;
import app.NotImplementedException;

/**
 * Servlet implementation class GetRequestsPlus
 */
@WebServlet(description = "Get Either Incoming or outgoing Requests for a given UserId", urlPatterns = { "/GetRequestsByX" })
public class GetRequestsByX  extends HttpServlet{

	private FlsLogger LOGGER = new FlsLogger(GetRequestsByX.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {
		// TODO Auto-generated method stub
		LOGGER.info("Inside POST method");
		//HTTP request to Service request pojo 
		ObjectMapper mapper = new ObjectMapper();
		GetRequestsByXReqObj request = mapper.readValue(httprequest.getInputStream(), GetRequestsByXReqObj.class);
		httpresponse.setContentType("application/json");
	
		// application logic comes here --------		
		GetRequestsByXResObj Response = null;
				
		try {
			//App handler to process request and create Service response pojo
			Response = (GetRequestsByXResObj) GetRequestsByXHandler.getInstance().process(request);
			
			//Service response pojo to JSON
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");				
			httpresponse.setContentType("application/json; charset=UTF-8");	
			mapper.writeValue(out, Response);
			LOGGER.info("Finished POST method");
							
		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("GetRequestsByX process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "GetRequestsByX process method not implemented");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
