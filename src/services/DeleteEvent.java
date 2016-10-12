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

import pojos.DeleteEventReqObj;
import pojos.DeleteEventResObj;
import util.FlsLogger;
import app.DeleteEventHandler;
import app.NotImplementedException;

/**
 * Servlet implementation class DeleteEvent
 */
@WebServlet(description = "List Item Requests for a given UserId", urlPatterns = { "/DeleteEvent" })
public class DeleteEvent extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(DeleteEvent.class.getName());

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		LOGGER.info("Inside POST method");
		// HTTP request to Service request pojo
		ObjectMapper mapper = new ObjectMapper();
		DeleteEventReqObj request = mapper.readValue(httprequest.getInputStream(), DeleteEventReqObj.class);
		httpresponse.setContentType("application/json");

		// application logic comes here --------
		DeleteEventResObj Response = null;

		try {
			// App handler to process request and create Service response pojo
			Response = (DeleteEventResObj) DeleteEventHandler.getInstance().process(request);

			// Service response pojo to JSON
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, Response);
			// System.out.println("Finished POST method " +
			// Response.getTitle());

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("DeleteEvent process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,
					"DeleteEvent process method not implemented");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}