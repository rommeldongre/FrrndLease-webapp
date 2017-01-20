package pojos;

public class GetRazorPayDetailsByXReqObj extends ReqObj {

	String razorPayId,userId;
	int cookie, limit;
	
	public String getRazorPayId() {
		return razorPayId;
	}
	public void setRazorPayId(String razorPayId) {
		this.razorPayId = razorPayId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public int getCookie() {
		return cookie;
	}
	public void setCookie(int cookie) {
		this.cookie = cookie;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
}
