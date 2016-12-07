package services;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import app.UserProfileHandler;
import app.NotImplementedException;
import pojos.UserProfileReqObj;
import pojos.UserProfileResObj;
import util.FlsLogger;

@WebServlet(description = "User Profile", urlPatterns = { "/UserProfile" })
public class UserProfile extends HttpServlet {

	private FlsLogger LOGGER = new FlsLogger(UserProfile.class.getName());

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest httprequest, HttpServletResponse httpresponse)
			throws ServletException, IOException {

		LOGGER.info("Inside GET Method");

		UserProfileReqObj request = new UserProfileReqObj();
		request.setUserUid(httprequest.getParameter("userUid"));

		UserProfileResObj response = null;

		try {

			response = (UserProfileResObj) UserProfileHandler.getInstance().process(request);

			httpresponse.setContentType("text/html;charset=UTF-8");

			if (response.getCode() == 0) {
				httprequest.setAttribute("code", response.getCode());
				httprequest.setAttribute("message", response.getMessage());
				httprequest.setAttribute("userId", response.getUserId());
				httprequest.setAttribute("userFullName", response.getUserFullName());
				httprequest.setAttribute("userProfilePic", response.getUserProfilePic());
				httprequest.setAttribute("sublocality", response.getSublocality());
				httprequest.setAttribute("locality", response.getLocality());
				httprequest.setAttribute("wishedList", response.getWishedList());
				httprequest.setAttribute("items", response.getItems());
			} else {
				httprequest.setAttribute("code", response.getCode());
				httprequest.setAttribute("message", "This user does not exist");
			}

			httprequest.getRequestDispatcher("/userprofile.jsp").forward(httprequest, httpresponse);

		} catch (NotImplementedException e) {
			e.printStackTrace();
			LOGGER.warning("UserProfile process method not implemented");
			httpresponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"UserProfile process method not implemented");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
