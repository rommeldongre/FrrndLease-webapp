package pojos;

import org.json.JSONException;
import org.json.JSONObject;

import util.FlsLogger;

public class RequestsModel {

	private FlsLogger LOGGER = new FlsLogger(RequestsModel.class.getName());
	
	private String userId, itemId;
	private JSONObject obj;
	
	public void getData(JSONObject ob) {
		obj = ob;
		extractData();
	}
	
	private void extractData() {
		try {
			userId = obj.getString("userId");
			itemId = obj.getString("itemId");
		} catch (JSONException e) {
			LOGGER.warning("Couldn't parse json");
			e.printStackTrace();
		}
	}
	
	public String getUserId() {
		return this.userId;
	}
	
	public String getItemId() {
		return this.itemId;
	}
}
