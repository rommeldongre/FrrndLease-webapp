package pojos;

import java.util.ArrayList;
import java.util.List;

public class GetPromoCodesByXListResObj extends ResObj{
	
	// Return code for GetPromoCodesByX
	int code;

	// Error String
	String message;
	int lastPromoCodeId;
	
	List<GetPromoCodesByXResObj> resList = new ArrayList<>();
	
	
	public int getLastPromoCodeId() {
		return lastPromoCodeId;
	}

	public void setLastPromoCodeId(int lastPromoCodeId) {
		this.lastPromoCodeId = lastPromoCodeId;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<GetPromoCodesByXResObj> getResList() {
		return resList;
	}

	public void setResList(List<GetPromoCodesByXResObj> resList) {
		this.resList = resList;
	}

	public void addResList(GetPromoCodesByXResObj res) {
		this.resList.add(res);
	}
}
