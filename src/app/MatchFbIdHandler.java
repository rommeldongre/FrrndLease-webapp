package app;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import connect.Connect;
import pojos.MatchFbIdReqObj;
import pojos.MatchFbIdResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.OAuth;

public class MatchFbIdHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(MatchFbIdHandler.class.getName());
	
	private static MatchFbIdHandler instance = null;

	public static MatchFbIdHandler getInstance() {
		if (instance == null)
			instance = new MatchFbIdHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside process method of Add Fb Id Handler");
		
		MatchFbIdReqObj rq = (MatchFbIdReqObj) req;
		MatchFbIdResObj rs = new MatchFbIdResObj();
		
		try {
			
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if(!oauthcheck.equals(rq.getUserId())){
				LOGGER.warning("OAuth failed");
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}
			
			// Grab the Scheduler instance from the Factory
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			JobKey matchjobKey = JobKey.jobKey("FlsMatchFbIdJob", "FlsMatchFbIdGroup");
			scheduler.triggerJob(matchjobKey);
			
			
			rs.setCode(FLS_SUCCESS);
			rs.setMessage(FLS_SUCCESS_M);
		} catch(SchedulerException e){
			LOGGER.warning("not able to get scheduler");
			e.printStackTrace();
		} catch (NullPointerException e) {
			rs.setCode(FLS_NULL_POINT);
			rs.setMessage(FLS_NULL_POINT_M);
			e.printStackTrace();
		}
		
		LOGGER.info("Finished process method of Add Fb Id handler");
	
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
	}
}
