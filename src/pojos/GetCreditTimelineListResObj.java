package pojos;

import java.util.ArrayList;
import java.util.List;

public class GetCreditTimelineListResObj extends ResObj{
	
		// Return code for GetCreditTimeline
		int ReturnCode = 0;

		// Error String
		String ErrorString;

		int lastItemId;

		List< GetCreditTimelineResObj> resList = new ArrayList<>();

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

		public int getLastItemId() {
			return lastItemId;
		}

		public void setLastItemId(int lastItemId) {
			this.lastItemId = lastItemId;
		}

		public List<GetCreditTimelineResObj> getResList() {
			return resList;
		}

		public void setResList(List<GetCreditTimelineResObj> resList) {
			this.resList = resList;
		}
		
		public void addResList(GetCreditTimelineResObj res) {
			this.resList.add(res);
		}

}
