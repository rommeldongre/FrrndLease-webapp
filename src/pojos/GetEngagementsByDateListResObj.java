package pojos;

import java.util.ArrayList;
import java.util.List;

public class GetEngagementsByDateListResObj extends ResObj{
	
	// Return code for GetEngagementsByDate
	int code;

	// Error String
	String message,lastEngagementId;
	
	List<GetEngagementsByDateResObj> resList = new ArrayList<>();

	
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

	public List<GetEngagementsByDateResObj> getResList() {
		return resList;
	}

	public void setResList(List<GetEngagementsByDateResObj> resList) {
		this.resList = resList;
	}

	public void addResList(GetEngagementsByDateResObj res) {
		this.resList.add(res);
	}
}
