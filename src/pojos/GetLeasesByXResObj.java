package pojos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetLeasesByXResObj extends ResObj {

	int code;
	String message;

	int cookie;

	// requesters details
	String requestorUserId, requestorUid, requestorFullName, requestorMobile, requestorAddress, requestorLocality,
			requestorSublocality, requestorProfilePic;

	// owners details
	String ownerUserId, ownerUid, ownerFullName, ownerMobile, ownerAddress, ownerLocality, ownerSublocality, ownerProfilePic;

	// lease details
	String leaseExpiryDate;
	int leaseId;

	// item details
	int itemId, surcharge;
	String title, description, category, leaseValue, leaseTerm, primaryImageLink, status, uid;

	// Delivery Plan
	String deliveryPlan;

	// Delivery Status
	boolean ownerPickupStatus;
	boolean leaseePickupStatus;

	// User Plan
	String ownerPlan;
	String requestorPlan;

	// Friendship
	boolean friend;

	// Distance
	String distance;

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

	public int getCookie() {
		return cookie;
	}

	public void setCookie(int cookie) {
		this.cookie = cookie;
	}

	public String getRequestorUserId() {
		return requestorUserId;
	}

	public void setRequestorUserId(String requestorUserId) {
		this.requestorUserId = requestorUserId;
	}

	public String getRequestorUid() {
		return requestorUid;
	}

	public void setRequestorUid(String requestorUid) {
		this.requestorUid = requestorUid;
	}

	public String getRequestorFullName() {
		return requestorFullName;
	}

	public void setRequestorFullName(String requestorFullName) {
		this.requestorFullName = requestorFullName;
	}

	public String getRequestorMobile() {
		return requestorMobile;
	}

	public void setRequestorMobile(String requestorMobile) {
		this.requestorMobile = requestorMobile;
	}

	public String getRequestorAddress() {
		return requestorAddress;
	}

	public void setRequestorAddress(String requestorAddress) {
		this.requestorAddress = requestorAddress;
	}

	public String getRequestorLocality() {
		return requestorLocality;
	}

	public void setRequestorLocality(String requestorLocality) {
		this.requestorLocality = requestorLocality;
	}

	public String getRequestorSublocality() {
		return requestorSublocality;
	}

	public void setRequestorSublocality(String requestorSublocality) {
		this.requestorSublocality = requestorSublocality;
	}

	public String getOwnerUserId() {
		return ownerUserId;
	}

	public void setOwnerUserId(String ownerUserId) {
		this.ownerUserId = ownerUserId;
	}

	public String getOwnerUid() {
		return ownerUid;
	}

	public void setOwnerUid(String ownerUid) {
		this.ownerUid = ownerUid;
	}

	public String getOwnerFullName() {
		return ownerFullName;
	}

	public void setOwnerFullName(String ownerFullName) {
		this.ownerFullName = ownerFullName;
	}

	public String getOwnerMobile() {
		return ownerMobile;
	}

	public void setOwnerMobile(String ownerMobile) {
		this.ownerMobile = ownerMobile;
	}

	public String getOwnerAddress() {
		return ownerAddress;
	}

	public void setOwnerAddress(String ownerAddress) {
		this.ownerAddress = ownerAddress;
	}

	public String getOwnerLocality() {
		return ownerLocality;
	}

	public void setOwnerLocality(String ownerLocality) {
		this.ownerLocality = ownerLocality;
	}

	public String getOwnerSublocality() {
		return ownerSublocality;
	}

	public void setOwnerSublocality(String ownerSublocality) {
		this.ownerSublocality = ownerSublocality;
	}

	public String getLeaseExpiryDate() {
		return leaseExpiryDate;
	}

	public void setLeaseExpiryDate(String leaseExpiryDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		try {
			date = sdf.parse(leaseExpiryDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.leaseExpiryDate = Long.toString(date.getTime());
	}

	public int getLeaseId() {
		return leaseId;
	}

	public void setLeaseId(int leaseId) {
		this.leaseId = leaseId;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getSurcharge() {
		return surcharge;
	}

	public void setSurcharge(int surcharge) {
		this.surcharge = surcharge;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getLeaseValue() {
		return leaseValue;
	}

	public void setLeaseValue(String leaseValue) {
		this.leaseValue = leaseValue;
	}

	public String getLeaseTerm() {
		return leaseTerm;
	}

	public void setLeaseTerm(String leaseTerm) {
		this.leaseTerm = leaseTerm;
	}

	public String getPrimaryImageLink() {
		return primaryImageLink;
	}

	public void setPrimaryImageLink(String primaryImageLink) {
		this.primaryImageLink = primaryImageLink;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getRequestorProfilePic() {
		return requestorProfilePic;
	}

	public void setRequestorProfilePic(String requestorProfilePic) {
		this.requestorProfilePic = requestorProfilePic;
	}

	public String getOwnerProfilePic() {
		return ownerProfilePic;
	}

	public void setOwnerProfilePic(String ownerProfilePic) {
		this.ownerProfilePic = ownerProfilePic;
	}

	public String getDeliveryPlan() {
		return deliveryPlan;
	}

	public void setDeliveryPlan(String deliveryPlan) {
		this.deliveryPlan = deliveryPlan;
	}

	public boolean isOwnerPickupStatus() {
		return ownerPickupStatus;
	}

	public void setOwnerPickupStatus(boolean ownerPickupStatus) {
		this.ownerPickupStatus = ownerPickupStatus;
	}

	public boolean isLeaseePickupStatus() {
		return leaseePickupStatus;
	}

	public void setLeaseePickupStatus(boolean leaseePickupStatus) {
		this.leaseePickupStatus = leaseePickupStatus;
	}

	public String getOwnerPlan() {
		return ownerPlan;
	}

	public void setOwnerPlan(String ownerPlan) {
		this.ownerPlan = ownerPlan;
	}

	public String getRequestorPlan() {
		return requestorPlan;
	}

	public void setRequestorPlan(String requestorPlan) {
		this.requestorPlan = requestorPlan;
	}

	public boolean isFriend() {
		return friend;
	}

	public void setFriend(boolean friend) {
		this.friend = friend;
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
}
