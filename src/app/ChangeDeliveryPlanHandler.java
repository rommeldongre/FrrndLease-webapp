package app;

import connect.Connect;
import pojos.ChangeDeliveryPlanReqObj;
import pojos.ChangeDeliveryPlanResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.FlsPlan;
import util.OAuth;
import util.FlsPlan.Delivery_Plan;

public class ChangeDeliveryPlanHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(ChangeDeliveryPlanHandler.class.getName());

	private static ChangeDeliveryPlanHandler instance = null;

	public static ChangeDeliveryPlanHandler getInstance() {
		if (instance == null)
			instance = new ChangeDeliveryPlanHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		
		LOGGER.info("Inside process method of ChangeDeliveryPlanHandler");
		
		ChangeDeliveryPlanReqObj rq = (ChangeDeliveryPlanReqObj) req;
		ChangeDeliveryPlanResObj rs = new ChangeDeliveryPlanResObj();
		
		try{
			
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if(!oauthcheck.equals(rq.getUserId())){
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}
			
			FlsPlan plan = new FlsPlan();
			int result = plan.changeDeliveryPlan(rq.getLeaseId(), Delivery_Plan.valueOf(rq.getDeliveryPlan()));
			
			if(result == 1){
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
			}else{
				rs.setCode(FLS_DELIVERY_STATUS_FAIL);
				rs.setMessage(FLS_DELIVERY_STATUS_FAIL_M);
			}
			
		}catch(Exception e){
			e.printStackTrace();
			rs.setCode(FLS_INVALID_OPERATION);
			rs.setMessage(FLS_INVALID_OPERATION_M);
			LOGGER.warning("Error occured in ChangeDeliveryPlan handler");
		}
		
		return rs;
	}

	@Override
	public void cleanup() {
	}

}
