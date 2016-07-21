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
import org.json.JSONException;
import org.json.JSONObject;

import app.ForgotPasswordHandler;
import app.NotImplementedException;
import pojos.ForgotPasswordReqObj;
import pojos.ForgotPasswordResObj;
import util.FlsLogger;

@WebServlet(description = "Forgot Password", urlPatterns = { "/ForgotPassword" })
public class ForgotPassword extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(ForgotPassword.class.getName());

	private static final long serialVersionUID = 1L;
	
	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse)
			throws ServletException, IOException {
		// HTTP request to Service request pojo

		LOGGER.info("Inside Post Method of Forgot Password Api");

		ObjectMapper mapper = new ObjectMapper();
		ForgotPasswordReqObj request = mapper.readValue(httprequest.getInputStream(), ForgotPasswordReqObj.class);
		httpresponse.setContentType("application/json");
		// application logic from here..
		ForgotPasswordResObj response = null;

		try {

			// App Handler to to process request and create service response
			// into pojo
			response = (ForgotPasswordResObj) ForgotPasswordHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("ForgotPassword process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "ForgotPassword process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
