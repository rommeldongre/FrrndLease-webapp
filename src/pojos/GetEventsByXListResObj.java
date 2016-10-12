package pojos;

import java.util.ArrayList;
import java.util.List;

public class GetEventsByXListResObj extends ResObj{
	
	// Return code for GetRequestsByUser
	int code = 0,lastEventId;

	// Error String
	String message;
	
	List<GetEventsByXResObj> resList = new ArrayList<>();

	public int getLastEventId() {
		return lastEventId;
	}

	public void setLastItemId(int lastEventId) {
		this.lastEventId = lastEventId;
	}
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

	public List<GetEventsByXResObj> getResList() {
		return resList;
	}

	public void setResList(List<GetEventsByXResObj> resList) {
		this.resList = resList;
	}

	public void addResList(GetEventsByXResObj res) {
		this.resList.add(res);
	}
}
