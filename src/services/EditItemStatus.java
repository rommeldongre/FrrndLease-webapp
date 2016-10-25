package services;

import errorCat.ErrorCat;
import util.FlsLogger;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import adminOps.AdminOpsHandler;
import adminOps.Response;

/**
 * Servlet implementation class EditPosting
 */
@WebServlet("/EditItemStatus")
public class EditItemStatus extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(EditItemStatus.class.getName());

	private static final long serialVersionUID = 1L;
	private AdminOpsHandler aoh = new AdminOpsHandler();
	private Response res = new Response();
	private ErrorCat e = new ErrorCat();

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		LOGGER.info("Inside GET Method");

		// doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		LOGGER.info("Inside POST Method");
		String table;
		PrintWriter out = response.getWriter();
		
		String str = request.getParameter("req");

		try {
			JSONObject obj = new JSONObject();
			table = "items";
			obj.put("table", table);
			obj.put("operation", "editstat");

			JSONObject row = new JSONObject(str);
			obj.put("row", row);

			LOGGER.info(table);

			// Sending data to Admin-Ops-Handler
			res = aoh.getInfo(table, obj);

			JSONObject json = new JSONObject();
			if (res.getIntCode() == e.FLS_SUCCESS) {
				json.put("Code", "FLS_SUCCESS");
				json.put("Message", e.FLS_EDIT_ITEM_STATUS);
			}

			else {
				json.put("Code", res.getCode());
				json.put("Message", res.getMessage());
			}

			json.put("Id", res.getId());
			out.print(json);

		} catch (JSONException e) {
			LOGGER.warning("Couldn't parse/retrieve JSON");
			res.setData(204, "0", "JSON request couldn't be parsed/retrieved (JSON Exception)");
			e.printStackTrace();
		}
	}

}
