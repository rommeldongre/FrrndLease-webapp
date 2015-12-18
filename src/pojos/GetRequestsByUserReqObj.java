package pojos;

import javax.validation.constraints.NotNull;

public class GetRequestsByUserReqObj extends ReqObj {
	// title of item
		@NotNull
		private String title;
		
		// description of item
		private String desc;
		
		// number of items, default is 1
		int quantity = 1;
		
		// category of item
		// TBD: need to change type
		@NotNull
		int categoryId;
		
		// user posting item
		// TBD: change to user id type
		@NotNull
		String userId;
		
		// Cookie
		// TBD: change to user id type
		@NotNull
		int cookie;
				
		public void setCookie(int cookie) {
			this.cookie = cookie;
		}
		
		/**
		 * @return the title
		 */
		public String getTitle() {
			return title;
		}
		/**
		 * @param title the title to set
		 */
		public void setTitle(String title) {
			this.title = title;
		}
		/**
		 * @return the desc
		 */
		public String getDesc() {
			return desc;
		}
		/**
		 * @param desc the desc to set
		 */
		public void setDesc(String desc) {
			this.desc = desc;
		}
		/**
		 * @return the quantity
		 */
		public int getQuantity() {
			return quantity;
		}
		/**
		 * @param quantity the quantity to set
		 */
		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}
		/**
		 * @return the categoryId
		 */
		public int getCategoryId() {
			return categoryId;
		}
		/**
		 * @param categoryId the categoryId to set
		 */
		public void setCategoryId(int categoryId) {
			this.categoryId = categoryId;
		}
		/**
		 * @return the userId
		 */
		public String getUserId() {
			return userId;
		}
		/**
		 * @param userId the userId to set
		 */
		public void setUserId(String userId) {
			this.userId = userId;
		}
		/**
		 * @return the userId
		 */
		public int getCookie() {
			return cookie;
		}
		/**
		 * @param userId the userId to set
		 */
	
	}
