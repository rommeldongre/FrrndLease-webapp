package pojos;

import org.json.JSONException;
import org.json.JSONObject;

public class CategoryModel {
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
	
	public String getParent() {
		return this.parent;
	}
	
	public String getChild() {
		return this.child;
	}
}
