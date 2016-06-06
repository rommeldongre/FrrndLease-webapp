package pojos;

public class GetSiteStatsResObj extends ResObj{
	
	int Code,leaseCount,itemCount,requestCount,userCount;
	String Message;
	public int getCode() {
		return Code;
	}
	public void setCode(int code) {
		Code = code;
	}
	public int getLeaseCount() {
		return leaseCount;
	}
	public void setLeaseCount(int leaseCount) {
		this.leaseCount = leaseCount;
	}
	public int getItemCount() {
		return itemCount;
	}
	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}
	public int getRequestCount() {
		return requestCount;
	}
	public void setRequestCount(int requestCount) {
		this.requestCount = requestCount;
	}
	public int getUserCount() {
		return userCount;
	}
	public void setUserCount(int userCount) {
		this.userCount = userCount;
	}
	public String getMessage() {
		return Message;
	}
	public void setMessage(String message) {
		Message = message;
	}
}
