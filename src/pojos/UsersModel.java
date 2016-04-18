package pojos;

import org.json.JSONException;
import org.json.JSONObject;

public class UsersModel {
	private String userId,fullName,mobile,location,auth,activation,status;
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
			if(obj.has("activation") || obj.has("status")){
				activation = obj.getString("activation");
				status = obj.getString("status");
			}
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
	
	public String getActivation() {
		return this.activation;
	}

	public String getStatus() {
		return this.status;
	}
}
