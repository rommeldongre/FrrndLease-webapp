package pojos;

import org.json.JSONException;
import org.json.JSONObject;

import util.FlsLogger;

public class ItemsModel {
	
	private FlsLogger LOGGER = new FlsLogger(ItemsModel.class.getName());
	
	private String title, category, description,userId,leaseTerm,status, uid, primaryImageLink, image;

	int id, leaseValue,leaseId, surcharge;
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
			if(obj.has("leaseId"))
				if(obj.isNull("leaseId")){
					leaseId = 0;
				}else{
					leaseId = obj.getInt("leaseId");
				}
			if(obj.has("category"))category = obj.getString("category");
			if(obj.has("userId"))userId = obj.getString("userId");
			if(obj.has("leaseTerm"))leaseTerm = obj.getString("leaseTerm");
			if(obj.has("id"))id = obj.getInt("id");
			if(obj.has("leaseValue"))leaseValue = obj.getInt("leaseValue");
			if(obj.has("surcharge"))surcharge = obj.getInt("surcharge");
			if(obj.has("status"))status = obj.getString("status");
			if(obj.has("uid")) uid = obj.getString("uid");
			if(obj.has("image")) image = obj.getString("image");
			if(obj.has("primaryImageLink")) primaryImageLink = obj.getString("primaryImageLink");
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
	
	public int getSurcharge() {
		return surcharge;
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
	
	public String getUid() {
		return uid;
	}

	public String getPrimaryImageLink() {
		return primaryImageLink;
	}

	public String getImage() {
		return image;
	}

	public int getLeaseId() {
		return leaseId;
	}
}
