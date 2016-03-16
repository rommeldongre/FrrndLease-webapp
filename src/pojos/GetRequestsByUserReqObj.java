package pojos;

import javax.validation.constraints.NotNull;

public class GetRequestsByUserReqObj extends ReqObj {
	// title of item
		// user posting item
		@NotNull
		String userId;
		
		// Cookie
		// TBD: change to user id type
		@NotNull
		int cookie;
				
		/**
		 * @return the userId
		 */
		public String getUserId() {
			return userId;
		}
		/**
		 * @param userId the userId to set
		 */
		public void setUserId(String userId) {
			this.userId = userId;
		}
		/**
		 * @return the userId
		 */
		/**
		 * @param userId the userId to set
		 */
		public int getCookie() {
			return cookie;
		}
		public void setCookie(int cookie) {
			this.cookie = cookie;
		}
	
		
	}
