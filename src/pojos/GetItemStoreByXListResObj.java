package pojos;

import java.util.ArrayList;
import java.util.List;

public class GetItemStoreByXListResObj extends ResObj {

	// Return code for GetRequestsByUser
	int ReturnCode = 0;

	// Error String
	String ErrorString;

	int lastItemId;

	List<GetItemStoreByXResObj> resList = new ArrayList<>();

	public int getLastItemId() {
		return lastItemId;
	}

	public void setLastItemId(int lastItemId) {
		this.lastItemId = lastItemId;
	}

	public int getReturnCode() {
		return ReturnCode;
	}

	public void setReturnCode(int returnCode) {
		ReturnCode = returnCode;
	}

	public String getErrorString() {
		return ErrorString;
	}

	public void setErrorString(String errorString) {
		ErrorString = errorString;
	}

	public List<GetItemStoreByXResObj> getResList() {
		return resList;
	}

	public void setResList(List<GetItemStoreByXResObj> resList) {
		this.resList = resList;
	}

	public void addResList(GetItemStoreByXResObj res) {
		this.resList.add(res);
	}

}
