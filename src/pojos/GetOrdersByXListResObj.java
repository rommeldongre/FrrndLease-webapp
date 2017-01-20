package pojos;

import java.util.ArrayList;
import java.util.List;

public class GetOrdersByXListResObj extends ResObj{
	
	// Return code for GetOrdersByX
	int code;

	// Error String
	String message;
	int lastOrderId;
	
	List<GetOrdersByXResObj> resList = new ArrayList<>();
	
	public int getLastOrderId() {
		return lastOrderId;
	}

	public void setLastOrderId(int lastOrderId) {
		this.lastOrderId = lastOrderId;
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

	public List<GetOrdersByXResObj> getResList() {
		return resList;
	}

	public void setResList(List<GetOrdersByXResObj> resList) {
		this.resList = resList;
	}

	public void addResList(GetOrdersByXResObj res) {
		this.resList.add(res);
	}
}
