package pojos;

public class ChangePickupStatusReqObj extends ReqObj {

	int leaseId;
	boolean owner, pickupStatus;

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

	public boolean isOwner() {
		return owner;
	}

	public void setOwner(boolean owner) {
		this.owner = owner;
	}

}
