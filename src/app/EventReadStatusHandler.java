package app;

import connect.Connect;
import pojos.EventReadStatusReqObj;
import pojos.EventReadStatusResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.Event;
import util.FlsLogger;
import util.OAuth;

public class EventReadStatusHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(EventReadStatusHandler.class.getName());

	private static EventReadStatusHandler instance = null;

	public static EventReadStatusHandler getInstance() {
		if (instance == null)
			instance = new EventReadStatusHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		
		EventReadStatusReqObj rq = (EventReadStatusReqObj) req;
		EventReadStatusResObj rs = new EventReadStatusResObj();
		
		OAuth oauth = new OAuth();
		String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
		if(!oauthcheck.equals(rq.getUserId())){
			rs.setCode(FLS_ACCESS_TOKEN_FAILED);
			rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
			return rs;
		}
		
		Event event = new Event();
		int result = event.changeReadStatus(rq.getEventId(), rq.getReadStatus());
		
		if(result == 1){
			rs.setCode(FLS_SUCCESS);
			rs.setMessage(FLS_SUCCESS_M);
			LOGGER.info("Event Read Status Changed!!");
		}else{
			rs.setCode(FLS_INVALID_OPERATION);
			rs.setMessage(FLS_INVALID_OPERATION_M);
			LOGGER.warning("Event Read Status Failed");
		}

		return rs;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

}
