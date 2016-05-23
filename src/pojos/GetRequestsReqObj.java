package pojos;

public class GetRequestsReqObj extends ReqObj{
	
	// user posting item
			//@NotNull
			String userId;
			
			// Cookie
			// TBD: change to user id type
			//@NotNull
			int cookie;

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
}
