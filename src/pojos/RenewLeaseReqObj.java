package pojos;

public class RenewLeaseReqObj extends ReqObj{
	
	int itemId;
	String  userId, reqUserId, flag;
	String accessToken;

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public String getReqUserId() {
		return reqUserId;
	}

	public void setReqUserId(String reqUserId) {
		this.reqUserId = reqUserId;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}
}
