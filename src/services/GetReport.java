package services;

import java.io.IOException;
import java.io.PrintWriter;

import app.GetReportHandler;
import app.NotImplementedException;
import pojos.GetReportReqObj;
import pojos.GetReportResObj;
import util.FlsLogger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

@WebServlet(description = "Get Report", urlPatterns = { "/GetReport" })
public class GetReport extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(GetReport.class.getName());

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {

		LOGGER.info("Inside Post of GetReport Service");

		ObjectMapper mapper = new ObjectMapper();
		GetReportReqObj request = mapper.readValue(httprequest.getInputStream(), GetReportReqObj.class);
		httpresponse.setContentType("application/json");
		GetReportResObj response = null;

		try {

			// App Handler to to process request and create service response into pojo
			response = (GetReportResObj) GetReportHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("GetReportHandler process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "GetReportHandler process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
