package pojos;

public class GetUserBadgesResObj extends ResObj {

	boolean idVerified;
	String message, signUpStatus;
	int code, itemsPosted, leaseCount;
	long responseTime;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public boolean isIdVerified() {
		return idVerified;
	}

	public void setIdVerified(boolean idVerified) {
		this.idVerified = idVerified;
	}

	public String getSignUpStatus() {
		return signUpStatus;
	}

	public void setSignUpStatus(String signUpStatus) {
		this.signUpStatus = signUpStatus;
	}

	public int getItemsPosted() {
		return itemsPosted;
	}

	public void setItemsPosted(int itemsPosted) {
		this.itemsPosted = itemsPosted;
	}

	public int getLeaseCount() {
		return leaseCount;
	}

	public void setLeaseCount(int leaseCount) {
		this.leaseCount = leaseCount;
	}

	public long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(long responseTime) {
		this.responseTime = responseTime;
	}

}
