package pojos;

public class AddPromoCreditsResObj extends ResObj{

	int code;
	String message;
	
	int newCreditBalance;

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

	public int getNewCreditBalance() {
		return newCreditBalance;
	}

	public void setNewCreditBalance(int newCreditBalance) {
		this.newCreditBalance = newCreditBalance;
	}
}
