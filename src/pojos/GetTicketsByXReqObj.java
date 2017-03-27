package pojos;

import util.FlsTicket.Filter_Status;

public class GetTicketsByXReqObj extends ReqObj {

	String userId, accessToken, ticketUserId;
	Filter_Status filterStatus;
	int cookie, limit;

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

	public Filter_Status getFilterStatus() {
		return filterStatus;
	}

	public void setFilterStatus(Filter_Status filterStatus) {
		this.filterStatus = filterStatus;
	}

	public int getCookie() {
		return cookie;
	}

	public void setCookie(int cookie) {
		this.cookie = cookie;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

}
