package app;

import connect.Connect;
import pojos.ChangeUserNotificationReqObj;
import pojos.ChangeUserNotificationResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.Event;
import util.Event.User_Notification;
import util.FlsLogger;
import util.OAuth;

public class ChangeUserNotificationHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(ChangeUserNotificationHandler.class.getName());

	private static ChangeUserNotificationHandler instance = null;

	public static ChangeUserNotificationHandler getInstance() {
		if (instance == null)
			instance = new ChangeUserNotificationHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		ChangeUserNotificationReqObj rq = (ChangeUserNotificationReqObj) req;
		ChangeUserNotificationResObj rs = new ChangeUserNotificationResObj();

		try {

			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if(!oauthcheck.equals(rq.getUserId())){
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}
			
			Event event = new Event();
			int result = event.changeUserNotification(rq.getUserId(), User_Notification.valueOf(rq.getNotification()), rq.getPeriodicUpdate());

			if (result == 1) {
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
			} else {
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
			}

		} catch (Exception e) {
			e.printStackTrace();
			rs.setCode(FLS_INVALID_OPERATION);
			rs.setMessage(FLS_INVALID_OPERATION_M);
			LOGGER.warning("Error occured in changeUserNotification handler");
		}

		return rs;
	}

	@Override
	public void cleanup() {
	}

}
