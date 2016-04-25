package services;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.JSONPObject;
import org.json.JSONObject;

import pojos.GetProfileReqObj;
import pojos.GetProfileResObj;
import util.FlsLogger;
import app.GetProfileHandler;
import app.NotImplementedException;

/**
 * Servlet implementation class GetProfile
 */

@WebServlet(description = "Get Profile for a given UserId", urlPatterns = { "/GetProfile" })
public class GetProfile extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(GetProfile.class.getName());

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse)
			throws ServletException, IOException {
		// HTTP request to Service request pojo

		LOGGER.info("Inside Post of GetProfile Service");

		ObjectMapper mapper = new ObjectMapper();
		GetProfileReqObj request = mapper.readValue(httprequest.getInputStream(), GetProfileReqObj.class);
		httpresponse.setContentType("application/json");
		// application logic from here..
		GetProfileResObj response = null;

		try {

			// App Handler to to process request and create service response
			// into pojo
			response = (GetProfileResObj) GetProfileHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("GetProfile process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "GetProfile process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
