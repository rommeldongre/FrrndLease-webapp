package app;

import connect.Connect;
import pojos.AddTicketTypeReqObj;
import pojos.AddTicketTypeResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.FlsTicket;
import util.OAuth;

public class AddTicketTypeHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(AddTicketTypeHandler.class.getName());

	private static AddTicketTypeHandler instance;

	public static AddTicketTypeHandler getInstance() {
		if (instance == null)
			instance = new AddTicketTypeHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside process method of AddTicketTypeHandler");
		
		AddTicketTypeReqObj rq = (AddTicketTypeReqObj) req;
		AddTicketTypeResObj rs = new AddTicketTypeResObj();

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
			int result = ticket.addTicketType(rq.getTicketType(), rq.getScript());

			if (result == 1) {
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
			} else {
				rs.setCode(FLS_TICKET_TYPE_NOT_ADDED);
				rs.setMessage(FLS_TICKET_TYPE_NOT_ADDED_M);
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
