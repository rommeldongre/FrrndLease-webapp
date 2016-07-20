package pojos;

import org.json.JSONObject;

import util.FlsLogger;

public class PromoCodeModel {

	private FlsLogger LOGGER = new FlsLogger(PromoCodeModel.class.getName());
	
	private String promoCode, expiry, userId;
	
	private int credit;
	
	private JSONObject obj;
	
	public void getData(JSONObject ob){
		obj = ob;
		extractData();
	}
	
	private void extractData(){
		try{
			LOGGER.info("Extacting data from row obj");
			
			if(obj.has("promoCode"))promoCode = obj.getString("promoCode");
			if(obj.has("expiry"))expiry = obj.getString("expiry");
			if(obj.has("userId"))userId = obj.getString("userId");
			if(obj.has("credit"))credit = obj.getInt("credit");
				
		}catch(Exception e){
			e.printStackTrace();
			LOGGER.warning(e.getMessage());
		}
	}

	public String getPromoCode() {
		return promoCode;
	}

	public void setPromoCode(String promoCode) {
		this.promoCode = promoCode;
	}

	public String getExpiry() {
		return expiry;
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getCredit() {
		return credit;
	}

	public void setCredit(int credit) {
		this.credit = credit;
	}
	
}
