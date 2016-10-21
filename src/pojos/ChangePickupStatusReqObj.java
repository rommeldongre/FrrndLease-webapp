package pojos;

public class ChangePickupStatusReqObj extends ReqObj {

	String userId, accessToken;
	String leaseUserId, leaseReqUserId;
	int leaseId;
	boolean pickupStatus;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getLeaseUserId() {
		return leaseUserId;
	}

	public void setLeaseUserId(String leaseUserId) {
		this.leaseUserId = leaseUserId;
	}

	public String getLeaseReqUserId() {
		return leaseReqUserId;
	}

	public void setLeaseReqUserId(String leaseReqUserId) {
		this.leaseReqUserId = leaseReqUserId;
	}

	public int getLeaseId() {
		return leaseId;
	}

	public void setLeaseId(int leaseId) {
		this.leaseId = leaseId;
	}

	public boolean isPickupStatus() {
		return pickupStatus;
	}

	public void setPickupStatus(boolean pickupStatus) {
		this.pickupStatus = pickupStatus;
	}

}
