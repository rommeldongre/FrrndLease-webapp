package pojos;

import org.json.JSONException;
import org.json.JSONObject;

import util.FlsLogger;

public class RequestsModel {

	private FlsLogger LOGGER = new FlsLogger(RequestsModel.class.getName());
	
	private String userId, itemId, message;
	private JSONObject obj;
	
	public void getData(JSONObject ob) {
		obj = ob;
		extractData();
	}
	
	private void extractData() {
		try {
			if(obj.has("userId"))userId = obj.getString("userId");
			if(obj.has("itemId"))itemId = obj.getString("itemId");
			if(obj.has("message")){
				if(obj.isNull("message")){
					message = null;
				}else{
					message = obj.getString("message");
				}
			}
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

	public String getMessage() {
		return this.message;
	}

}
