package pojos;

import util.FlsEnums.Read_Status;

public class EventReadStatusReqObj extends ReqObj{

	int eventId;
	Read_Status readStatus;

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
	
}
