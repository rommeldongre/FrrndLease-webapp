package pojos;

import util.FlsTicket.Ticket_Status;

public class ToggleTicketStatusReqObj extends ReqObj {

	String userId, accessToken;
	Ticket_Status ticketStatus;
	int ticketId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public Ticket_Status getTicketStatus() {
		return ticketStatus;
	}

	public void setTicketStatus(Ticket_Status ticketStatus) {
		this.ticketStatus = ticketStatus;
	}

	public int getTicketId() {
		return ticketId;
	}

	public void setTicketId(int ticketId) {
		this.ticketId = ticketId;
	}

}
