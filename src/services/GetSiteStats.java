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
import org.json.JSONObject;

import pojos.GetSiteStatsReqObj;
import pojos.GetSiteStatsResObj;
import util.FlsLogger;
import app.GetSiteStatsHandler;
import app.NotImplementedException;

/**
 * Servlet implementation class GetSiteStats
 */
@WebServlet(description = "Get Site Statistics", urlPatterns = { "/GetSiteStats" })
public class GetSiteStats extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(GetSiteStats.class.getName());

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		LOGGER.info("Inside GET method");
		// HTTP request to Service request pojo
		
		ObjectMapper mapper = new ObjectMapper();
		GetSiteStatsReqObj request = mapper.readValue(httprequest.getInputStream(), GetSiteStatsReqObj.class);
		
		httpresponse.setContentType("application/json");

		// application logic comes here --------
		GetSiteStatsResObj Response = null;

		try {
			// App handler to process request and create Service response pojo
			Response = (GetSiteStatsResObj) GetSiteStatsHandler.getInstance().process(request);

			// Service response pojo to JSON
			PrintWriter out = httpresponse.getWriter();
			httpresponse.addHeader("Cache-Control", "max-age=86400");
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, Response);
			LOGGER.info("Finished GET method ");

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("GetSiteStats process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,
					"GetSiteStats process method not implemented");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}