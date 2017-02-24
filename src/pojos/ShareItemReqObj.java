package pojos;

public class ShareItemReqObj extends ReqObj{
	
	String userId,userName,
		   accessToken,itemTitle,itemUid,itemOwnerId;
	int itemId;
	boolean friendsStatus;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getItemTitle() {
		return itemTitle;
	}
	public void setItemTitle(String itemTitle) {
		this.itemTitle = itemTitle;
	}
	public String getItemUid() {
		return itemUid;
	}
	public void setItemUid(String itemUid) {
		this.itemUid = itemUid;
	}
	public String getItemOwnerId() {
		return itemOwnerId;
	}
	public void setItemOwnerId(String itemOwnerId) {
		this.itemOwnerId = itemOwnerId;
	}
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public boolean isFriendsStatus() {
		return friendsStatus;
	}
	public void setFriendsStatus(boolean friendsStatus) {
		this.friendsStatus = friendsStatus;
	}
}
