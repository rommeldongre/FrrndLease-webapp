package pojos;

import javax.validation.constraints.NotNull;

import java.util.Date;

public class GetItemStoreByXResObj extends ResObj{
	
	//Return code for GetRequestsByUser
	int ReturnCode = 0;
	
	//Error String
	String ErrorString;
	
	
	String title;
	
	// description of item
	String desc;
	
	// category of item
	String category;
		
	// number of items, default is 1
	int quantity = 1;
	
	//Owner Name
	String fullName;
	
	// lease term of item
	String leaseTerm;
	
	//id of item
	int itemId;
	
	// uid of the item
	String uid;
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	
	
	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getLeaseTerm() {
		return leaseTerm;
	}

	public void setLeaseTerm(String leaseTerm) {
		this.leaseTerm = leaseTerm;
	}

	
	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getLeaseValue() {
		return leaseValue;
	}

	public void setLeaseValue(int leaseValue) {
		this.leaseValue = leaseValue;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	//leaseValue of item
	int leaseValue;
	
	//status of item
	String status;
	
	//image of item
	String image;
	
}