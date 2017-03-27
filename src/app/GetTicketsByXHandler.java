package app;

import connect.Connect;
import pojos.GetTicketsByXListResObj;
import pojos.GetTicketsByXReqObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.FlsTicket;
import util.OAuth;

public class GetTicketsByXHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetTicketsByXHandler.class.getName());

	private static GetTicketsByXHandler instance = null;

	public static GetTicketsByXHandler getInstance() {
		if (instance == null)
			instance = new GetTicketsByXHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside process method of GetTicketsByXHandler");

		GetTicketsByXReqObj rq = (GetTicketsByXReqObj) req;
		GetTicketsByXListResObj rs = new GetTicketsByXListResObj();

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
			rs = ticket.getTicketsByX(rq.getFilterStatus(), rq.getTicketUserId(), rq.getCookie(), rq.getLimit());

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
