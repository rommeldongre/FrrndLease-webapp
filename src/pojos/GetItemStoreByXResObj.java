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
	
	// owner user id
	String userId;
	
	// lease term of item
	String leaseTerm;
	
	//id of item
	int itemId;
	
	// uid of the item
	String uid;
	
	// locality of user
	String locality;
	
	// sublocality of user
	String sublocality;
	
	// distance of item from current searched location
	String distance;

	//leaseValue of item
	int leaseValue;
	
	//status of item
	String status;
	
	//image of item
	String image, imageLink;
	
	boolean friendStatus;
	
	public boolean isFriendStatus() {
		return friendStatus;
	}

	public void setFriendStatus(boolean friendStatus) {
		this.friendStatus = friendStatus;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public String getSublocality() {
		return sublocality;
	}

	public void setSublocality(String sublocality) {
		this.sublocality = sublocality;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		if(distance < 1)
			this.distance = "0m";
		else
			this.distance = Math.round(distance)+"km";
	}

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

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
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

	public String getImageLink() {
		return imageLink;
	}

	public void setImageLink(String imageLink) {
		this.imageLink = imageLink;
	}
	
}