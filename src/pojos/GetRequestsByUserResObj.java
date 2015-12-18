package pojos;

import javax.validation.constraints.NotNull;

public class GetRequestsByUserResObj extends ResObj{
	
	@NotNull
	private String title;
	
	// description of item
	private String desc;
	
	// Request Id of Of a Request
	// TBD: need to change type
	@NotNull
	int requestId;
	
	// Id of a user
	// TBD: change to user id type
	@NotNull
	String owneruserId;
	
	// Date on which Item was posted
	// TBD: change to user id type
	@NotNull
	String date;
	
	// Token
	// TBD: change to user id type
	@NotNull
	int token;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public String getOwneruserId() {
		return owneruserId;
	}

	public void setOwneruserId(String owneruserId) {
		this.owneruserId = owneruserId;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getToken() {
		return token;
	}

	public void setToken(int token) {
		this.token = token;
	}
	
	
	
}
