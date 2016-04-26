package services;

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
import errorCat.ErrorCat;
import util.FlsLogger;

/**
 * Servlet implementation class SearchItem
 */
@WebServlet("/SearchItem")
public class SearchItem extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(SearchItem.class.getName());

	private static final long serialVersionUID = 1L;
	private AdminOpsHandler aoh = new AdminOpsHandler();
	private Response res = new Response();
	private ErrorCat e = new ErrorCat();

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		LOGGER.info("Inside POST Method");
		String table, title = "%%", description = "%%", category = "%%", leaseTerm = "%%",
				Message = e.FLS_SEARCH_ITEM_F_M;
		int leaseValue, token;
		String Id = "0", Code = String.valueOf(e.FLS_SEARCH_ITEM_F);
		PrintWriter out = response.getWriter();

		String str = request.getParameter("req");
		LOGGER.info(str);
		// String str2 = request.getParameter("req2");

		try {
			JSONObject req = new JSONObject(str);
			if (req.getString("title") != null) {
				title = "%" + req.getString("title") + "%";
			}

			if (req.getString("description") != null) {
				description = "%" + req.getString("description") + "%";
			}

			if (req.getString("category") != null) {
				category = "%" + req.getString("category") + "%";
			}

			if (req.getString("leaseTerm") != null) {
				leaseTerm = "%" + req.getString("leaseTerm") + "%";
			}

			leaseValue = req.getInt("leaseValue");
			token = req.getInt("token");

			JSONObject obj1 = new JSONObject();
			table = "items";
			obj1.put("table", table);
			obj1.put("operation", "searchitem");

			JSONObject row = new JSONObject();
			row.put("id", 0);
			row.put("title", title);
			row.put("description", description);
			row.put("category", category);
			row.put("leaseTerm", leaseTerm);
			row.put("leaseValue", leaseValue);
			row.put("status", "0");
			row.put("userId", "0");
			row.put("image", "0");

			obj1.put("row", row);
			obj1.put("token", token);

			/*
			 * JSONObject obj2 = new JSONObject(str2); table2 =
			 * obj2.getString("table"); LOGGER.info(table2);
			 */

			res = aoh.getInfo(table, obj1);
			LOGGER.info(res.getCode());
			LOGGER.info(res.getId());
			if (res.getIntCode() == e.FLS_SUCCESS) {
				LOGGER.warning("Search done !!!!");
				Id = res.getId();
				Message = res.getMessage();
				Code = "FLS_SUCCESS";
			}

			else {
				LOGGER.warning("no data found");
			}

			JSONObject json = new JSONObject();
			json.put("Code", Code);
			// json.put("Message", Message);
			json.put("Message", Message);
			json.put("Id", Id);
			out.print(json);

		} catch (JSONException e) {
			LOGGER.warning("Couldn't parse/retrieve JSON");
			e.printStackTrace();
		}
	}

}
