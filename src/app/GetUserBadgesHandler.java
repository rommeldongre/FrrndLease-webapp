package app;

import connect.Connect;
import pojos.GetUserBadgesReqObj;
import pojos.GetUserBadgesResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsBadges;
import util.FlsLogger;

public class GetUserBadgesHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetUserBadgesHandler.class.getName());

	private static GetUserBadgesHandler instance = null;

	public static GetUserBadgesHandler getInstance() {
		if (instance == null)
			instance = new GetUserBadgesHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside Process Method of GetUserBadgesHandler");
		
		GetUserBadgesReqObj rq = (GetUserBadgesReqObj) req;
		GetUserBadgesResObj rs = new GetUserBadgesResObj();

		FlsBadges badges = new FlsBadges(rq.getUserId());
		rs = badges.getBadges();

		LOGGER.info("Finished Process Method");
		return rs;

	}

	@Override
	public void cleanup() {
	}

}
