package pojos;

import javax.validation.constraints.NotNull;

public class ImportWishlistReqObj extends ReqObj{
	
	// url of public wishlist
			@NotNull
			String url;
			
	// email id of logged in user
			@NotNull
			String userId;

			public String getUserId() {
				return userId;
			}

			public void setUserId(String userId) {
				this.userId = userId;
			}

			public String getUrl() {
				return url;
			}

			public void setUrl(String url) {
				this.url = url;
			}

}
