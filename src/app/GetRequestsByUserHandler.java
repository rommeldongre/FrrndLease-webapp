package app;

import pojos.GetRequestsByUserReqObj;
import pojos.GetRequestsByUserResObj;
import pojos.ReqObj;
import pojos.ResObj;

public class GetRequestsByUserHandler implements AppHandler {

	private static GetRequestsByUserHandler instance = null;

	public static GetRequestsByUserHandler getInstance() {
		if (instance == null)
			instance = new GetRequestsByUserHandler();
		return instance;
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		
		GetRequestsByUserReqObj rq = (GetRequestsByUserReqObj) req;
		
		
		//TODO: Core of the processing takes place here
		
		//Create the response
		GetRequestsByUserResObj rs = new GetRequestsByUserResObj();
		
		//Populate the response
		rs.setTitle("Dummy");
		rs.setDesc("More Dummy");
		rs.setUserId(rq.getUserId());
		rs.setCookie(rq.getCookie() + 1);
		
		//return the response
		return rs;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
	}

}
