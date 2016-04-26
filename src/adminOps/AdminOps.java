package adminOps;

//import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import util.FlsLogger;

/**
 * Servlet implementation class AdminOps
 */
@WebServlet("/AdminOps")
public class AdminOps extends HttpServlet {
	private FlsLogger LOGGER = new FlsLogger(AdminOps.class.getName());

	private static final long serialVersionUID = 1L;
	private AdminOpsHandler aoh = new AdminOpsHandler();
	private Response res = new Response();

	// GET Method
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		LOGGER.info("Inside GET Method");

		doPost(request, response);
	}

	// POST Method
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		LOGGER.info("Inside POST Method");
		String table;
		PrintWriter out = response.getWriter();

		// string builder method
		/*
		 * StringBuilder jb = new StringBuilder(); String line = null;
		 * 
		 * BufferedReader reader = request.getReader(); while((line =
		 * reader.readLine()) != null){ jb.append(line); }
		 * 
		 * String str = jb.toString(); System.out.println(str);
		 */

		String str = request.getParameter("req");

		try {
			JSONObject obj = new JSONObject(str);
			table = obj.getString("table");

			// Sending data to Admin-Ops-Handler
			res = aoh.getInfo(table, obj);

			JSONObject json = new JSONObject();
			json.put("Code", res.getCode());
			json.put("Message", res.getMessage());
			json.put("Id", res.getId());
			out.print(json);

		} catch (JSONException e) {
			LOGGER.warning("Couldn't parse/retrieve JSON");
			res.setData(204, "0", "JSON request couldn't be parsed/retrieved (JSON Exception)");
			e.printStackTrace();
		}
	}

}
