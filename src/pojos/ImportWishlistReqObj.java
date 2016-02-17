package pojos;

import javax.validation.constraints.NotNull;

public class ImportWishlistReqObj extends ReqObj{
	
	// url of public wishlist
			@NotNull
			String url;

			public String getUrl() {
				return url;
			}

			public void setUrl(String url) {
				this.url = url;
			}

}
