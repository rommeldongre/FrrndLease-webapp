package pojos;

public class GetItemTimelineReqObj extends ReqObj{
	
	// item id 
	int itemId;

	// Cookie
	int cookie;
	
	// limit till which to get the items
	int limit;

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
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
