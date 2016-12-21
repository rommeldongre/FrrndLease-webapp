package pojos;

import java.util.ArrayList;
import java.util.List;

public class GetEngagementsByXListResObj extends ResObj{
	
	// Return code for GetEngagementsByX
	int code;

	// Error String
	String message,lastEngagementId;
	
	List<GetEngagementsByXResObj> resList = new ArrayList<>();

	
	public String getLastEngagementId() {
		return lastEngagementId;
	}

	public void setLastEngagementId(String lastEngagementId) {
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

	public List<GetEngagementsByXResObj> getResList() {
		return resList;
	}

	public void setResList(List<GetEngagementsByXResObj> resList) {
		this.resList = resList;
	}

	public void addResList(GetEngagementsByXResObj res) {
		this.resList.add(res);
	}
}
