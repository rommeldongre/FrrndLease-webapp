package pojos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetRequestsByUserResObj extends ResObj {

	int code, offset;
	String message;

	// Item Details
	int itemId, insurance;
	String title, description, category, leaseTerm, uid, primaryImageLink;

	// request details
	int requestId;
	String requestDate, requestMessage, ownerId, ownerUid, ownerName, ownerProfilePic, ownerLocality, ownerSublocality, distance;
	boolean friend;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getInsurance() {
		return insurance;
	}

	public void setInsurance(int insurance) {
		this.insurance = insurance;
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

	public String getLeaseTerm() {
		return leaseTerm;
	}

	public void setLeaseTerm(String leaseTerm) {
		this.leaseTerm = leaseTerm;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getPrimaryImageLink() {
		return primaryImageLink;
	}

	public void setPrimaryImageLink(String primaryImageLink) {
		this.primaryImageLink = primaryImageLink;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public String getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(String requestDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		try {
			date = sdf.parse(requestDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.requestDate = Long.toString(date.getTime());
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerUid() {
		return ownerUid;
	}

	public void setOwnerUid(String ownerUid) {
		this.ownerUid = ownerUid;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getOwnerProfilePic() {
		return ownerProfilePic;
	}

	public void setOwnerProfilePic(String ownerProfilePic) {
		this.ownerProfilePic = ownerProfilePic;
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

	public String getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		if(distance < 1)
			this.distance = "0m";
		else
			this.distance = Math.round(distance)+"km";
	}

	public String getRequestMessage() {
		return requestMessage;
	}

	public void setRequestMessage(String requestMessage) {
		this.requestMessage = requestMessage;
	}

	public boolean isFriend() {
		return friend;
	}

	public void setFriend(boolean friend) {
		this.friend = friend;
	}

}