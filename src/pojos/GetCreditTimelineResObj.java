package pojos;

public class GetCreditTimelineResObj extends ResObj{

	//Return code for GetCreditTimeline
	int ReturnCode = 0;
		
	//Error String
	String ErrorString;
	
	//User ID
	String userId;
	
	//Date on which credit was credited
	String credit_date;
	
	//credit amount
	int credit_amount;

	//Type of Operation
	String credit_type;
	
	//Description of credit operation
	String description;

	public int getReturnCode() {
		return ReturnCode;
	}

	public void setReturnCode(int returnCode) {
		ReturnCode = returnCode;
	}

	public String getErrorString() {
		return ErrorString;
	}

	public void setErrorString(String errorString) {
		ErrorString = errorString;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCredit_date() {
		return credit_date;
	}

	public void setCredit_date(String credit_date) {
		this.credit_date = credit_date;
	}

	public int getCredit_amount() {
		return credit_amount;
	}

	public void setCredit_amount(int credit_amount) {
		this.credit_amount = credit_amount;
	}

	public String getCredit_type() {
		return credit_type;
	}

	public void setCredit_type(String credit_type) {
		this.credit_type = credit_type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
