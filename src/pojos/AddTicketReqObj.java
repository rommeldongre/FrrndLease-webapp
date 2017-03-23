package pojos;

public class AddTicketReqObj extends ReqObj {

	String userId, accessToken, ticketUserId, dueDate, ticketType;

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

	public String getTicketUserId() {
		return ticketUserId;
	}

	public void setTicketUserId(String ticketUserId) {
		this.ticketUserId = ticketUserId;
	}

	public String getDueDate() {
		return dueDate;
	}

	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}

	public String getTicketType() {
		return ticketType;
	}

	public void setTicketType(String ticketType) {
		this.ticketType = ticketType;
	}
	
}
