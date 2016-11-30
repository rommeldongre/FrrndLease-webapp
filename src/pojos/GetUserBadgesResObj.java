package pojos;

public class GetUserBadgesResObj extends ResObj {

	boolean idVerified;
	String message, signUpStatus;
	int code, itemsPosted, leaseCount, memberSince;
	String responseTime;

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

	public String getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(long responseTime) {
		if(responseTime == -1){
			this.responseTime = "Not Enough Data";
		}else{
			if(responseTime < 86400)
				this.responseTime = "In A Day";
			else if(responseTime > 86400 && responseTime < 259200)
				this.responseTime = "In 3 Days";
			else
				this.responseTime = "In A Week";
		}
	}

	public int getMemberSince() {
		return memberSince;
	}

	public void setMemberSince(int memberSince) {
		this.memberSince = memberSince;
	}

}
