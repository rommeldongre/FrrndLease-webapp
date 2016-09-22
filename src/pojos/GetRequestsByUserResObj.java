package pojos;

import javax.validation.constraints.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetRequestsByUserResObj extends ResObj {

	// Return code for GetRequestsByUser
	int ReturnCode = 0;

	// Error String
	String ErrorString;

	String title;

	// description of item
	String desc;

	// number of items, default is 1
	int quantity = 1;

	// Request id
	int request_id;

	// ID of request
	// TBD: need to change type
	@NotNull
	int request_item_id;

	// user posting item
	// TBD: change to user id type
	@NotNull
	String owner_Id;

	// user requesting item
	String user_Id;

	// Cookie
	// TBD: change to user id type
	@NotNull
	int cookie;

	// Request Status
	String Request_status;

	// Request date
	String Request_date;

	// Owner Name
	String owner_name;

	// more items details
	String category, leaseValue, leaseTerm, image, uid, imageLink;

	// more items owners details
	String owner_mobile, owner_address, owner_locality, owner_sublocality;

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

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getOwner_mobile() {
		return owner_mobile;
	}

	public void setOwner_mobile(String owner_mobile) {
		this.owner_mobile = owner_mobile;
	}

	public String getOwner_address() {
		return owner_address;
	}

	public void setOwner_address(String owner_address) {
		this.owner_address = owner_address;
	}

	public String getOwner_locality() {
		return owner_locality;
	}

	public void setOwner_locality(String owner_locality) {
		this.owner_locality = owner_locality;
	}

	public String getOwner_sublocality() {
		return owner_sublocality;
	}

	public void setOwner_sublocality(String owner_sublocality) {
		this.owner_sublocality = owner_sublocality;
	}

	public String getOwner_name() {
		return owner_name;
	}

	public void setOwner_name(String owner_name) {
		this.owner_name = owner_name;
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

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getRequest_id() {
		return request_id;
	}

	public void setRequest_id(int request_id) {
		this.request_id = request_id;
	}

	public int getRequest_item_id() {
		return request_item_id;
	}

	public void setRequest_item_id(int request_item_id) {
		this.request_item_id = request_item_id;
	}

	public String getOwner_Id() {
		return owner_Id;
	}

	public void setOwner_Id(String owner_Id) {
		this.owner_Id = owner_Id;
	}

	public String getUser_Id() {
		return user_Id;
	}

	public void setUser_Id(String user_Id) {
		this.user_Id = user_Id;
	}

	public int getCookie() {
		return cookie;
	}

	public void setCookie(int cookie) {
		this.cookie = cookie;
	}

	public String getRequest_status() {
		return Request_status;
	}

	public void setRequest_status(String request_status) {
		Request_status = request_status;
	}

	public String getRequest_date() {
		return Request_date;
	}

	public void setRequest_date(String request_date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		try {
			date = sdf.parse(request_date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Request_date = Long.toString(date.getTime());
	}

}