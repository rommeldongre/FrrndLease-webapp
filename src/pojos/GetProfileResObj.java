package pojos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetProfileResObj extends ResObj {

	int code;
	String message;

	int credit, liveStatus,periodicUpdate;
	String plan, userUid, fullName, mobile, email, location, address, locality, sublocality, referralCode, photoId, profilePic;
	float lat, lng;
	boolean photoIdVerified;
	String userStatus, userSecStatus, userNotification, userFeeExpiry;
	
	String about, website, mail, phoneNo, businessHours;
	
	String[] imageLinks;

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

	public String getPlan() {
		return plan;
	}

	public void setPlan(String plan) {
		this.plan = plan;
	}

	public String getUserUid() {
		return userUid;
	}

	public void setUserUid(String userUid) {
		this.userUid = userUid;
	}

	public int getLiveStatus() {
		return liveStatus;
	}

	public void setLiveStatus(int liveStatus) {
		this.liveStatus = liveStatus;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setCredit(int credit) {
		this.credit = credit;
	}

	public int getCredit() {
		return this.credit;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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

	public float getLat() {
		return lat;
	}

	public void setLat(float lat) {
		this.lat = lat;
	}

	public float getLng() {
		return lng;
	}

	public void setLng(float lng) {
		this.lng = lng;
	}

	public String getReferralCode() {
		return referralCode;
	}

	public void setReferralCode(String referralCode) {
		this.referralCode = referralCode;
	}

	public String getPhotoId() {
		return photoId;
	}

	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}

	public boolean isPhotoIdVerified() {
		return photoIdVerified;
	}

	public void setPhotoIdVerified(boolean photoIdVerified) {
		this.photoIdVerified = photoIdVerified;
	}

	public String getProfilePic() {
		return profilePic;
	}

	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}

	public String getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(String userStatus) {
		this.userStatus = userStatus;
	}

	public String getUserSecStatus() {
		return userSecStatus;
	}

	public void setUserSecStatus(String userSecStatus) {
		this.userSecStatus = userSecStatus;
	}

	public String getUserNotification() {
		return userNotification;
	}

	public void setUserNotification(String userNotification) {
		this.userNotification = userNotification;
	}

	public int getPeriodicUpdate() {
		return periodicUpdate;
	}

	public void setPeriodicUpdate(int periodicUpdate) {
		this.periodicUpdate = periodicUpdate;
	}

	public String getUserFeeExpiry() {
		return userFeeExpiry;
	}

	public void setUserFeeExpiry(String userFeeExpiry) {
		if(userFeeExpiry != null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			try {
				date = sdf.parse(userFeeExpiry);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			this.userFeeExpiry = Long.toString(date.getTime());
		}else{
			this.userFeeExpiry = userFeeExpiry;
		}
	}

	public String[] getImageLinks() {
		return imageLinks;
	}

	public void setImageLinks(String[] imageLinks) {
		this.imageLinks = imageLinks;
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

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
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
}
