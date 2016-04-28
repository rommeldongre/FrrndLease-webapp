package services;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import app.ItemDetailsHandler;
import app.NotImplementedException;
import pojos.ItemDetailsReqObj;
import pojos.ItemDetailsResObj;
import util.FlsLogger;

@WebServlet(description = "Item Details", urlPatterns = { "/ItemDetails" })
public class ItemDetails extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(ItemDetails.class.getName());

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest httprequest, HttpServletResponse httpresponse)
			throws ServletException, IOException {

		LOGGER.info("Inside GET Method");

		ItemDetailsReqObj request = new ItemDetailsReqObj();
		request.setId(Integer.parseInt(httprequest.getParameter("id")));
		request.setTitle(httprequest.getParameter("title"));

		ItemDetailsResObj response = null;

		try {

			response = (ItemDetailsResObj) ItemDetailsHandler.getInstance().process(request);

			httpresponse.setContentType("text/html;charset=UTF-8");

			if (response.getCode() == 0) {
				httprequest.setAttribute("title", response.getTitle());
				httprequest.setAttribute("category", response.getCategory());
				httprequest.setAttribute("description", response.getDescription());
				httprequest.setAttribute("leaseValue", response.getLeaseValue());
				httprequest.setAttribute("leaseTerm", response.getLeaseTerm());
				httprequest.setAttribute("image", response.getImage());
				httprequest.setAttribute("userId", response.getUserId());
				httprequest.setAttribute("itemId", response.getId());
			} else {
				httprequest.setAttribute("id", "Not Found");
				httprequest.setAttribute("title", "Please Try Again Later!!");
			}

			httprequest.getRequestDispatcher("/itemdetails.jsp").forward(httprequest, httpresponse);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("ItemDetails process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,
					"ItemDetails process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
