package pojos;

import java.util.ArrayList;
import java.util.List;

public class GetTicketDetailsResObj extends ResObj {

	int code;
	String message, ticketDate, ticketUserId, dueDate, ticketType, ticketStatus;

	List<TicketNote> notes = new ArrayList<>();

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

	public List<TicketNote> getNotes() {
		return notes;
	}

	public void setNotes(List<TicketNote> notes) {
		this.notes = notes;
	}
	
	public void addNotes(TicketNote note) {
		this.notes.add(note);
	}

}
