package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import com.mysql.jdbc.MysqlErrorNumbers;

import connect.Connect;
import pojos.MatchFbIdReqObj;
import pojos.MatchFbIdResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.Event;
import util.Event.Event_Type;
import util.Event.Notification_Type;
import util.FlsConfig;
import util.FlsLogger;
import util.LogCredit;
import util.LogItem;
import util.OAuth;

public class MatchFbIdHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(MatchFbIdHandler.class.getName());

	private String URL = FlsConfig.prefixUrl;
	
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
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null, ps2 = null;
		ResultSet rs1 = null;
		int rs2=0;

		
		try {
			
			/*OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if(!oauthcheck.equals(rq.getUserId())){
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}*/
			
			LOGGER.info("Creating statement for selecting users lat lng.....");
			/*String sqlCheckFbId = "SELECT * FROM users WHERE user_fb_id=?";
			ps1 = hcp.prepareStatement(sqlCheckFbId);
			ps1.setString(1, rq.getUserFbId());
			rs1 = ps1.executeQuery();
			
			if(!rs1.next()){
			hcp.setAutoCommit(false);
			
			LOGGER.info("Creating statement to insert Fb it......");
			String sqlInsertFbId = "UPDATE `users` SET user_fb_id=? WHERE user_id=?";
			ps2 = hcp.prepareStatement(sqlInsertFbId);
			ps2.setString(1, rq.getUserFbId());
			ps2.setString(2, rq.getUserId());
			rs2 = ps2.executeUpdate();
			//LOGGER.info("Result of insertion query : " + rs2);
			
			if(rs2==0){
				LOGGER.info("Error while adding Fb Id");
				hcp.rollback();
			}
		
			hcp.commit();
			rs.setCode(FLS_SUCCESS);
			rs.setMessage(FLS_SUCCESS_M);
			}else{
				rs.setCode(FLS_DUPLICATE_ENTRY);
				rs.setMessage(FLS_DUPLICATE_ENTRY_FB);
			}*/
			
			// Grab the Scheduler instance from the Factory
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			JobKey matchjobKey = JobKey.jobKey("FlsMatchFbIdJob", "FlsMatchFbIdGroup");
			scheduler.triggerJob(matchjobKey);
			
			rs.setCode(FLS_SUCCESS);
			rs.setMessage(FLS_SUCCESS_M);
		} catch(SchedulerException e){
			LOGGER.warning("not able to get scheduler");
			e.printStackTrace();
		/*} catch (SQLException e) {
			LOGGER.warning("Couldnt create a statement");
				rs.setCode(FLS_SQL_EXCEPTION);
				rs.setMessage(FLS_SQL_EXCEPTION_M);
				e.printStackTrace();*/
		} catch (NullPointerException e) {
			rs.setCode(FLS_NULL_POINT);
			rs.setMessage(FLS_NULL_POINT_M);
			e.printStackTrace();
		} finally {
			try {
				if(rs1 != null)rs1.close();
				if(ps1 != null)ps1.close();
				if(ps2 != null)ps2.close();
				if(hcp != null)hcp.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		LOGGER.info("Finished process method of Add Fb Id handler");
	
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
	}
}
