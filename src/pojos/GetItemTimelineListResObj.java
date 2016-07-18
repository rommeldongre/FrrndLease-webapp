package pojos;

import java.util.ArrayList;
import java.util.List;

public class GetItemTimelineListResObj extends ResObj {

	int code = 0;
	String message;

	int cookie;
	
	List<GetItemTimelineResObj> resList = new ArrayList<>();

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

	public int getCookie() {
		return cookie;
	}

	public void setCookie(int cookie) {
		this.cookie = cookie;
	}

	public List<GetItemTimelineResObj> getResList() {
		return resList;
	}

	public void setResList(List<GetItemTimelineResObj> resList) {
		this.resList = resList;
	}
	
	public void addResList(GetItemTimelineResObj res){
		this.resList.add(res);
	}
}
