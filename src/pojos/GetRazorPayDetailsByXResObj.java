package pojos;

public class GetRazorPayDetailsByXResObj extends ResObj{
	
	int amount;
	String paymentId,paymentDate,paymentUserEmail,paymentUserPhoneNumber;
	
	public String getPaymentId() {
		return paymentId;
	}
	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public String getPaymentUserPhoneNumber() {
		return paymentUserPhoneNumber;
	}
	public void setPaymentUserPhoneNumber(String paymentUserPhoneNumber) {
		this.paymentUserPhoneNumber = paymentUserPhoneNumber;
	}
	public String getPaymentDate() {
		return paymentDate;
	}
	public void setPaymentDate(String paymentDate) {
		this.paymentDate = paymentDate;
	}
	public String getPaymentUserEmail() {
		return paymentUserEmail;
	}
	public void setPaymentUserEmail(String paymentUserEmail) {
		this.paymentUserEmail = paymentUserEmail;
	}
}
