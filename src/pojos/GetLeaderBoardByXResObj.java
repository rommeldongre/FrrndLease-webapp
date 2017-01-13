package pojos;

public class GetLeaderBoardByXResObj extends ResObj {

	int code, highestCreditValue = 0, totalCreditMonthly = 0;
	String message, highestCreditUser = "", monthlyCreditUserName = "", mostRequestedItemName = "", mostRequestedItemUserName = "";

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
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

	public String getMonthlyCreditUserName() {
		return monthlyCreditUserName;
	}

	public void setMonthlyCreditUserName(String monthlyCreditUserName) {
		this.monthlyCreditUserName = monthlyCreditUserName;
	}
}
