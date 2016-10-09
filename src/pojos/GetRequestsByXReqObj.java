package pojos;

public class GetRequestsByXReqObj extends ReqObj{

	String userId,reqUserId,status;
	int cookie;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getReqUserId() {
		return reqUserId;
	}
	public void setReqUserId(String reqUserId) {
		this.reqUserId = reqUserId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getCookie() {
		return cookie;
	}
	public void setCookie(int cookie) {
		this.cookie = cookie;
	}
}
