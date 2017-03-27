package services;

import java.io.IOException;
import java.io.PrintWriter;

import app.ToggleTicketStatusHandler;
import app.NotImplementedException;
import pojos.ToggleTicketStatusReqObj;
import pojos.ToggleTicketStatusResObj;
import util.FlsLogger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

@WebServlet(description = "Toggle Ticket Status", urlPatterns = { "/ToggleTicketStatus" })
public class ToggleTicketStatus extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(ToggleTicketStatus.class.getName());

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {

		LOGGER.info("Inside Post of ToggleTicketStatus Service");

		ObjectMapper mapper = new ObjectMapper();
		ToggleTicketStatusReqObj request = mapper.readValue(httprequest.getInputStream(), ToggleTicketStatusReqObj.class);
		httpresponse.setContentType("application/json");
		ToggleTicketStatusResObj response = null;

		try {

			// App Handler to to process request and create service response into pojo
			response = (ToggleTicketStatusResObj) ToggleTicketStatusHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("ToggleTicketStatus process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "ToggleTicketStatus process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
