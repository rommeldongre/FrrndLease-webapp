package services;

import java.io.IOException;
import java.io.PrintWriter;

import app.AddNoteHandler;
import app.NotImplementedException;
import pojos.AddNoteReqObj;
import pojos.AddNoteResObj;
import util.FlsLogger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

@WebServlet(description = "Add Note", urlPatterns = { "/AddNote" })
public class AddNote extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(AddNote.class.getName());

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest httprequest, HttpServletResponse httpresponse) throws ServletException, IOException {

		LOGGER.info("Inside Post of AddNote Service");

		ObjectMapper mapper = new ObjectMapper();
		AddNoteReqObj request = mapper.readValue(httprequest.getInputStream(), AddNoteReqObj.class);
		httpresponse.setContentType("application/json");
		AddNoteResObj response = null;

		try {

			// App Handler to to process request and create service response into pojo
			response = (AddNoteResObj) AddNoteHandler.getInstance().process(request);

			// service response pojo to json
			PrintWriter out = httpresponse.getWriter();
			httpresponse.setContentType("text/json");
			httpresponse.setContentType("application/json; charset=UTF-8");
			mapper.writeValue(out, response);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("AddNoteHandler process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "AddNoteHandler process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
