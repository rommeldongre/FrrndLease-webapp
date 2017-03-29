package app;

import connect.Connect;
import pojos.ChangeTicketDueDateReqObj;
import pojos.ChangeTicketDueDateResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.FlsTicket;
import util.OAuth;

public class ChangeTicketDueDateHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(ChangeTicketDueDateHandler.class.getName());

	private static ChangeTicketDueDateHandler instance;

	public static ChangeTicketDueDateHandler getInstance() {
		if (instance == null)
			instance = new ChangeTicketDueDateHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside process method of ChangeTicketDueDate");

		ChangeTicketDueDateReqObj rq = (ChangeTicketDueDateReqObj) req;
		ChangeTicketDueDateResObj rs = new ChangeTicketDueDateResObj();

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
			int result = ticket.updateDueDate(rq.getTicketId(), rq.getDueDate());

			if (result == 1) {
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
			} else if (result == 0) {
				rs.setCode(FLS_TICKET_DUE_DATE_FAILED);
				rs.setMessage(FLS_TICKET_DUE_DATE_FAILED_M);
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
