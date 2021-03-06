package pojos;

import java.util.ArrayList;
import java.util.List;

public class GetLeadsByXListResObj extends ResObj{
	
	// Return code for GetRequestsByUser
	int code,lastLeadId;

	// Error String
	String message;
	
	List<GetLeadsByXResObj> resList = new ArrayList<>();

	
	public int getLastLeadId() {
		return lastLeadId;
	}

	public void setLastLeadId(int lastLeadId) {
		this.lastLeadId = lastLeadId;
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

	public List<GetLeadsByXResObj> getResList() {
		return resList;
	}

	public void setResList(List<GetLeadsByXResObj> resList) {
		this.resList = resList;
	}

	public void addResList(GetLeadsByXResObj res) {
		this.resList.add(res);
	}
}
