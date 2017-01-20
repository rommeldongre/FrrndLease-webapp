package pojos;

public class GetOrdersByXResObj extends ResObj{
	
	int orderId,amount,creditLogId;
	String orderDate,orderUserId,orderUserName,promoCode,razorPayId,orderType;
	
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public int getCreditLogId() {
		return creditLogId;
	}
	public void setCreditLogId(int creditLogId) {
		this.creditLogId = creditLogId;
	}
	public String getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}
	public String getOrderUserId() {
		return orderUserId;
	}
	public void setOrderUserId(String orderUserId) {
		this.orderUserId = orderUserId;
	}
	public String getOrderUserName() {
		return orderUserName;
	}
	public void setOrderUserName(String orderUserName) {
		this.orderUserName = orderUserName;
	}
	public String getPromoCode() {
		return promoCode;
	}
	public void setPromoCode(String promoCode) {
		this.promoCode = promoCode;
	}
	public String getRazorPayId() {
		return razorPayId;
	}
	public void setRazorPayId(String razorPayId) {
		this.razorPayId = razorPayId;
	}
	public String getOrderType() {
		return orderType;
	}
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
}
