package app;

import connect.Connect;
import pojos.AddNoteReqObj;
import pojos.AddNoteResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.FlsTicket;
import util.OAuth;

public class AddNoteHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(AddNoteHandler.class.getName());

	private static AddNoteHandler instance;

	public static AddNoteHandler getInstance() {
		if (instance == null)
			instance = new AddNoteHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside process method of AddNoteHandler");

		AddNoteReqObj rq = (AddNoteReqObj) req;
		AddNoteResObj rs = new AddNoteResObj();

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
			int result = ticket.addNote(rq.getNote(), rq.getTicketId());

			if (result == 1) {
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
			} else if (result == 0) {
				rs.setCode(FLS_NOTE_NOT_ADDED);
				rs.setMessage(FLS_NOTE_NOT_ADDED_M);
			} else if (result == -1) {
				rs.setCode(FLS_TICKET_NOT_FOUND);
				rs.setMessage(FLS_TICKET_NOT_FOUND_M);
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
