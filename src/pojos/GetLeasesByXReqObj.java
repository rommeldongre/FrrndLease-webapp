package pojos;

public class GetLeasesByXReqObj extends ReqObj{

	String leaseUserId;
	
	String leaseReqUserId;
	
	int cookie;
	
	String status;

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

	public int getCookie() {
		return cookie;
	}

	public void setCookie(int cookie) {
		this.cookie = cookie;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
