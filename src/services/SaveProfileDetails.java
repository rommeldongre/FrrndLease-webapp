package services;

import java.io.IOException;
import java.io.PrintWriter;

import app.SaveProfileDetailsHandler;
import app.NotImplementedException;
import pojos.SaveProfileDetailsReqObj;
import pojos.SaveProfileDetailsResObj;
import util.FlsLogger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

@WebServlet(description = "Save Profile Details for a user id", urlPatterns = { "/SaveProfileDetails" })
public class SaveProfileDetails extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(SaveProfileDetails.class.getName());

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {

		LOGGER.info("Inside Post of ValidatePromo Service");

		ObjectMapper mapper = new ObjectMapper();
		SaveProfileDetailsReqObj request = mapper.readValue(httprequest.getInputStream(), SaveProfileDetailsReqObj.class);
		httpresponse.setContentType("application/json");
		SaveProfileDetailsResObj response = null;

		try {

			// App Handler to to process request and create service response into pojo
			response = (SaveProfileDetailsResObj) SaveProfileDetailsHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("SaveProfileDetails process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "SaveProfileDetails process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
