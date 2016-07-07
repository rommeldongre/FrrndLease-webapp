package pojos;

import javax.validation.constraints.NotNull;

public class GetItemStoreByXReqObj extends ReqObj {
	// title of item
	// user posting item
	@NotNull
	String userId;

	// Cookie
	// TBD: change to user id type
	@NotNull
	int cookie;

	// limit till which to get the items
	int limit;
	
	// lat lng from the search bar's location
	float lat, lng;
	
	// storing the search string
	String searchString;
	
	// storing the item status
	String[] itemStatus;

	public String[] getItemStatus() {
		return itemStatus;
	}

	public void setItemStatus(String[] itemStatus) {
		this.itemStatus = itemStatus;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public float getLat() {
		return lat;
	}

	public void setLat(float lat) {
		this.lat = lat;
	}

	public float getLng() {
		return lng;
	}

	public void setLng(float lng) {
		this.lng = lng;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	// Category
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
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the userId
	 */
	/**
	 * @param userId
	 *            the userId to set
	 */
	public int getCookie() {
		return cookie;
	}

	public void setCookie(int cookie) {
		this.cookie = cookie;
	}

}
