package pojos;

import org.json.JSONException;
import org.json.JSONObject;

import util.FlsLogger;

public class FriendsModel {
	
	private FlsLogger LOGGER = new FlsLogger(FriendsModel.class.getName());
	
	private String friendId, fullName, mobile, userId, referralCode;
	private JSONObject obj;
	
	public void getData(JSONObject ob) {
		obj = ob;
		extractData();
	}
	
	private void extractData() {
		try {
			if(obj.has("id"))friendId = obj.getString("id");
			if(obj.has("fullName"))fullName = obj.getString("fullName");
			if(obj.has("mobile"))mobile = obj.getString("mobile");
			if(obj.has("userId"))userId = obj.getString("userId");
			if(obj.has("referralCode"))referralCode = obj.getString("referralCode");
		} catch (JSONException e) {
			LOGGER.warning("Couldn't parse json");
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

	public String getReferralCode() {
		return referralCode;
	}
}
