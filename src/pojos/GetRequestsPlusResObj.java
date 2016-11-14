package pojos;

import java.util.ArrayList;
import java.util.List;

public class GetRequestsPlusResObj extends ResObj {

	int code, offset;
	String message;

	// Item Details
	int itemId, insurance;
	String title, description, category, leaseTerm, uid, primaryImageLink;
	float itemLat, itemLng;

	List<RequestObj> requests = new ArrayList<>();

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

	public int getInsurance() {
		return insurance;
	}

	public void setInsurance(int insurance) {
		this.insurance = insurance;
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

	public float getItemLat() {
		return itemLat;
	}

	public void setItemLat(float itemLat) {
		this.itemLat = itemLat;
	}

	public float getItemLng() {
		return itemLng;
	}

	public void setItemLng(float itemLng) {
		this.itemLng = itemLng;
	}

	public List<RequestObj> getRequests() {
		return requests;
	}

	public void setRequests(List<RequestObj> requests) {
		this.requests = requests;
	}
	
	public void addRequests(RequestObj req){
		this.requests.add(req);
	}

}
