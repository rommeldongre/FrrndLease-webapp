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

import pojos.GetLeasesByXReqObj;
import pojos.GetLeasesByXResObj;
import util.FlsLogger;
import app.GetLeasesByXHandler;
import app.NotImplementedException;

/**
 * Servlet implementation class GetRequestsPlus
 */
@WebServlet(description = "Get Either Incoming or outgoing lease for a given UserId", urlPatterns = { "/GetLeasesByX" })
public class GetLeasesByX  extends HttpServlet{

	private FlsLogger LOGGER = new FlsLogger(GetLeasesByX.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {
		// TODO Auto-generated method stub
		LOGGER.info("Inside POST method");
		//HTTP request to Service request pojo 
		ObjectMapper mapper = new ObjectMapper();
		GetLeasesByXReqObj request = mapper.readValue(httprequest.getInputStream(), GetLeasesByXReqObj.class);
		httpresponse.setContentType("application/json");
					
		// application logic comes here --------		
		GetLeasesByXResObj Response = null;
				
		try {
			//App handler to process request and create Service response pojo
			Response = (GetLeasesByXResObj) GetLeasesByXHandler.getInstance().process(request);
						
			//Service response pojo to JSON
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");				
			httpresponse.setContentType("application/json; charset=UTF-8");	
			mapper.writeValue(out, Response);
			LOGGER.info("Finished POST method");
							
		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("GetLeasesByX process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "GetLeasesByX process method not implemented");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
