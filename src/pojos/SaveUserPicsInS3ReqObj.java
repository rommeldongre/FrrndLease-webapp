package pojos;

public class SaveUserPicsInS3ReqObj extends ReqObj {

	String userId, accessToken, image, existingLink;
	boolean profile;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getExistingLink() {
		return existingLink;
	}

	public void setExistingLink(String existingLink) {
		this.existingLink = existingLink;
	}

	public boolean isProfile() {
		return profile;
	}

	public void setProfile(boolean profile) {
		this.profile = profile;
	}

}
