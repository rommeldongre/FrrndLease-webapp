package services;

import java.io.IOException;
import java.io.PrintWriter;

import app.ValidateAndCommitPromoHandler;
import app.NotImplementedException;
import pojos.ValidateAndCommitPromoReqObj;
import pojos.ValidateAndCommitPromoResObj;
import util.FlsLogger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

@WebServlet(description = "Validate And Commit Promo for a UserId", urlPatterns = { "/ValidateAndCommitPromo" })
public class ValidateAndCommitPromo extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(ValidateAndCommitPromo.class.getName());

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {

		LOGGER.info("Inside Post of ValidateAndCommitPromo Service");

		ObjectMapper mapper = new ObjectMapper();
		ValidateAndCommitPromoReqObj request = mapper.readValue(httprequest.getInputStream(), ValidateAndCommitPromoReqObj.class);
		httpresponse.setContentType("application/json");
		ValidateAndCommitPromoResObj response = null;

		try {

			// App Handler to to process request and create service response into pojo
			response = (ValidateAndCommitPromoResObj) ValidateAndCommitPromoHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("ValidateAndCommitPromo process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "ValidateAndCommitPromo process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
