package pojos;

import javax.validation.constraints.NotNull;

public class GetItemStoreReqObj extends ReqObj {
	// title of item
		// user posting item
		@NotNull
		String userId;
		
		// Cookie
		// TBD: change to user id type
		@NotNull
		int cookie;
		
		//Category
		String category;	
		
		public String getCategory() {
			return category;
		}
		public void setCategory(String category) {
			this.category = category;
		}
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
