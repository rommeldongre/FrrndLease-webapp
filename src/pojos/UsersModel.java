package pojos;

import org.json.JSONException;
import org.json.JSONObject;

import util.FlsLogger;

public class UsersModel {

	private FlsLogger LOGGER = new FlsLogger(UsersModel.class.getName());

	private String userId, fullName, mobile, location, auth, activation, status, address, locality, sublocality;
	private float lat, lng;
	private JSONObject obj;

	public void getData(JSONObject ob) {
		obj = ob;
		extractData();
	}

	private void extractData() {
		try {
			userId = obj.getString("userId");
			fullName = obj.getString("fullName");
			mobile = obj.getString("mobile");
			location = obj.getString("location");
			auth = obj.getString("auth");
			if (obj.has("activation") || obj.has("status")) {
				activation = obj.getString("activation");
				status = obj.getString("status");
			}
			address = obj.getString("address");
			locality = obj.getString("locality");
			sublocality = obj.getString("sublocality");
			lat = Float.parseFloat(obj.getString("lat"));
			lng = Float.parseFloat(obj.getString("lng"));
		} catch (JSONException e) {
			LOGGER.warning("Couldn't parse json");
			e.printStackTrace();
		}
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
}
