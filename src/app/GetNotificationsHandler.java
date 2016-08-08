package app;

import connect.Connect;
import pojos.GetNotificationsListResObj;
import pojos.GetNotificationsReqObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.Event;
import util.FlsLogger;

public class GetNotificationsHandler extends Connect implements AppHandler {

	FlsLogger LOGGER = new FlsLogger(GetNotificationsHandler.class.getName());
	
	private static GetNotificationsHandler instance = null;
	
	public static GetNotificationsHandler getInstance(){
		if(instance == null)
			instance = new GetNotificationsHandler();
		return instance;
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		
		GetNotificationsReqObj rq = (GetNotificationsReqObj) req;
		GetNotificationsListResObj rs = new GetNotificationsListResObj();
		
		Event event = new Event();
		rs = event.getNotifications(rq.getUserId(), rq.getLimit(), rq.getOffset());
		
		return rs;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

}
