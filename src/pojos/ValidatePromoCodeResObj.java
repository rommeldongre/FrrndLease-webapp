package pojos;

public class ValidatePromoCodeResObj extends ResObj {

	int code;
	String message, promoCode;
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

	public String getPromoCode() {
		return promoCode;
	}

	public void setPromoCode(String promoCode) {
		this.promoCode = promoCode;
	}

	public int getNewCreditBalance() {
		return newCreditBalance;
	}

	public void setNewCreditBalance(int newCreditBalance) {
		this.newCreditBalance = newCreditBalance;
	}

}
