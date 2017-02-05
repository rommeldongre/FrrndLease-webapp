package services;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import app.EditProfileHandler;
import app.NotImplementedException;
import pojos.EditProfileReqObj;
import pojos.EditProfileResObj;
import util.FlsLogger;

@WebServlet(description = "Edit Profile for a given UserId", urlPatterns = { "/EditProfile" })
public class EditProfile extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(EditProfile.class.getName());

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse)
			throws ServletException, IOException {
		// HTTP request to Service request pojo

		LOGGER.info("Inside Post of EditProfile Service");

		ObjectMapper mapper = new ObjectMapper();
		EditProfileReqObj request = mapper.readValue(httprequest.getInputStream(), EditProfileReqObj.class);
		httpresponse.setContentType("application/json");
		// application logic from here..
		EditProfileResObj response = null;

		try {

			// App Handler to to process request and create service response
			// into pojo
			response = (EditProfileResObj) EditProfileHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("EditProfile process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,
					"EditProfile process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
