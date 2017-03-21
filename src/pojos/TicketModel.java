package pojos;

import org.json.JSONObject;

import util.FlsLogger;

public class TicketModel {

	private FlsLogger LOGGER = new FlsLogger(TicketModel.class.getName());

	private String userId, accessToken, ticketUserId, dueDate, ticketType, note, script;
	
	private int ticketId;
	
	private Ticket_Status ticketStatus;
	
	public enum Ticket_Status {
		FLS_OPEN,
		FLS_CLOSED
	}

	private JSONObject obj;

	public void getData(JSONObject ob) {
		obj = ob;
		extractData();
	}

	private void extractData() {
		try {
			LOGGER.info("Extacting data from row obj");

			if (obj.has("userId"))
				userId = obj.getString("userId");
			if (obj.has("accessToken"))
				accessToken = obj.getString("accessToken");
			if (obj.has("ticketUserId"))
				ticketUserId = obj.getString("ticketUserId");
			if (obj.has("dueDate"))
				dueDate = obj.getString("dueDate");
			if (obj.has("ticketType"))
				ticketType = obj.getString("ticketType");
			if (obj.has("note"))
				note = obj.getString("note");
			if (obj.has("script"))
				script = obj.getString("script");
			if (obj.has("ticketId"))
				ticketId = obj.getInt("ticketId");
			if (obj.has("ticketStatus"))
				ticketStatus = Ticket_Status.valueOf(obj.getString("ticketStatus"));

		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.warning(e.getMessage());
		}
	}

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

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public int getTicketId() {
		return ticketId;
	}

	public void setTicketId(int ticketId) {
		this.ticketId = ticketId;
	}

	public Ticket_Status getTicketStatus() {
		return ticketStatus;
	}

	public void setTicketStatus(Ticket_Status ticketStatus) {
		this.ticketStatus = ticketStatus;
	}
}
