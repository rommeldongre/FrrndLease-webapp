package pojos;

import java.util.ArrayList;
import java.util.List;

public class GetTicketsByXListResObj extends ResObj {

	int code;
	String message;

	int offset;

	List<GetTicketsByXResObj> tickets = new ArrayList<>();

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

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public List<GetTicketsByXResObj> getTickets() {
		return tickets;
	}

	public void setTickets(List<GetTicketsByXResObj> tickets) {
		this.tickets = tickets;
	}

	public void addTickets(GetTicketsByXResObj ticket) {
		this.tickets.add(ticket);
	}

}
