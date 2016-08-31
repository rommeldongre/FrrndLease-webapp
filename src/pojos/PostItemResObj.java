package pojos;

public class PostItemResObj extends ResObj {
	
		// UID of item
		String uid;
		
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
