package pojos;

public class ChangeUserNotificationReqObj extends ReqObj {

	String userId, notification, accessToken;
	int periodicUpdate;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getNotification() {
		return notification;
	}

	public void setNotification(String notification) {
		this.notification = notification;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public int getPeriodicUpdate() {
		return periodicUpdate;
	}

	public void setPeriodicUpdate(int periodicUpdate) {
		this.periodicUpdate = periodicUpdate;
	}

}
