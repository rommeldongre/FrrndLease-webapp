package services;

import java.io.IOException;
import java.io.PrintWriter;

import app.BuyCreditsHandler;
import app.NotImplementedException;
import pojos.BuyCreditsReqObj;
import pojos.BuyCreditsResObj;
import util.FlsLogger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

@WebServlet(description = "Buy Credits for a UserId", urlPatterns = { "/BuyCredits" })
public class BuyCredits extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(BuyCredits.class.getName());

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {

		LOGGER.info("Inside Post of BuyCredits Service");

		ObjectMapper mapper = new ObjectMapper();
		BuyCreditsReqObj request = mapper.readValue(httprequest.getInputStream(), BuyCreditsReqObj.class);
		httpresponse.setContentType("application/json");
		BuyCreditsResObj response = null;

		try {

			// App Handler to to process request and create service response into pojo
			response = (BuyCreditsResObj) BuyCreditsHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("BuyCreditsHandler process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "BuyCreditsHandler process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
