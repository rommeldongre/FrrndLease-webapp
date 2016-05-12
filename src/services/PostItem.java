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

import pojos.PostItemReqObj;
import pojos.PostItemResObj;
import util.FlsLogger;
import app.PostItemHandler;
import app.NotImplementedException;

/**
 * Servlet implementation class PostItem
 */
@WebServlet(description = "Post Item for a given request", urlPatterns = { "/PostItem" })
public class PostItem extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(PostItem.class.getName());

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		LOGGER.info("Inside POST method");
		// HTTP request to Service request pojo
		ObjectMapper mapper = new ObjectMapper();
		PostItemReqObj request = mapper.readValue(httprequest.getInputStream(), PostItemReqObj.class);
		httpresponse.setContentType("application/json");

		// application logic comes here --------
		PostItemResObj Response = null;

		try {
			// App handler to process request and create Service response pojo
			Response = (PostItemResObj) PostItemHandler.getInstance().process(request);

			// Service response pojo to JSON
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, Response);
			LOGGER.info("Finished POST method " + Response.getUid());

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("PostItem process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,
					"PostItem process method not implemented");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}