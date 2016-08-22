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

import app.VerificationHandler;
import app.NotImplementedException;
import pojos.VerificationReqObj;
import pojos.VerificationResObj;
import util.FlsLogger;

@WebServlet(description = "Mobile or Email Verification", urlPatterns = { "/Verification" })
public class Verification extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(Verification.class.getName());

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {
		
		LOGGER.info("Inside Post Method of Verification");

		ObjectMapper mapper = new ObjectMapper();
		VerificationReqObj request = mapper.readValue(httprequest.getInputStream(), VerificationReqObj.class);
		httpresponse.setContentType("application/json");
		
		VerificationResObj response = null;

		try {

			response = (VerificationResObj) VerificationHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("Verification process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Verification process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
