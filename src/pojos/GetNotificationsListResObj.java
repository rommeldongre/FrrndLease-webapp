package pojos;

import java.util.ArrayList;
import java.util.List;

public class GetNotificationsListResObj extends ResObj {

	int code, offset;
	String message;

	List<GetNotificationsResObj> resList = new ArrayList<>();

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

	public List<GetNotificationsResObj> getResList() {
		return resList;
	}

	public void setResList(List<GetNotificationsResObj> resList) {
		this.resList = resList;
	}

	public void addResList(GetNotificationsResObj res) {
		this.resList.add(res);
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

}
