package pojos;

import org.json.JSONObject;

import util.FlsLogger;

public class PromoCodeModel {

	private FlsLogger LOGGER = new FlsLogger(PromoCodeModel.class.getName());

	private String promoCode, expiry = null, userId, accessToken;

	private int credit, count = -1, perPersonCount = -1;
	
	private Code_Type codeType;
	
	public enum Code_Type{
		FLS_INTERNAL,
		FLS_EXTERNAL
	}

	private JSONObject obj;

	public void getData(JSONObject ob) {
		obj = ob;
		extractData();
	}

	private void extractData() {
		try {
			LOGGER.info("Extacting data from row obj");

			if (obj.has("promoCode"))
				promoCode = obj.getString("promoCode");
			if (!obj.isNull("expiry"))
				expiry = obj.getString("expiry");
			if (obj.has("userId"))
				userId = obj.getString("userId");
			if (obj.has("credit"))
				credit = obj.getInt("credit");
			if (obj.has("accessToken"))
				accessToken = obj.getString("accessToken");
			if (obj.has("codeType"))
				codeType = Code_Type.valueOf(obj.getString("codeType"));
			if (obj.has("count"))
				count = obj.getInt("count");
			if (obj.has("perPersonCount"))
				perPersonCount = obj.getInt("perPersonCount");

		} catch (Exception e) {
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

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public Code_Type getCodeType() {
		return codeType;
	}

	public void setCodeType(Code_Type codeType) {
		this.codeType = codeType;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getPerPersonCount() {
		return perPersonCount;
	}

	public void setPerPersonCount(int perPersonCount) {
		this.perPersonCount = perPersonCount;
	}

}
