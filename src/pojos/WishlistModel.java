package pojos;

import org.json.JSONException;
import org.json.JSONObject;

import util.FlsLogger;

public class WishlistModel {

	private FlsLogger LOGGER = new FlsLogger(WishlistModel.class.getName());

	private int itemId;

	public void getData(JSONObject obj) {
		try {
			itemId = obj.getInt("itemId");
		} catch (JSONException e) {
			LOGGER.warning("Couldnt parse JSON");
			e.printStackTrace();
		}
	}

	public int getItemId() {
		return this.itemId;
	}
}
