package app;

import connect.Connect;
import pojos.GetTicketDetailsReqObj;
import pojos.GetTicketDetailsResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.FlsTicket;
import util.OAuth;

public class GetTicketDetailsHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetTicketDetailsHandler.class.getName());

	private static GetTicketDetailsHandler instance = null;

	public static GetTicketDetailsHandler getInstance() {
		if (instance == null)
			instance = new GetTicketDetailsHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside process method of GetTicketDetailsHandler");

		GetTicketDetailsReqObj rq = (GetTicketDetailsReqObj) req;
		GetTicketDetailsResObj rs = new GetTicketDetailsResObj();

		try {

			// Checking oauth of the user
			String userId = rq.getUserId();
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if (!oauthcheck.equals(userId)) {
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}

			FlsTicket ticket = new FlsTicket();
			rs = ticket.getTicketDetails(rq.getTicketId());

		} catch (Exception e) {
			e.printStackTrace();
			rs.setCode(FLS_INVALID_OPERATION);
			rs.setMessage(FLS_INVALID_OPERATION_M);
		}

		LOGGER.info("Finished process method");
		return rs;
	}

	@Override
	public void cleanup() {
	}

}
