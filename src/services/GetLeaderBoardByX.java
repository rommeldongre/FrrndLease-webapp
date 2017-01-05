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

import pojos.GetLeaderBoardByXListResObj;
import pojos.GetLeaderBoardByXReqObj;
import pojos.GetLeaderBoardByXResObj;
import util.FlsLogger;
import app.GetLeaderBoardByXHandler;
import app.NotImplementedException;

/**
 * Servlet implementation class GetLeaderBoardByX
 */
@WebServlet(description = "Get Site Statistics", urlPatterns = { "/GetLeaderBoardByX" })
public class GetLeaderBoardByX extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(GetLeaderBoardByX.class.getName());

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
		GetLeaderBoardByXReqObj request = mapper.readValue(httprequest.getInputStream(), GetLeaderBoardByXReqObj.class);
		
		httpresponse.setContentType("application/json");

		// application logic comes here --------
		GetLeaderBoardByXListResObj Response = null;

		try {
			// App handler to process request and create Service response pojo
			Response = (GetLeaderBoardByXListResObj) GetLeaderBoardByXHandler.getInstance().process(request);

			// Service response pojo to JSON
			PrintWriter out = httpresponse.getWriter();
			httpresponse.addHeader("Cache-Control", "max-age=86400");
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, Response);
			LOGGER.info("Finished GET method ");

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("GetLeaderBoardByX process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,
					"GetLeaderBoardByX process method not implemented");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}