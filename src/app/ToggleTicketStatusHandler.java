package app;

import connect.Connect;
import pojos.ToggleTicketStatusReqObj;
import pojos.ToggleTicketStatusResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.FlsTicket;
import util.OAuth;

public class ToggleTicketStatusHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(ToggleTicketStatusHandler.class.getName());

	private static ToggleTicketStatusHandler instance = null;

	public static ToggleTicketStatusHandler getInstance() {
		if (instance == null)
			instance = new ToggleTicketStatusHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside process method of ToggleTicketStatus");

		ToggleTicketStatusReqObj rq = (ToggleTicketStatusReqObj) req;
		ToggleTicketStatusResObj rs = new ToggleTicketStatusResObj();

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
			int result = ticket.toggleStatus(rq.getTicketId(), rq.getTicketStatus());
			
			if (result > 0) {
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
			} else if(result == 0) {
				rs.setCode(FLS_TICKET_STATUS_FAILED);
				rs.setMessage(FLS_TICKET_STATUS_FAILED_M);
			} else if (result < 0) {
				rs.setCode(FLS_TICKET_TYPE_NOT_FOUND);
				rs.setMessage(FLS_TICKET_TYPE_NOT_FOUND_M);
			}

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
