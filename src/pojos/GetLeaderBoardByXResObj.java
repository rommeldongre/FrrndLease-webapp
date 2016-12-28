package pojos;

public class GetLeaderBoardByXResObj extends ResObj{
	
	int Code,mostRequestedItemId,highestCreditValue,totalCreditMonthly;
	String Message,mostRequestedItemName,mostRequestedItemUserName,highestCreditUser,monthlyCreditUserId,monthlyCreditUserName;
	public int getCode() {
		return Code;
	}
	public void setCode(int code) {
		Code = code;
	}
	public int getMostRequestedItemId() {
		return mostRequestedItemId;
	}
	public void setMostRequestedItemId(int mostRequestedItemId) {
		this.mostRequestedItemId = mostRequestedItemId;
	}
	public int getHighestCreditValue() {
		return highestCreditValue;
	}
	public void setHighestCreditValue(int highestCreditValue) {
		this.highestCreditValue = highestCreditValue;
	}
	public int getTotalCreditMonthly() {
		return totalCreditMonthly;
	}
	public void setTotalCreditMonthly(int totalCreditMonthly) {
		this.totalCreditMonthly = totalCreditMonthly;
	}
	public String getMostRequestedItemName() {
		return mostRequestedItemName;
	}
	public void setMostRequestedItemName(String mostRequestedItemName) {
		this.mostRequestedItemName = mostRequestedItemName;
	}
	public String getMostRequestedItemUserName() {
		return mostRequestedItemUserName;
	}
	public void setMostRequestedItemUserName(String mostRequestedItemUserName) {
		this.mostRequestedItemUserName = mostRequestedItemUserName;
	}
	public String getHighestCreditUser() {
		return highestCreditUser;
	}
	public void setHighestCreditUser(String highestCreditUser) {
		this.highestCreditUser = highestCreditUser;
	}
	public String getMonthlyCreditUserId() {
		return monthlyCreditUserId;
	}
	public void setMonthlyCreditUserId(String monthlyCreditUserId) {
		this.monthlyCreditUserId = monthlyCreditUserId;
	}
	public String getMonthlyCreditUserName() {
		return monthlyCreditUserName;
	}
	public void setMonthlyCreditUserName(String monthlyCreditUserName) {
		this.monthlyCreditUserName = monthlyCreditUserName;
	}
	public String getMessage() {
		return Message;
	}
	public void setMessage(String message) {
		Message = message;
	}
}
