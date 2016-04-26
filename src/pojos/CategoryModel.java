package pojos;

import org.json.JSONException;
import org.json.JSONObject;

import util.FlsLogger;

public class CategoryModel {
	
	private FlsLogger LOGGER = new FlsLogger(CategoryModel.class.getName());
	
	private String name,description,parent,child;
	private JSONObject obj;
	
	public void getData(JSONObject ob) {
		obj = ob;
		extractData();
	}
	
	private void extractData() {
		try {
			name = obj.getString("name");
			description = obj.getString("description");
			parent = obj.getString("parent");
			child = obj.getString("child");
		} catch (JSONException e) {
			LOGGER.warning("Couldn't parse json");
			e.printStackTrace();
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String getParent() {
		return this.parent;
	}
	
	public String getChild() {
		return this.child;
	}
}
