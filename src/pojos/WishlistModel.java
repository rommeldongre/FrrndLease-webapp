package pojos;

import org.json.JSONException;
import org.json.JSONObject;

public class WishlistModel {
private int itemId;
	
	public void getData(JSONObject obj) {
		try {
			itemId = obj.getInt("itemId");
			System.out.println(itemId);
		} catch (JSONException e) {
			System.out.println("Couldnt parse JSON");
			e.printStackTrace();
		}
	}
	
	public int getItemId() {
		return this.itemId;
	}
}
