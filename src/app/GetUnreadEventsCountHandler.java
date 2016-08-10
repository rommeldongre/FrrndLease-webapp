package app;

import connect.Connect;
import pojos.GetUnreadEventsCountReqObj;
import pojos.GetUnreadEventsCountResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.Event;
import util.FlsLogger;

public class GetUnreadEventsCountHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetUnreadEventsCountHandler.class.getName());
	
	private static GetUnreadEventsCountHandler instance = null;
	
	public static GetUnreadEventsCountHandler getInstance(){
		if(instance == null)
			instance = new GetUnreadEventsCountHandler();
		return instance;
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		
		LOGGER.info("inside process method of GetUnreadEventsCountHandler");
		
		GetUnreadEventsCountReqObj rq = (GetUnreadEventsCountReqObj) req;
		GetUnreadEventsCountResObj rs = new GetUnreadEventsCountResObj();
		
		Event event = new Event();
		rs = event.getUnreadEventsCount(rq.getUserId());
		
		return rs;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

}
