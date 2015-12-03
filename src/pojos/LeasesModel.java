package pojos;

import org.json.JSONException;
import org.json.JSONObject;

public class LeasesModel {
	private String reqUserId, itemId, userId,status;
	private JSONObject obj;
	
	public void getData(JSONObject ob) {
		obj = ob;
		extractData();
	}
	
	private void extractData() {
		try {
			reqUserId = obj.getString("reqUserId");
			itemId = obj.getString("itemId");
			userId = obj.getString("userId");
			status = obj.getString("status");
		} catch (JSONException e) {
			System.out.println("Couldn't parse json");
			e.printStackTrace();
		}
	}
	
	public String getReqUserId() {
		return this.reqUserId;
	}
	
	public String getItemId() {
		return this.itemId;
	}
	
	public String getUserId() {
		return this.userId;
	}
	
	public String getStatus() {
		return this.status;
	}
}
