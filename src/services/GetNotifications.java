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

import pojos.GetNotificationsReqObj;
import pojos.GetNotificationsResObj;
import util.FlsLogger;
import app.GetNotificationsHandler;
import app.NotImplementedException;

@WebServlet(description = "Get Notifications for a given UserId", urlPatterns = { "/GetNotifications" })
public class GetNotifications extends HttpServlet{

	private FlsLogger LOGGER = new FlsLogger(GetNotifications.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {
		// HTTP request to Service request pojo
		
		LOGGER.info("Inside Post of GetNotifications Service");
		
		ObjectMapper mapper = new ObjectMapper();
		GetNotificationsReqObj request = mapper.readValue(httprequest.getInputStream(), GetNotificationsReqObj.class);
		httpresponse.setContentType("application/json");
		GetNotificationsResObj response = null;
		
		try {

			// App Handler to to process request and create service response
			// into pojo
			response = (GetNotificationsResObj) GetNotificationsHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("GetProfile process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "GetProfile process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
