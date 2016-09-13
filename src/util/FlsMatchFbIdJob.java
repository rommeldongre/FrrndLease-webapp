package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;

import org.quartz.*;

@DisallowConcurrentExecution
public class FlsMatchFbIdJob extends Connect implements org.quartz.Job {
	
	private FlsLogger LOGGER = new FlsLogger(FlsMatchFbIdJob.class.getName());

    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("Fls Match Fb Id Job Started");
        try {
        		fbIdTask();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("Fls Match Fb Id Job Failed");
		} finally{
			LOGGER.info("Fls Match Fb Id Job Ended");
		}
    }
	
	public void fbIdTask(){
    	
    	LOGGER.info("Inside Fb Id Task job");
    	String user_fb_id=null,user_id=null;
    	
    	Connection hcp = getConnectionFromPool();
  	    PreparedStatement psgetFbId=null;
  	    ResultSet resultFbIds =null;
  	    
  	    try {
  	    	String getFbIds ="SELECT user_id,user_fb_id FROM users WHERE user_fb_id IS NOT NULL";
  	    	psgetFbId= hcp.prepareStatement(getFbIds);
    		
  	    	resultFbIds = psgetFbId.executeQuery();
    		
    		LOGGER.info("1st Select Query for FB Ids Fired");
  	    	
    		if (!resultFbIds.next()) {
      			System.out.println("Empty result while firing select query on users table");
      			return;
      		}
    		
    		LOGGER.info("Checking Resultset if query returned anything");
    		resultFbIds.beforeFirst();
    		while (resultFbIds.next()) {
      			LOGGER.info("Result Set not Empty..Getting data one by one");
      			user_id = resultFbIds.getString("user_id");
      			user_fb_id = resultFbIds.getString("user_fb_id");
      			
      			updateFriendId(user_id,user_fb_id);
      			
    		}
			
  	    }catch (SQLException e) {
			// TODO: handle exception
			LOGGER.warning("SQL Exception Occured in Fb Id Task Method");
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			try {
				if(resultFbIds != null)resultFbIds.close();
				if(psgetFbId != null)psgetFbId.close();
				
				if(hcp != null)hcp.close();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
    	
    }
    
    private void updateFriendId(String User_id,String Fb_id){
    	
    	LOGGER.info("Inside Update Friend Id Method");
  	    Connection hcp = getConnectionFromPool();
  	    PreparedStatement psupdateFbId=null;
  	    int renewFbIdAction = 0;
	   
	    try {
	    	hcp.setAutoCommit(false);
	    	String UpdateFriendUserIdql = "UPDATE `friends` SET friend_id=?, friend_fb_id=? WHERE friend_id=?";
			
	    	psupdateFbId = hcp.prepareStatement(UpdateFriendUserIdql);
			
			LOGGER.info("Statement created. Executing renew query ...");
			psupdateFbId.setString(1, User_id);
			psupdateFbId.setString(2, Fb_id);
			psupdateFbId.setString(3, Fb_id);
			renewFbIdAction = psupdateFbId.executeUpdate();
			
			if(renewFbIdAction == 0 ){
				LOGGER.warning("Error occured while firing Update query on friends table for Updating friend ID");
				hcp.rollback();
				return;
			}
			
			hcp.commit();
	    }catch (SQLException e) {
			// TODO: handle exception
			LOGGER.warning("SQL Exception Occured in Fb Id Task Method");
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			try {
				if(psupdateFbId != null)psupdateFbId.close();
				if(hcp != null)hcp.close();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
    	
    }
}
	