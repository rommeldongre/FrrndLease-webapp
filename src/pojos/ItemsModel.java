package pojos;

import org.json.JSONException;
import org.json.JSONObject;

public class ItemsModel {
	private String title, description;
	int quantity,id;
	private JSONObject obj;
	
	public void getData(JSONObject ob) {
		obj = ob;
		extractData();
	}
	
	private void extractData() {
		try {
			System.out.println("Extracting data from row object");
			
			title = obj.getString("title");
			description = obj.getString("description");
			quantity = obj.getInt("quantity");
			id = obj.getInt("id");
			
			System.out.println("Extracted data : " +title + description+ quantity);
		} catch (JSONException e) {
			System.out.println("Couldn't parse row object of JSON");
			e.printStackTrace();
		}
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public int getQuantity() {
		return this.quantity;
	}
	
	public int getId(){
		return this.id;
	}
}
