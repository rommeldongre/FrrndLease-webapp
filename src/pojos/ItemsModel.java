package pojos;

import org.json.JSONException;
import org.json.JSONObject;

import util.FlsLogger;

public class ItemsModel {
	
	private FlsLogger LOGGER = new FlsLogger(ItemsModel.class.getName());
	
	private String title, category, description,userId,leaseTerm,status,image, uid;

	int id, leaseValue;
	private JSONObject obj;
	
	public void getData(JSONObject ob) {
		obj = ob;
		extractData();
	}
	
	private void extractData() {
		try {
			LOGGER.info("Extracting data from row object : " + obj.toString());
			
			if(obj.has("title"))title = obj.getString("title");
			if(obj.has("description"))
				if(obj.isNull("description")){
					description = "";
				}else{
					description = obj.getString("description");
				}
			if(obj.has("category"))category = obj.getString("category");
			if(obj.has("userId"))userId = obj.getString("userId");
			if(obj.has("leaseTerm"))leaseTerm = obj.getString("leaseTerm");
			if(obj.has("id"))id = obj.getInt("id");
			if(obj.has("leaseValue"))leaseValue = obj.getInt("leaseValue");
			if(obj.has("status"))status = obj.getString("status");
			if(obj.has("image"))image = obj.getString("image");
			if(obj.has("uid")) uid = obj.getString("uid");
		} catch (JSONException e) {
			LOGGER.warning("Couldn't parse row object of JSON");
			e.printStackTrace();
		}
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public int getLeaseValue() {
		return this.leaseValue;
	}
	
	public int getId(){
		return this.id;
	}
	
	public String getCategory() {
		return this.category;
	}
	
	public String getLeaseTerm() {
		return this.leaseTerm;
	}
	
	public String getUserId() {
		return this.userId;
	}
	
	public String getStatus() {
		return this.status;
	}
	
	public String getImage() {
		return this.image;
	}
	
	public String getUid() {
		return uid;
	}
}
