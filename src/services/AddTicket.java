package services;

import java.io.IOException;
import java.io.PrintWriter;

import app.AddTicketHandler;
import app.NotImplementedException;
import pojos.AddTicketReqObj;
import pojos.AddTicketResObj;
import util.FlsLogger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

@WebServlet(description = "Add Ticket", urlPatterns = { "/AddTicket" })
public class AddTicket extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(AddTicket.class.getName());

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {

		LOGGER.info("Inside Post of AddTicket Service");

		ObjectMapper mapper = new ObjectMapper();
		AddTicketReqObj request = mapper.readValue(httprequest.getInputStream(), AddTicketReqObj.class);
		httpresponse.setContentType("application/json");
		AddTicketResObj response = null;

		try {

			// App Handler to to process request and create service response into pojo
			response = (AddTicketResObj) AddTicketHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("AddTicketHandler process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "AddTicketHandler process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
