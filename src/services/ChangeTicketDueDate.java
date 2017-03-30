package services;

import java.io.IOException;
import java.io.PrintWriter;

import app.ChangeTicketDueDateHandler;
import app.NotImplementedException;
import pojos.ChangeTicketDueDateReqObj;
import pojos.ChangeTicketDueDateResObj;
import util.FlsLogger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

@WebServlet(description = "Change Ticket Due Date", urlPatterns = { "/ChangeTicketDueDate" })
public class ChangeTicketDueDate extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(ChangeTicketDueDate.class.getName());

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {

		LOGGER.info("Inside Post of ChangeTicketDueDate Service");

		ObjectMapper mapper = new ObjectMapper();
		ChangeTicketDueDateReqObj request = mapper.readValue(httprequest.getInputStream(), ChangeTicketDueDateReqObj.class);
		httpresponse.setContentType("application/json");
		ChangeTicketDueDateResObj response = null;

		try {

			// App Handler to to process request and create service response into pojo
			response = (ChangeTicketDueDateResObj) ChangeTicketDueDateHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("ChangeTicketDueDate process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "ChangeTicketDueDate process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
