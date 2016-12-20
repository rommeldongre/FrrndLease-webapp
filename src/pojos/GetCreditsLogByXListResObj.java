package pojos;

import java.util.ArrayList;
import java.util.List;

public class GetCreditsLogByXListResObj extends ResObj{
	
	// Return code for GetRequestsByUser
	int code,lastEngagementId;

	// Error String
	String message;
	
	List<GetCreditsLogByXResObj> resList = new ArrayList<>();

	public int getLastEngagementId() {
		return lastEngagementId;
	}

	public void setLastEngagementId(int lastEngagementId) {
		this.lastEngagementId = lastEngagementId;
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

	public List<GetCreditsLogByXResObj> getResList() {
		return resList;
	}

	public void setResList(List<GetCreditsLogByXResObj> resList) {
		this.resList = resList;
	}

	public void addResList(GetCreditsLogByXResObj res) {
		this.resList.add(res);
	}
}
