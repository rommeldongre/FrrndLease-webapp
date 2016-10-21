package app;

import connect.Connect;
import pojos.ChangePickupStatusReqObj;
import pojos.ChangePickupStatusResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.FlsPlan;

public class ChangePickupStatusHandler extends Connect implements AppHandler{

	private FlsLogger LOGGER = new FlsLogger(ChangePickupStatusHandler.class.getName());
	
	private static ChangePickupStatusHandler instance = null;
	
	public static ChangePickupStatusHandler getInstance(){
		if(instance == null)
			instance = new ChangePickupStatusHandler();
		return instance;
	}
	
	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		
		LOGGER.info("Inside process method of ChangePickupStatusHandler");
		
		ChangePickupStatusReqObj rq = (ChangePickupStatusReqObj) req;
		ChangePickupStatusResObj rs = new ChangePickupStatusResObj();
		
		try{
			
			FlsPlan plan = new FlsPlan();
			int result = plan.changePickupStatus(rq.getLeaseId(), rq.isOwner(), rq.isPickupStatus());
			
			if(result == 1){
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
			}else{
				rs.setCode(FLS_CHANGE_PICKUP_STATUS_FAIL);
				rs.setMessage(FLS_CHANGE_PICKUP_STATUS_FAIL_M);
			}
			
		}catch(Exception e){
			e.printStackTrace();
			rs.setCode(FLS_INVALID_OPERATION);
			rs.setMessage(FLS_INVALID_OPERATION_M);
			LOGGER.warning("Error occured in ChangePickupStatus handler");
		}
		
		LOGGER.info("Finished process method of ChangePickupStatusHandler");
		return rs;
	}

	@Override
	public void cleanup() {
	}

}
