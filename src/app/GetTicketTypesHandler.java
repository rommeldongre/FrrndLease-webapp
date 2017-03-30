package app;

import connect.Connect;
import pojos.GetTicketTypesReqObj;
import pojos.GetTicketTypesResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.FlsTicket;
import util.OAuth;

public class GetTicketTypesHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetTicketTypesHandler.class.getName());

	private static GetTicketTypesHandler instance;

	public static GetTicketTypesHandler getInstance() {
		if (instance == null)
			instance = new GetTicketTypesHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside process method of GetTicketTypesHandler");

		GetTicketTypesReqObj rq = (GetTicketTypesReqObj) req;
		GetTicketTypesResObj rs = new GetTicketTypesResObj();

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
			rs = ticket.getTicketTypes();

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
