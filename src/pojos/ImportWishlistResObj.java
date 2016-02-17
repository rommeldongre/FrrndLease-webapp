package pojos;

import javax.validation.constraints.NotNull;

public class ImportWishlistResObj extends ResObj{
	
	// count of items added from public wishlist
				@NotNull
				Integer wishItemCount;

				public Integer getWishItemCount() {
					return wishItemCount;
				}

				public void setWishItemCount(Integer wishItemCount) {
					this.wishItemCount = wishItemCount;
				}

}
