package pojos;

import javax.validation.constraints.NotNull;

public class GetRequestsByUserReqObj extends ReqObj {
	// title of item
	
	// Id of a user
		// TBD: change to user id type
		@NotNull
		String requestoruserId;
		
		// Token
		// TBD: change to user id type
		@NotNull
		int token;

		public String getRequestoruserId() {
			return requestoruserId;
		}

		public void setRequestoruserId(String requestoruserId) {
			this.requestoruserId = requestoruserId;
		}

		public int getToken() {
			return token;
		}

		public void setToken(int token) {
			this.token = token;
		}
		
		
		
		
}
