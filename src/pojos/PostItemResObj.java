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
		int code = 0;
		
		//Error String
		String message;

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
		
	}
