package pojos;

import org.json.JSONException;
import org.json.JSONObject;

public class FriendsModel {
	private String friendId, fullName, mobile, userId;
	private JSONObject obj;
	
	public void getData(JSONObject ob) {
		obj = ob;
		extractData();
	}
	
	private void extractData() {
		try {
			friendId = obj.getString("id");
			fullName = obj.getString("fullName");
			mobile = obj.getString("mobile");
			userId = obj.getString("userId");
		} catch (JSONException e) {
			System.out.println("Couldn't parse json");
			e.printStackTrace();
		}
	}
	
	public String getFriendId() {
		return this.friendId;
	}
	
	public String getFullName() {
		return this.fullName;
	}
	
	public String getMobile() {
		return this.mobile;
	}
	
	public String getUserId() {
		return this.userId;
	}
}
