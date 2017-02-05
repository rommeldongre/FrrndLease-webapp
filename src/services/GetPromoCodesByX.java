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

import pojos.GetPromoCodesByXListResObj;
import pojos.GetPromoCodesByXReqObj;
import pojos.GetPromoCodesByXResObj;
import util.FlsLogger;
import app.GetPromoCodesByXHandler;
import app.NotImplementedException;


/**
 * Servlet implementation class GetPromoCodesByX
 */
@WebServlet(description = "Get Engagements between 2 dates", urlPatterns = { "/GetPromoCodesByX" })
public class GetPromoCodesByX extends HttpServlet {
	
	private FlsLogger LOGGER = new FlsLogger(GetPromoCodesByX.class.getName());
	
	private static final long serialVersionUID = 1L;
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {
		// TODO Auto-generated method stub
			LOGGER.info("Inside POST method");
			//HTTP request to Service request pojo 
			ObjectMapper mapper = new ObjectMapper();
			GetPromoCodesByXReqObj request = mapper.readValue(httprequest.getInputStream(), GetPromoCodesByXReqObj.class);
			httpresponse.setContentType("application/json");
			
			// application logic comes here --------		
			GetPromoCodesByXListResObj Response = null;
		
			try {
				//App handler to process request and create Service response pojo
				Response = (GetPromoCodesByXListResObj) GetPromoCodesByXHandler.getInstance().process(request);
				
				//Service response pojo to JSON
				PrintWriter out = httpresponse.getWriter();
				httpresponse.setContentType("text/json");				
				httpresponse.setContentType("application/json; charset=UTF-8");	
				mapper.writeValue(out, Response);
				LOGGER.info("Finished POST method");
					
			} catch (NotImplementedException e) {
				e.printStackTrace();
				LOGGER.warning("GetPromoCodesByX process method not implemented");
				httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "GetPromoCodesByX process method not implemented");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
	}
}