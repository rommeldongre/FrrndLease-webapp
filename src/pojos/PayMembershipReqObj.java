package pojos;

public class PayMembershipReqObj extends ReqObj {

	String userId, accessToken, promoCode, razorPayId;
	int amountPaid;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getPromoCode() {
		return promoCode;
	}

	public void setPromoCode(String promoCode) {
		this.promoCode = promoCode;
	}

	public int getAmountPaid() {
		return amountPaid;
	}

	public void setAmountPaid(int amountPaid) {
		this.amountPaid = amountPaid;
	}

	public String getRazorPayId() {
		return razorPayId;
	}

	public void setRazorPayId(String razorPayId) {
		this.razorPayId = razorPayId;
	}

}
