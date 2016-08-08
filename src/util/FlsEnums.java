package util;

public class FlsEnums {

	public enum Notification_Type {
		FLS_MAIL_REGISTER, // done
		FLS_MAIL_SIGNUP_VALIDATION, // done
		FLS_MAIL_POST_ITEM, // done
		FLS_MAIL_MATCH_WISHLIST_ITEM, // done
		FLS_MAIL_MATCH_POST_ITEM, // done
		FLS_MAIL_DELETE_ITEM, // done
		FLS_MAIL_MAKE_REQUEST_FROM, // done
		FLS_MAIL_MAKE_REQUEST_TO, // done
		FLS_MAIL_GRANT_REQUEST_FROM, // same as grant lease from
		FLS_MAIL_GRANT_REQUEST_TO, // same as grant lease to
		FLS_MAIL_REJECT_REQUEST_FROM, // working partially. Set flag for lease
										// part(done)
		FLS_MAIL_REJECT_REQUEST_TO, // not done as pojo needs to be filled
		FLS_MAIL_DELETE_REQUEST_FROM, // done
		FLS_MAIL_DELETE_REQUEST_TO, // done
		FLS_MAIL_ADD_FRIEND_FROM, // done
		FLS_MAIL_ADD_FRIEND_TO, // done
		FLS_MAIL_DELETE_FRIEND_FROM, // done
		FLS_MAIL_DELETE_FRIEND_TO, // done
		FLS_MAIL_GRANT_LEASE_FROM, // done
		FLS_MAIL_GRANT_LEASE_TO, // done
		FLS_MAIL_REJECT_LEASE_FROM, // not done pojo needs to be filled //done
		FLS_MAIL_REJECT_LEASE_TO,
		FLS_MAIL_FORGOT_PASSWORD, //
		FLS_MAIL_GRACE_PERIOD_OWNER,
		FLS_MAIL_GRACE_PERIOD_REQUESTOR,
		FLS_MAIL_RENEW_LEASE_OWNER,
		FLS_MAIL_RENEW_LEASE_REQUESTOR
	}
	
	public enum Event_Type {
		FLS_EVENT_NOT_NOTIFICATION,
		FLS_EVENT_NOTIFICATION,
		FLS_EVENT_CHAT
	}
	
	public enum Read_Status {
		FLS_READ,
		FLS_UNREAD
	}
	
	public enum Delivery_Status {
		FLS_DELIVERED,
		FLS_UNDELIVERED
	}
	
	public enum Archived {
		FLS_ACTIVE,
		FLS_ARCHIVED
	}

}