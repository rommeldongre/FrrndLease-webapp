package pojos;

import org.json.JSONArray;

public class UserProfileResObj extends ResObj {

	int code;
	String message, userFullName, userId, userProfilePic, sublocality, locality, wishedList;
	JSONArray friends = new JSONArray();

	// Profile Details
	String address, about, website, email, phoneNo, businessHours;
	String[] imageLinks;
	boolean uber;

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

	public String getUserFullName() {
		return userFullName;
	}

	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserProfilePic() {
		return userProfilePic;
	}

	public void setUserProfilePic(String userProfilePic) {
		this.userProfilePic = userProfilePic;
	}

	public String getSublocality() {
		return sublocality;
	}

	public void setSublocality(String sublocality) {
		this.sublocality = sublocality;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public String getWishedList() {
		return wishedList;
	}

	public void setWishedList(String wishedList) {
		this.wishedList = wishedList;
	}

	public JSONArray getFriends() {
		return friends;
	}

	public void setFriends(JSONArray friends) {
		this.friends = friends;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAbout() {
		return about;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getBusinessHours() {
		return businessHours;
	}

	public void setBusinessHours(String businessHours) {
		this.businessHours = businessHours;
	}

	public String[] getImageLinks() {
		return imageLinks;
	}

	public void setImageLinks(String[] imageLinks) {
		this.imageLinks = imageLinks;
	}

	public boolean isUber() {
		return uber;
	}

	public void setUber(boolean uber) {
		this.uber = uber;
	}

}
