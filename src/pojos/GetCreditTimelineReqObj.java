package pojos;

public class GetCreditTimelineReqObj extends ReqObj{
	
		// user id 
		String userId;

		// Cookie
		int cookie;
		
		// limit till which to get the items
		int limit;
		
		// id of that particular credit entry
		int creditId;

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

		public int getCreditId() {
			return creditId;
		}

		public void setCreditId(int creditId) {
			this.creditId = creditId;
		}
		
}
