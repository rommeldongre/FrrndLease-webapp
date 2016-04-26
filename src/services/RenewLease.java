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
 * Servlet implementation class RenewLease
 */
@WebServlet("/RenewLease")
public class RenewLease extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(RenewLease.class.getName());

	private static final long serialVersionUID = 1L;
	private AdminOpsHandler aoh = new AdminOpsHandler();
	private AdminOpsHandler aoh2 = new AdminOpsHandler();
	private AdminOpsHandler aoh3 = new AdminOpsHandler();

	private Response res = new Response();
	private Response res2 = new Response();
	private Response res3 = new Response();

	private ErrorCat e = new ErrorCat();

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		LOGGER.info("Inside GET Method");

		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		LOGGER.info("Inside POST Method");
		String table;
		PrintWriter out = response.getWriter();

		String str = request.getParameter("req");

		try {
			JSONObject req = new JSONObject(str);
			String flag = req.getString("flag");
			LOGGER.info(flag);
			switch (flag) {

			case "renew":
				JSONObject row = new JSONObject(str);
				JSONObject obj = new JSONObject();
				table = "leases";
				obj.put("table", table);
				obj.put("operation", "RenewLease");

				row.put("userId", "0");
				row.put("status", "0");

				obj.put("row", row);

				// Sending data to Admin-Ops-Handler
				res = aoh.getInfo(table, obj);
				JSONObject json = new JSONObject();

				if (Integer.parseInt(res.getCode()) == e.FLS_SUCCESS) {
					json.put("Code", "FLS_SUCCESS");
					json.put("Message", e.FLS_RENEW_LEASE);
					json.put("Id", res.getId());
				}

				else {
					json.put("Code", res.getCode());
					json.put("Message", res.getMessage());
					json.put("Id", res.getId());
				}

				out.print(json);
				break;

			case "close":
				JSONObject row1 = new JSONObject(str);
				JSONObject obj1 = new JSONObject();
				table = "leases";
				obj1.put("table", table);
				obj1.put("operation", "EditStat");

				row1.put("userId", "0");
				row1.put("status", "Archived");

				obj1.put("row", row1);

				// Sending data to Admin-Ops-Handler
				res = aoh.getInfo(table, obj1); // leases - editstat
				JSONObject json1 = new JSONObject();

				if (Integer.parseInt(res.getCode()) == e.FLS_SUCCESS) {
					JSONObject row2 = new JSONObject();
					JSONObject obj2 = new JSONObject();
					table = "items";
					obj2.put("table", table);
					obj2.put("operation", "EditStat");

					row2.put("id", Integer.parseInt(req.getString("itemId")));
					row2.put("title", "0");
					row2.put("description", "0");
					row2.put("category", "0");
					row2.put("userId", "0");
					row2.put("leaseValue", "0");
					row2.put("leaseTerm", "0");
					row2.put("status", "InStore");
					row2.put("image", "0");
					obj2.put("row", row2);

					res2 = aoh2.getInfo(table, obj2);

					if (res2.getIntCode() == e.FLS_SUCCESS) {
						LOGGER.warning("Item status updated to InStore..");
						JSONObject obj3 = new JSONObject();
						table = "store";
						obj3.put("table", table);
						obj3.put("operation", "add");

						JSONObject row3 = new JSONObject();
						row3.put("itemId", Integer.parseInt(req.getString("itemId")));

						obj3.put("row", row3);

						res3 = aoh3.getInfo(table, obj3);
						if (res3.getIntCode() == e.FLS_SUCCESS) {
							json1.put("Code", "FLS_SUCCESS");
							json1.put("Message", e.FLS_CLOSE_LEASE);
							json1.put("Id", res3.getId());
						}
					}
				}

				else {
					json1.put("Code", res.getCode());
					json1.put("Message", res.getMessage());
					json1.put("Id", res.getId());
				}

				out.print(json1);
				break;

			default:
				break;
			}

		} catch (JSONException e) {
			LOGGER.warning("Couldn't parse/retrieve JSON");
			res.setData(204, "0", "JSON request couldn't be parsed/retrieved (JSON Exception)");
			e.printStackTrace();
		}
	}

}
