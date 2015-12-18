package pojos;

import javax.validation.constraints.NotNull;

public class GetRequestsByUserResObj extends ResObj{
	
	//Return code for GetRequestsByUser
	int ReturnCode = 0;
	
	//Error String
	String ErrorString;
	
	
	private String title;
	
	// description of item
	private String desc;
	
	// number of items, default is 1
	int quantity = 1;
	
	// category of item
	// TBD: need to change type
	@NotNull
	String categoryId;
	
	// user posting item
	// TBD: change to user id type
	@NotNull
	String userId;
	
	// Cookie
	// TBD: change to user id type
	@NotNull
	int cookie;
			
	// term of lease: week/month/quarter/year/forever
	//LeaseTermType leaseTerm;
	String leaseTerm;
	
	// reasonable replacement value in Indian Rs.
	long leaseValue = 0;
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}
	/**
	 * @param desc the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}
	/**
	 * @return the quantity
	 */
	public int getQuantity() {
		return quantity;
	}
	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	/**
	 * @return the categoryId
	 */
	public String getCategoryId() {
		return categoryId;
	}
	/**
	 * @param string the categoryId to set
	 */
	public void setCategoryId(String string) {
		this.categoryId = string;
	}
	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	/**
	 * @return the leaseValue
	 */
	public long getLeaseValue() {
		return leaseValue;
	}
	/**
	 * @param leaseValue the leaseValue to set
	 */
	public void setLeaseValue(long leaseValue) {
		this.leaseValue = leaseValue;
	}

	/**
	 * @return the userId
	 */
	/*
	public String getCookie() {
		return cookie;
	}*/
	/**
	 * @param userId the userId to set
	 */
	/*
	public void setCookie(String cookie) {
		this.userId = cookie;
	}
	*/
	
	public int getReturnCode() {
		return ReturnCode;
	}
	public int getCookie() {
		return cookie;
	}
	public void setCookie(int cookie) {
		this.cookie = cookie;
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
	/*
	public LeaseTermType getLeaseTerm() {
		return leaseTerm;
	}
	public void setLeaseTerm(LeaseTermType leaseTerm) {
		this.leaseTerm = leaseTerm;
	}*/
	public String getLeaseTerm() {
		return leaseTerm;
	}
	public void setLeaseTerm(String leaseTerm) {
		this.leaseTerm = leaseTerm;
	}
	
	
}
