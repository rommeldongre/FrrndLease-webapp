package services;

import java.io.IOException;
import java.io.PrintWriter;

import app.GetTicketDetailsHandler;
import app.NotImplementedException;
import pojos.GetTicketDetailsReqObj;
import pojos.GetTicketDetailsResObj;
import util.FlsLogger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

@WebServlet(description = "Get Ticket Details", urlPatterns = { "/GetTicketDetails" })
public class GetTicketDetails extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(GetTicketDetails.class.getName());

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {

		LOGGER.info("Inside Post of AddTicket Service");

		ObjectMapper mapper = new ObjectMapper();
		GetTicketDetailsReqObj request = mapper.readValue(httprequest.getInputStream(), GetTicketDetailsReqObj.class);
		httpresponse.setContentType("application/json");
		GetTicketDetailsResObj response = null;

		try {

			// App Handler to to process request and create service response into pojo
			response = (GetTicketDetailsResObj) GetTicketDetailsHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("GetTicketDetails process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "GetTicketDetails process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
