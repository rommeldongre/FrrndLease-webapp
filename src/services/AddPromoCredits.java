package services;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import app.AddPromoCreditsHandler;
import app.NotImplementedException;
import pojos.AddPromoCreditsReqObj;
import pojos.AddPromoCreditsResObj;
import util.FlsLogger;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.JSONPObject;
import org.json.JSONObject;

@WebServlet(description = "Add promo credits for a given UserId", urlPatterns = { "/AddPromoCredits" })
public class AddPromoCredits extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(AddPromoCredits.class.getName());

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse)
			throws ServletException, IOException {
		// HTTP request to Service request pojo

		LOGGER.info("Inside Post of AddPromoCredits Service");

		ObjectMapper mapper = new ObjectMapper();
		AddPromoCreditsReqObj request = mapper.readValue(httprequest.getInputStream(), AddPromoCreditsReqObj.class);
		httpresponse.setContentType("application/json");
		// application logic from here..
		AddPromoCreditsResObj response = null;

		try {

			// App Handler to to process request and create service response
			// into pojo
			response = (AddPromoCreditsResObj) AddPromoCreditsHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("AddPromoCredit process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "AddPromoCredit process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
