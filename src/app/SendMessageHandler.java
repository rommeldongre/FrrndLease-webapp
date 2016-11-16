package app;

import connect.Connect;
import pojos.ReqObj;
import pojos.ResObj;
import pojos.SendMessageReqObj;
import pojos.SendMessageResObj;
import util.FlsLogger;
import util.Message;
import util.OAuth;

public class SendMessageHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(SendMessageHandler.class.getName());
	
	private static SendMessageHandler instance = null;

	public static SendMessageHandler getInstance() {
		if (instance == null)
			instance = new SendMessageHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside process method of Send Message Handler");
		
		SendMessageReqObj rq = (SendMessageReqObj) req;
		SendMessageResObj rs = new SendMessageResObj();

		try {
			
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if(!oauthcheck.equals(rq.getUserId())){
				LOGGER.warning("OAuth failed");
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}
			
			if(rq.getMessage() != null){
				
				LOGGER.info("Sending message from : " + rq.getFrom() + " to : " + rq.getTo());
				Message message = new Message(rq.getItemId());
				int response = message.sendMessage(rq.getFrom(), rq.getTo(), rq.getSubject(), rq.getMessage());

				if(response == 1){
					rs.setCode(FLS_SUCCESS);
					rs.setMessage(FLS_SUCCESS_M);
				}else{
					rs.setCode(FLS_MESSAGE_NOT_SENT);
					rs.setMessage(FLS_MESSAGE_NOT_SENT_M);
				}
				
			}else{
				rs.setCode(FLS_INVALID_MESSAGE);
				rs.setMessage(FLS_INVALID_MESSAGE_M);
			}
		
		} catch (NullPointerException e) {
			LOGGER.warning("Null Pointer Exception in Send Message App Handler");
			rs.setCode(FLS_NULL_POINT);
			rs.setMessage(FLS_INVALID_MESSAGE_M);
			e.printStackTrace();
		} catch(Exception e){
			LOGGER.warning("not able to get scheduler inside Send Message App Handler");
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_INVALID_MESSAGE_M);
			e.printStackTrace();
		}
		
		LOGGER.info("Finished process method of Send Message handler");
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
	}
}
