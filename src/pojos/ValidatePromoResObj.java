package pojos;

public class ValidatePromoResObj extends ResObj {

	int code, discountAmount;
	String message;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(int discountAmount) {
		this.discountAmount = discountAmount;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}