package pojos;

import javax.validation.constraints.NotNull;

public class PostItemResObj extends ResObj {
	
		// UID of item
		@NotNull
		String uid;
		
		// Item Id
		// TBD: change to user id type
		@NotNull
		int itemId;
		
		//Return code for PostItem
		int ReturnCode = 0;
		
		//Error String
		String ErrorString;

		public String getUid() {
			return uid;
		}

		public void setUid(String uid) {
			this.uid = uid;
		}

		public int getItemId() {
			return itemId;
		}

		public void setItemId(int itemId) {
			this.itemId = itemId;
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
		
	}
