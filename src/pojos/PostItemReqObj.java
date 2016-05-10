package pojos;

import javax.validation.constraints.NotNull;

public class PostItemReqObj extends ReqObj {
	
		// id of item
		@NotNull
		int id;
		
		//Title of Item
		String title;
		
		//Description of Item
		String description;
		
		//Category of Item
		String category;
		
		//UserId of Item
		String userId;
		
		//leaseValue of Item
		int leaseValue;
		
		//LeaseTerm of Item
		String leaseTerm;
		
		//Status of Item
		String status;
		
		//Image of Item
		String image;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public int getLeaseValue() {
			return leaseValue;
		}

		public void setLeaseValue(int leaseValue) {
			this.leaseValue = leaseValue;
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

		public String getImage() {
			return image;
		}

		public void setImage(String image) {
			this.image = image;
		}
	}
