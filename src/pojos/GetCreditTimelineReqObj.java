package pojos;

public class GetCreditTimelineReqObj extends ReqObj{
	
		// user id 
		String userId;

		// Cookie
		int cookie;
		
		// limit till which to get the items
		int limit;

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public int getCookie() {
			return cookie;
		}

		public void setCookie(int cookie) {
			this.cookie = cookie;
		}

		public int getLimit() {
			return limit;
		}

		public void setLimit(int limit) {
			this.limit = limit;
		}
}
