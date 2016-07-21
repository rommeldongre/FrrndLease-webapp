package pojos;

import org.json.JSONException;
import org.json.JSONObject;

import util.FlsLogger;

public class UsersModel {

	private FlsLogger LOGGER = new FlsLogger(UsersModel.class.getName());

	private String userId, fullName, mobile, location, auth, activation, status, address, locality, sublocality, referralCode,profilePicture;
	private int liveStatus, verification;
	private float lat, lng;
	private JSONObject obj;

	public void getData(JSONObject ob) {
		obj = ob;
		extractData();
	}

	private void extractData() {
		try {
			if(obj.has("userId"))userId = obj.getString("userId");
			if(obj.has("fullName"))fullName = obj.getString("fullName");
			if(obj.has("mobile"))mobile = obj.getString("mobile");
			if(obj.has("location"))location = obj.getString("location");
			if(obj.has("auth"))auth = obj.getString("auth");
			if(obj.has("activation"))activation = obj.getString("activation");
			if(obj.has("status"))status = obj.getString("status");
			if(obj.has("address"))address = obj.getString("address");
			if(obj.has("locality"))locality = obj.getString("locality");
			if(obj.has("sublocality"))sublocality = obj.getString("sublocality");
			if(obj.has("lat"))lat = Float.parseFloat(obj.getString("lat"));
			if(obj.has("lng"))lng = Float.parseFloat(obj.getString("lng"));
			if(obj.has("referralCode"))referralCode = obj.getString("referralCode");
			if(obj.has("profilePicture"))profilePicture = obj.getString("profilePicture");
			if(obj.has("liveStatus"))liveStatus = obj.getInt("liveStatus");
			if(obj.has("verification"))verification = obj.getInt("verification");
		} catch (JSONException e) {
			LOGGER.warning("Couldn't parse json");
			e.printStackTrace();
		}
	}

	public void setActivation(String activation) {
		this.activation = activation;
	}

	public String getUserId() {
		return this.userId;
	}

	public String getFullName() {
		return this.fullName;
	}

	public String getMobile() {
		return this.mobile;
	}

	public String getLocation() {
		return this.location;
	}

	public String getAuth() {
		return this.auth;
	}

	public String getActivation() {
		return this.activation;
	}

	public String getStatus() {
		return this.status;
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

	public String getProfilePicture() {
		return profilePicture;
	}

	public int getLiveStatus() {
		return liveStatus;
	}

	public void setLiveStatus(int liveStatus) {
		this.liveStatus = liveStatus;
	}

	public int getVerification() {
		return verification;
	}

	public void setVerification(int verification) {
		this.verification = verification;
	}
}
