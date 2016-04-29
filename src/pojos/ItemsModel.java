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
			LOGGER.info("Extracting data from row object");
			
			title = obj.getString("title");
			if(obj.isNull("description")){
				description = "";
			}else{
				description = obj.getString("description");
			}
			category = obj.getString("category");
			userId = obj.getString("userId");
			leaseTerm = obj.getString("leaseTerm");
			id = obj.getInt("id");
			leaseValue = obj.getInt("leaseValue");
			status = obj.getString("status");
			image = obj.getString("image");
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
