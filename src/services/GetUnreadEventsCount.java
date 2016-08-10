package services;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.JSONPObject;

import pojos.GetUnreadEventsCountResObj;
import pojos.GetUnreadEventsCountReqObj;
import util.FlsLogger;
import app.GetUnreadEventsCountHandler;
import app.NotImplementedException;

@WebServlet(description = "Get Unread Events Count of Notifications for a given UserId", urlPatterns = { "/GetUnreadEventsCount" })
public class GetUnreadEventsCount extends HttpServlet{

	private FlsLogger LOGGER = new FlsLogger(GetUnreadEventsCount.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {
		// HTTP request to Service request pojo
		
		LOGGER.info("Inside Post of GetUnreadEventsCount Service");
		
		ObjectMapper mapper = new ObjectMapper();
		GetUnreadEventsCountReqObj request = mapper.readValue(httprequest.getInputStream(), GetUnreadEventsCountReqObj.class);
		httpresponse.setContentType("application/json");
		GetUnreadEventsCountResObj response = null;
		
		try {

			// App Handler to to process request and create service response
			// into pojo
			response = (GetUnreadEventsCountResObj) GetUnreadEventsCountHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("GetUnreadEventsCount process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "GetUnreadEventsCount process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
