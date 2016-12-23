package pojos;

public class GetCreditsLogByXResObj extends ResObj{
	
	int Credits;
	String creditDate,creditType, userName;
	public int getCredits() {
		return Credits;
	}
	public void setCredits(int credits) {
		Credits = credits;
	}
	public String getCreditDate() {
		return creditDate;
	}
	public void setCreditDate(String creditDate) {
		this.creditDate = creditDate;
	}
	public String getCreditType() {
		return creditType;
	}
	public void setCreditType(String creditType) {
		this.creditType = creditType;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
