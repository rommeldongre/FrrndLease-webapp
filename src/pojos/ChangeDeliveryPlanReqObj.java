package pojos;

public class ChangeDeliveryPlanReqObj extends ReqObj{

	String deliveryPlan;
	int leaseId;

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
