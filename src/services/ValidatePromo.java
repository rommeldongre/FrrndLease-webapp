package services;

import java.io.IOException;
import java.io.PrintWriter;

import app.ValidatePromoHandler;
import app.NotImplementedException;
import pojos.ValidatePromoReqObj;
import pojos.ValidatePromoResObj;
import util.FlsLogger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

@WebServlet(description = "Validate Promo for a UserId", urlPatterns = { "/ValidatePromo" })
public class ValidatePromo extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(ValidatePromo.class.getName());

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {

		LOGGER.info("Inside Post of ValidatePromo Service");

		ObjectMapper mapper = new ObjectMapper();
		ValidatePromoReqObj request = mapper.readValue(httprequest.getInputStream(), ValidatePromoReqObj.class);
		httpresponse.setContentType("application/json");
		ValidatePromoResObj response = null;

		try {

			// App Handler to to process request and create service response into pojo
			response = (ValidatePromoResObj) ValidatePromoHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("ValidatePromo process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "ValidatePromo process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
