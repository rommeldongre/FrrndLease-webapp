package pojos;

import java.util.ArrayList;
import java.util.List;

public class GetEventsByXListResObj extends ResObj{
	
	// Return code for GetRequestsByUser
	int ReturnCode = 0,lastEventId;

	// Error String
	String ErrorString;
	
	List<GetEventsByXResObj> resList = new ArrayList<>();

	public int getLastEventId() {
		return lastEventId;
	}

	public void setLastItemId(int lastEventId) {
		this.lastEventId = lastEventId;
	}

	public int getReturnCode() {
		return ReturnCode;
	}

	public void setReturnCode(int returnCode) {
		ReturnCode = returnCode;
	}

	public String getErrorString() {
		return ErrorString;
	}

	public void setErrorString(String errorString) {
		ErrorString = errorString;
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
