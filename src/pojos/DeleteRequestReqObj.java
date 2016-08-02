package pojos;

public class DeleteRequestReqObj extends ReqObj {
	
	String userId; 
	int request_Id;
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

	public int getRequest_Id() {
		return request_Id;
	}

	public void setRequest_Id(int request_Id) {
		this.request_Id = request_Id;
	}
}
