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
		request.setUid(httprequest.getParameter("uid"));

		ItemDetailsResObj response = null;

		try {

			response = (ItemDetailsResObj) ItemDetailsHandler.getInstance().process(request);

			httpresponse.setContentType("text/html;charset=UTF-8");

			if (response.getCode() == 0) {
				httprequest.setAttribute("code", response.getCode());
				httprequest.setAttribute("title", response.getTitle());
				httprequest.setAttribute("category", response.getCategory());
				httprequest.setAttribute("description", response.getDescription());
				httprequest.setAttribute("leaseValue", response.getLeaseValue());
				httprequest.setAttribute("surcharge", response.getSurcharge());
				httprequest.setAttribute("leaseTerm", response.getLeaseTerm());
				httprequest.setAttribute("userId", response.getUserId());
				httprequest.setAttribute("itemId", response.getId());
				httprequest.setAttribute("uid", response.getUid());
				httprequest.setAttribute("locality", response.getLocality());
				httprequest.setAttribute("sublocality", response.getSublocality());
				httprequest.setAttribute("primaryImageLink", response.getPrimaryImageLink());
				
				String[] arr = response.getImageLinks();

				String links = null;
				
				if(arr != null){
					for(int i = 0; i < arr.length; i++){
						if(links == null)
							links = arr[i];
						else
							links = links + "," + arr[i];
					}
				}
				
				if(links == null)
					httprequest.setAttribute("imageLinks", "");
				else
					httprequest.setAttribute("imageLinks", links);
				
			} else {
				httprequest.setAttribute("code", response.getCode());
				httprequest.setAttribute("message", "This item does not exist");
			}

			httprequest.getRequestDispatcher("/itemdetails.jsp").forward(httprequest, httpresponse);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("ItemDetails process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"ItemDetails process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
