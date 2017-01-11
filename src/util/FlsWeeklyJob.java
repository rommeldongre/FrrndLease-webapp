package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import util.Event.Event_Type;
import util.Event.Notification_Type;
import connect.Connect;

public class FlsWeeklyJob extends Connect implements org.quartz.Job{

	private FlsLogger LOGGER = new FlsLogger(FlsWeeklyJob.class.getName());

	private String URL = FlsConfig.prefixUrl;
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		
		LOGGER.warning("Its Saturday 11AM, Starting FlsWeeklyJob...");
		checkIdProof();
	}

    private void checkIdProof(){
		
		LOGGER.info("Send reminder to upload Id Proof");
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement ps1 = null,ps2=null,ps3=null,ps4=null;
		ResultSet rs1 = null,rs2=null,rs3=null;
		String next_reminder_date=null;
		
		Calendar currentCal = Calendar.getInstance();
		SimpleDateFormat currentSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  		String currentDate = currentSdf.format(currentCal.getTime());
		
		try {
			
			String sqlSelectUsersToRemind = "SELECT * FROM `users` WHERE user_photo_id IS NULL";
			ps1 = hcp.prepareStatement(sqlSelectUsersToRemind);
			rs1 = ps1.executeQuery();
			
			String sqlSelectPrimePlaces = "SELECT * FROM `places`";
			ps2 = hcp.prepareStatement(sqlSelectPrimePlaces);
			rs2 = ps2.executeQuery();
			
			String sqlSelectPhotoIdDate = "SELECT * FROM `config` WHERE option=?";
			ps3 = hcp.prepareStatement(sqlSelectPhotoIdDate);
			ps3.setString(1, "photo_id_reminder");
			rs3 = ps3.executeQuery();
			
			while(rs3.next()){
				next_reminder_date = rs3.getString("value");
			}
			
			LOGGER.info("Next date :"+next_reminder_date+" Current Date :"+currentDate);
			if(currentSdf.parse(currentDate).before(currentSdf.parse(next_reminder_date))){
				LOGGER.warning("Current date is before the next time stamp date");
				return;	
			}
			
			ArrayList<String> places =new ArrayList<String>();
			
			while(rs2.next()){
				places.add(rs2.getString("locality").toUpperCase());
			}
			
			while(rs1.next()){
				LOGGER.info("Sending a reminder to the user about uploading photo id");
				if(places.contains(rs1.getString("user_locality").toUpperCase())){
					try {
						Event event = new Event();
						event.createEvent(rs1.getString("user_id"), rs1.getString("user_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_REMIND_PHOTO_ID, 0, "You are in a Prime location! This means that you can avail of Prime delivery from FrrndLease, by having a valid photo id stored with us! You can always choose to not get delivery through FrrndLease. Upload your Photo Id now!!");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}
			
			String sqlAddNextPhotoIdDate = "UPDATE `config` SET `value`=(CURRENT_TIMESTAMP + INTERVAL 192 DAY_HOUR) WHERE option=?";
			ps4 = hcp.prepareStatement(sqlAddNextPhotoIdDate);
			ps4.setString(1, "photo_id_reminder");
			ps4.executeUpdate();
			
		}catch(SQLException e){
			LOGGER.warning("Error with the mysql operation in checkIdProof");
			e.printStackTrace();
		}catch(Exception e){
			LOGGER.warning("Exception Occured");
			e.printStackTrace();
		}finally{
			try{
				if(rs1 != null) rs1.close();
				if(rs2 != null) rs2.close();
				if(rs3 != null) rs3.close();
				if(ps1 != null) ps1.close();
				if(ps2 != null) ps2.close();
				if(ps3 != null) ps3.close();
				if(ps4 != null) ps4.close();
				if(hcp != null) hcp.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
