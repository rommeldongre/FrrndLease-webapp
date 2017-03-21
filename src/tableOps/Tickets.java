package tableOps;

import org.json.JSONObject;

import adminOps.Response;
import connect.Connect;
import pojos.TicketModel;
import pojos.TicketModel.Ticket_Status;
import util.FlsLogger;
import util.OAuth;

public class Tickets extends Connect {

	private FlsLogger LOGGER = new FlsLogger(Tickets.class.getName());

	private String operation, userId, accessToken, ticketUserId, dueDate, ticketType, note, script;
	private int ticketId;
	private Ticket_Status ticketStatus;

	private TicketModel tkm;
	private Response res = new Response();

	public Response selectOp(String Operation, TicketModel tm, JSONObject obj) {
		operation = Operation.toLowerCase();
		tkm = tm;

		switch (operation) {
		case "addticket":
			LOGGER.info("addticket op is selected..");
			addTicket();
			break;
		default:
			res.setData(FLS_INVALID_OPERATION, "0", FLS_INVALID_OPERATION_M);
			break;
		}

		return res;
	}

	private void addTicket() {

		LOGGER.info("Inside addTicket Method");

		userId = tkm.getUserId();
		accessToken = tkm.getAccessToken();

		OAuth oauth = new OAuth();
		String oauthcheck = oauth.CheckOAuth(accessToken);
		if (!oauthcheck.equals(userId)) {
			res.setData(FLS_ACCESS_TOKEN_FAILED, "0", FLS_ACCESS_TOKEN_FAILED_M);
			return;
		}

	}

}
