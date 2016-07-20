package services;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.JSONPObject;
import org.json.JSONObject;

import app.GetItemTimelineHandler;
import app.NotImplementedException;
import pojos.GetItemTimelineListResObj;
import pojos.GetItemTimelineReqObj;
import util.FlsLogger;

/**
 * Servlet implementation class  GetCreditTimeline
 */
@WebServlet(description = "Get Item Timeline for a given ItemId", urlPatterns = { "/GetItemTimeline" })
public class GetItemTimeline extends HttpServlet{
	
	private FlsLogger LOGGER = new FlsLogger( GetItemTimeline.class.getName());
	
	private static final long serialVersionUID = 1L;
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {
		// TODO Auto-generated method stub
			LOGGER.info("Inside POST method");
			//HTTP request to Service request pojo 
			ObjectMapper mapper = new ObjectMapper();
			GetItemTimelineReqObj request = mapper.readValue(httprequest.getInputStream(),  GetItemTimelineReqObj.class);
			httpresponse.setContentType("application/json");
			
			// application logic comes here --------		
			 GetItemTimelineListResObj Response = null;
		
			try {
				//App handler to process request and create Service response pojo
				Response = ( GetItemTimelineListResObj)  GetItemTimelineHandler.getInstance().process(request);
				
				//Service response pojo to JSON
				PrintWriter out = httpresponse.getWriter();
				httpresponse.setContentType("text/json");				
				httpresponse.setContentType("application/json; charset=UTF-8");	
				mapper.writeValue(out, Response);
				LOGGER.info("Finished POST method");
					
			} catch (NotImplementedException e) {
				e.printStackTrace();
				LOGGER.warning("GetCreditTimeline process method not implemented");
				httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "GetCreditTimeline process method not implemented");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
	}
}
