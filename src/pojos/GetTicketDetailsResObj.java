package pojos;

import org.json.JSONArray;

public class GetTicketDetailsResObj extends ResObj {

	int code;
	String message, ticketDate, ticketUserId, dueDate, ticketType, ticketStatus;
	JSONArray notes = new JSONArray();

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTicketDate() {
		return ticketDate;
	}

	public void setTicketDate(String ticketDate) {
		this.ticketDate = ticketDate;
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

	public String getTicketStatus() {
		return ticketStatus;
	}

	public void setTicketStatus(String ticketStatus) {
		this.ticketStatus = ticketStatus;
	}

	public JSONArray getNotes() {
		return notes;
	}

	public void setNotes(JSONArray notes) {
		this.notes = notes;
	}

}
