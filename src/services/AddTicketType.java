package services;

import java.io.IOException;
import java.io.PrintWriter;

import app.AddTicketTypeHandler;
import app.NotImplementedException;
import pojos.AddTicketTypeReqObj;
import pojos.AddTicketTypeResObj;
import util.FlsLogger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

@WebServlet(description = "Add Ticket Type", urlPatterns = { "/AddTicketType" })
public class AddTicketType extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(AddTicketType.class.getName());

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {

		LOGGER.info("Inside Post of AddTicketType Service");

		ObjectMapper mapper = new ObjectMapper();
		AddTicketTypeReqObj request = mapper.readValue(httprequest.getInputStream(), AddTicketTypeReqObj.class);
		httpresponse.setContentType("application/json");
		AddTicketTypeResObj response = null;

		try {

			// App Handler to to process request and create service response into pojo
			response = (AddTicketTypeResObj) AddTicketTypeHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("AddTicketTypeHandler process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "AddTicketTypeHandler process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
