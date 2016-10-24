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

import adminOps.Response;
import adminOps.AdminOpsHandler;

/**
 * Servlet implementation class WishItem
 */
@WebServlet("/WishItem")
public class WishItem extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(WishItem.class.getName());

	private static final long serialVersionUID = 1L;
	private AdminOpsHandler aoh1 = new AdminOpsHandler();

	private Response res1 = new Response();

	private ErrorCat e = new ErrorCat();

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		/*
		 * System.out.println("Inside GET Method");
		 * 
		 * doPost(request,response);
		 */
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		LOGGER.info("Inside POST Method");
		String table;
		String Id = "0", Message = e.FLS_WISH_ITEM_F_M;
		int Code = e.FLS_WISH_ITEM_F;
		PrintWriter out = response.getWriter();

		String str = request.getParameter("req");

		try {
			JSONObject row = new JSONObject(str);
			JSONObject obj1 = new JSONObject();
			table = "items";
			obj1.put("table", table);
			obj1.put("operation", "add");
			obj1.put("row", row);

			res1 = aoh1.getInfo(table, obj1);
			LOGGER.info(res1.getCode());
			if (res1.getIntCode() == e.FLS_SUCCESS) {
					LOGGER.warning("Item added to store...");
					Id = res1.getId();
					Message = e.FLS_WISH_ITEM;
					Code = 0;
			}else {
				LOGGER.warning("Couldn't perform WishItem");
			}

			JSONObject json = new JSONObject();
			json.put("Code", Code);
			json.put("Message", Message);
			json.put("Id", Id);
			out.print(json);

		} catch (JSONException e) {
			LOGGER.warning("Couldn't parse/retrieve JSON");
			e.printStackTrace();
		}
	}
}
