package pojos;

import javax.validation.constraints.NotNull;

import java.util.Date;

public class GetItemStoreResObj extends ResObj{
	
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
	String owner_name;
	
	// lease term of item
	String leaseTerm;
	
	//id of item
	int id;
	
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

	public String getOwner_name() {
		return owner_name;
	}

	public void setOwner_name(String owner_name) {
		this.owner_name = owner_name;
	}

	public String getLeaseTerm() {
		return leaseTerm;
	}

	public void setLeaseTerm(String leaseTerm) {
		this.leaseTerm = leaseTerm;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLeaseValue() {
		return leaseValue;
	}

	public void setLeaseValue(String leaseValue) {
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
	String leaseValue;
	
	//status of item
	String status;
	
	//image of item
	String image;
	
}