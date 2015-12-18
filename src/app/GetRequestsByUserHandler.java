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
		
		GetRequestsByUserReqObj r = (GetRequestsByUserReqObj) req;
		
		
		//TODO: Core of the processing takes place here
		
		//Create the response
		GetRequestsByUserResObj response=new GetRequestsByUserResObj();
		
		//Populate the response
		/*
		response.setTitle(myItem.GetRequestsByUser_name());
		response.setDesc(myItem.GetRequestsByUser_desc());
		response.setCategoryId(myItem.GetRequestsByUser_category());
		response.setUserId(myItem.GetRequestsByUser_user_id());
		response.setLeaseTerm(myItem.GetRequestsByUser_lease_term());
		response.setLeaseValue(myItem.GetRequestsByUser_lease_value());
		response.setCookie(cookie);
		*/
		
		//return the response
		return response;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
	}

}
