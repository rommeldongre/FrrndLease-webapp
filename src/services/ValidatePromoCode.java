package services;

import java.io.IOException;
import java.io.PrintWriter;

import app.ValidatePromoCodeHandler;
import app.NotImplementedException;
import pojos.ValidatePromoCodeReqObj;
import pojos.ValidatePromoCodeResObj;
import util.FlsLogger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

@WebServlet(description = "Validate Promo Code for a UserId", urlPatterns = { "/ValidatePromoCode" })
public class ValidatePromoCode extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(ValidatePromoCode.class.getName());

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {

		LOGGER.info("Inside Post of ValidatePromoCode Service");

		ObjectMapper mapper = new ObjectMapper();
		ValidatePromoCodeReqObj request = mapper.readValue(httprequest.getInputStream(), ValidatePromoCodeReqObj.class);
		httpresponse.setContentType("application/json");
		ValidatePromoCodeResObj response = null;

		try {

			// App Handler to to process request and create service response into pojo
			response = (ValidatePromoCodeResObj) ValidatePromoCodeHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("ValidatePromoCode process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "ValidatePromoCode process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
