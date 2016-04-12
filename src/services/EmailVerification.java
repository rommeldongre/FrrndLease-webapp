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

import app.EmailVerificationHandler;
import app.NotImplementedException;
import pojos.EmailVerificationReqObj;
import pojos.EmailVerificationResObj;

@WebServlet(description = "Email Verification", urlPatterns = { "/EmailVerification" })
public class EmailVerification extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		System.out.println("Inside GET Method");

		doPost(request, response);
	}

	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse)
			throws ServletException, IOException {
		// HTTP request to Service request pojo

		ObjectMapper mapper = new ObjectMapper();
		
		EmailVerificationReqObj request = new EmailVerificationReqObj();
		request.setVerification(httprequest.getQueryString());
		
		httpresponse.setContentType("application/json");
		// application logic from here..
		EmailVerificationResObj response = null;

		try {

			// App Handler to to process request and create service response
			// into pojo
			response = (EmailVerificationResObj) EmailVerificationHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			System.out.println("GetProfile process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "GetProfile process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
