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
import org.json.JSONException;
import org.json.JSONObject;

import app.EventReadStatusHandler;
import app.NotImplementedException;
import pojos.EventReadStatusReqObj;
import pojos.EventReadStatusResObj;
import util.FlsLogger;

@WebServlet(description = "Event Read Status", urlPatterns = { "/EventReadStatus" })
public class EventReadStatus extends HttpServlet{

	private FlsLogger LOGGER = new FlsLogger(EventReadStatus.class.getName());

	private static final long serialVersionUID = 1L;
	
	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse)
			throws ServletException, IOException {
		// HTTP request to Service request pojo

		LOGGER.info("Inside Post Method of Event Read Status Api");

		ObjectMapper mapper = new ObjectMapper();
		EventReadStatusReqObj request = mapper.readValue(httprequest.getInputStream(), EventReadStatusReqObj.class);
		httpresponse.setContentType("application/json");
		// application logic from here..
		EventReadStatusResObj response = null;

		try {

			// App Handler to to process request and create service response
			// into pojo
			response = (EventReadStatusResObj) EventReadStatusHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("EventReadStatus process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "EventReadStatus process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
