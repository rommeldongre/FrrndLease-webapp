package app;

import connect.Connect;
import pojos.GetReportReqObj;
import pojos.GetReportResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;
import util.FlsReports;

public class GetReportHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetReportHandler.class.getName());

	private static GetReportHandler instance = null;

	public static GetReportHandler getInstance() {
		if (instance == null)
			instance = new GetReportHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside process method of GetReportHandler");

		GetReportReqObj rq = (GetReportReqObj) req;
		GetReportResObj rs = new GetReportResObj();
		
		FlsReports reports = new FlsReports();
		
		rs = reports.generateReport(rq);

		return rs;
	}

	@Override
	public void cleanup() {
	}

}
