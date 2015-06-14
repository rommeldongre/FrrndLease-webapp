package pojos;

import org.json.JSONException;
import org.json.JSONObject;

public class LeaseTermsModel {
	private String name, description;
	private int duration;
	private JSONObject obj;
	
	public void getData(JSONObject ob) {
		obj = ob;
		extractData();
	}
	
	private void extractData() {
		try {
			name = obj.getString("name");
			description = obj.getString("description");
			duration = obj.getInt("duration");
		} catch (JSONException e) {
			System.out.println("Couldn't parse json");
			e.printStackTrace();
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public int getDuration() {
		return this.duration;
	}
}
