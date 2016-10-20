package pojos;

public class ChangeDeliveryPlanReqObj extends ReqObj{

	String userId, accessToken, deliveryPlan;
	int leaseId;

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

	public String getDeliveryPlan() {
		return deliveryPlan;
	}

	public void setDeliveryPlan(String deliveryPlan) {
		this.deliveryPlan = deliveryPlan;
	}

	public int getLeaseId() {
		return leaseId;
	}

	public void setLeaseId(int leaseId) {
		this.leaseId = leaseId;
	}
	
}
