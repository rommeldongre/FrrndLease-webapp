package pojos;

import javax.validation.constraints.NotNull;

public class ImportWishlistResObj extends ResObj{
	
	// count of items added from public wishlist
				@NotNull
				Integer wishItemCount;
				
				@NotNull
				Integer totalWishItemCount;

				public Integer getTotalWishItemCount() {
					return totalWishItemCount;
				}

				public void setTotalWishItemCount(Integer totalWishItemCount) {
					this.totalWishItemCount = totalWishItemCount;
				}

				public Integer getWishItemCount() {
					return wishItemCount;
				}

				public void setWishItemCount(Integer wishItemCount) {
					this.wishItemCount = wishItemCount;
				}

}
