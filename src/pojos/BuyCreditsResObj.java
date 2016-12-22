package pojos;

public class BuyCreditsResObj extends ResObj {

	int code, creditsBalance;
	String message;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getCreditsBalance() {
		return creditsBalance;
	}

	public void setCreditsBalance(int creditsBalance) {
		this.creditsBalance = creditsBalance;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
