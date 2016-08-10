package pojos;

import util.Event.Read_Status;

public class EventReadStatusReqObj extends ReqObj{

	int eventId;
	Read_Status readStatus;
	String userId, accessToken;

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public Read_Status getReadStatus() {
		return readStatus;
	}

	public void setReadStatus(Read_Status readStatus) {
		this.readStatus = readStatus;
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
	
}
