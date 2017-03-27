package services;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import app.GetTicketsByXHandler;
import app.NotImplementedException;
import pojos.GetTicketsByXListResObj;
import pojos.GetTicketsByXReqObj;
import util.FlsLogger;

@WebServlet(description = "Get Tickets By X", urlPatterns = { "/GetTicketsByX" })
public class GetTicketsByX extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(GetTicketsByX.class.getName());

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse)
			throws ServletException, IOException {

		LOGGER.info("Inside Post of GetTicketsByX Service");

		ObjectMapper mapper = new ObjectMapper();
		GetTicketsByXReqObj request = mapper.readValue(httprequest.getInputStream(), GetTicketsByXReqObj.class);
		httpresponse.setContentType("application/json");
		GetTicketsByXListResObj response = null;

		try {

			// App Handler to to process request and create service response
			// into pojo
			response = (GetTicketsByXListResObj) GetTicketsByXHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("GetTicketsByX process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,
					"GetTicketsByX process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
