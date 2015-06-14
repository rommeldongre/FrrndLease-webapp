package pojos;

import org.json.JSONException;
import org.json.JSONObject;

public class UsersModel {
	private String userId,fullName,mobile,location,auth;
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
		} catch (JSONException e) {
			System.out.println("Couldn't parse json");
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
}
