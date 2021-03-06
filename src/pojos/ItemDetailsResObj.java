package pojos;

public class ItemDetailsResObj extends ResObj {

	private String title, category, description, userId, leaseTerm, status, primaryImageLink, message, uid, locality,
			sublocality, userName;
	int id, leaseValue, code, surcharge;

	String[] imageLinks;

	public String getTitle() {
		return title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getLeaseTerm() {
		return leaseTerm;
	}

	public void setLeaseTerm(String leaseTerm) {
		this.leaseTerm = leaseTerm;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPrimaryImageLink() {
		return primaryImageLink;
	}

	public void setPrimaryImageLink(String primaryImageLink) {
		this.primaryImageLink = primaryImageLink;
	}

	public String[] getImageLinks() {
		return imageLinks;
	}

	public void setImageLinks(String[] imageLinks) {
		this.imageLinks = imageLinks;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLeaseValue() {
		return leaseValue;
	}

	public void setLeaseValue(int leaseValue) {
		this.leaseValue = leaseValue;
	}

	public int getSurcharge() {
		return surcharge;
	}

	public void setSurcharge(int surcharge) {
		this.surcharge = surcharge;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public String getSublocality() {
		return sublocality;
	}

	public void setSublocality(String sublocality) {
		this.sublocality = sublocality;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}
