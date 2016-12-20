package pojos;

import java.util.ArrayList;
import java.util.List;

public class GetEngagementsByUserListResObj extends ResObj{
	
	// Return code for GetRequestsByUser
	int code,lastEngagementId;

	// Error String
	String message;
	
	List<GetEngagementsByUserResObj> resList = new ArrayList<>();

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

	public List<GetEngagementsByUserResObj> getResList() {
		return resList;
	}

	public void setResList(List<GetEngagementsByUserResObj> resList) {
		this.resList = resList;
	}

	public void addResList(GetEngagementsByUserResObj res) {
		this.resList.add(res);
	}
}
