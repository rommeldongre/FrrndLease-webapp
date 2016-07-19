package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import app.GrantLeaseHandler;
import app.RenewLeaseHandler;
import util.AwsSESEmail;
import connect.Connect;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

import org.quartz.*;

import pojos.RenewLeaseReqObj;


public class FlsJob extends Connect implements org.quartz.Job {

      public FlsJob() {
      }

      public void execute(JobExecutionContext context) throws JobExecutionException {
          System.err.println("Its Showtime!!  FlsJob is now executing ...");
          
          System.out.println("Lease Task job Called");
          leasetask();
      }
      
      public void leasetask(){
    	  
    	System.out.println("Inside Lease Task job");
  		String lease_requser_id=null,lease_user_id=null,lease_expiry_date=null,term,date;
  	    int user_credit=0,gracedays = 5,lease_item_id=0,days=0;
  	    Calendar gracePeroidCal = Calendar.getInstance();
  	    
  	    PreparedStatement psgetLeases=null,psRenewUpdate=null,psCloseUpdate=null,psItemSelect=null,psItemUpdate=null,psStoreUpdate=null,psAddCredit=null,psDebitCredit=null;
  	    ResultSet resultLeases =null,dbResponseitems=null;
		SimpleDateFormat sdfCal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		GrantLeaseHandler GLH = new GrantLeaseHandler();
		RenewLeaseHandler RLH = new RenewLeaseHandler();
		LogCredit lc = new LogCredit();
  	    
    	  try {
    		  getConnection();
    		  System.out.println("After getting connection");
    		  String getLeases =" SELECT tb1.lease_requser_id, tb1.lease_item_id, tb1.lease_user_id, tb1.lease_status, tb1.lease_expiry_date, tb2.user_credit FROM leases tb1 INNER JOIN users tb2 on tb1.lease_requser_id = tb2.user_id WHERE tb1.lease_status=?";
    		  psgetLeases= connection.prepareStatement(getLeases);
    		  psgetLeases.setString(1, "Active");
    		  
    		  resultLeases = psgetLeases.executeQuery();
    		  
    		  System.out.println("Query Fired");
    		  if (!resultLeases.next()) {
  				System.out.println("Empty result while firing select query on lease table");
  				return;
  			  }
    		  
    		  if (resultLeases.isBeforeFirst()) {
  				while (resultLeases.next()) {
  					System.out.println("Result Set not Empty..Getting data one by one");
  					lease_requser_id = resultLeases.getString("lease_requser_id");
  					lease_user_id = resultLeases.getString("lease_user_id");
  					lease_item_id = resultLeases.getInt("lease_item_id");
  					lease_expiry_date = resultLeases.getString("lease_expiry_date");
  					user_credit = resultLeases.getInt("user_credit");
  					
  				    if(RLH.checkGracePeroid(lease_expiry_date,"Lease")){
  				    	System.out.println("Inside check block for 5 day period");
  				    	try {
  				    		AwsSESEmail newE = new AwsSESEmail();
  				    		System.out.println("Inside Block to send Grace Period Notification");
  							// newE.send(lease_user_id, FlsSendMail.Fls_Enum.FLS_MAIL_GRANT_LEASE_FROM, rq);
  						    //newE.send(rq.getReqUserId(), FlsSendMail.Fls_Enum.FLS_MAIL_GRANT_LEASE_TO, rq);
  						} catch (Exception e) {
  								e.printStackTrace();
  						}
  					}else{
  						System.out.println("Grace Condition is false ...");
  					    if(user_credit >10){
  					    	term = GLH.getLeaseTerm(lease_item_id);
  							days = GLH.getDuration(term);
  								
  							try {
  								gracePeroidCal.setTime(sdfCal.parse(lease_expiry_date));
  							} catch (ParseException e2) {
  								e2.printStackTrace();
  								System.out.println("Error parsing Date info inside Renew Lease ...");
  							}
  								gracePeroidCal.add(Calendar.DATE, days);
  								 date = sdfCal.format(gracePeroidCal.getTime());
  								
  								String UpdateRenewLeasesql = "UPDATE`leases` SET lease_expiry_date=? WHERE lease_requser_id=? AND lease_item_id=? AND lease_status =?"; //
  								
  								psRenewUpdate = connection.prepareStatement(UpdateRenewLeasesql);
  								
  								System.out.println("Statement created. Executing renew query ...");
  								psRenewUpdate.setString(1, date);
  								psRenewUpdate.setString(2, lease_requser_id);
  								psRenewUpdate.setInt(3, lease_item_id);
  								psRenewUpdate.setString(4, "Active");
  								
  								int renewAction =0;
  								renewAction = psRenewUpdate.executeUpdate();
  								
  								if(renewAction == 0 ){
  									System.out.println("Error occured while firing Update query on lease table for Renew Lease");
  									return;
  								}
  								
  							// add credit to user giving item on lease
  								String sqlAddCredit = "UPDATE users SET user_credit=user_credit+10 WHERE user_id=?";
  								psAddCredit = connection.prepareStatement(sqlAddCredit);
  								psAddCredit.setString(1, lease_user_id);
  								int addCredit=0;
  								addCredit = psAddCredit.executeUpdate();
  								
  								if(addCredit == 0){
  									System.out.println("Error occured while firing 1st update credit query on users table");
  									return;
  								}
  								
  								
  								lc.addLogCredit(lease_user_id,10,"Lease Renewed","");
  								
  								// subtract credit from user getting a lease
  								String sqlSubCredit = "UPDATE users SET user_credit=user_credit-10 WHERE user_id=?";
  								psDebitCredit = connection.prepareStatement(sqlSubCredit);
  								psDebitCredit.setString(1, lease_requser_id);
  								int subCredit =0;
  								subCredit = psDebitCredit.executeUpdate();

  								if(subCredit == 0){
  									System.out.println("Error occured while firing 2nd update credit query on users");
  									return;
  								}
  								
  								lc.addLogCredit(lease_requser_id,-10,"Lease Renewed","");
  								
  							}else{
  								
  								String CloseRenewLeasesql = "UPDATE`leases` SET lease_status=? WHERE lease_requser_id=? AND lease_item_id=? AND lease_status =?"; //
  								
  								psCloseUpdate = connection.prepareStatement(CloseRenewLeasesql);
  								psCloseUpdate.setString(1, "Archived");
  								psCloseUpdate.setString(2, lease_requser_id);
  								psCloseUpdate.setInt(3, lease_item_id);
  								psCloseUpdate.setString(4, "Active");
  								
  								int leaseAction=0;
  								leaseAction = psCloseUpdate.executeUpdate();
  								
  								if(leaseAction == 0){
  									System.out.println("Error occured while firing edit query on lease table");
  									return;
  								}
  								
  								String selectItemSql = "SELECT * FROM items WHERE item_id=?";
  								psItemSelect = connection.prepareStatement(selectItemSql);
  								psItemSelect.setInt(1,lease_item_id);
  								dbResponseitems = psItemSelect.executeQuery();
  								
  								if(!dbResponseitems.next()) {
  									System.out.println("Empty result while firing select query on 2nd table(items)");
  							        return;
  								}
  								
  								String updateItemsSql = "UPDATE items SET item_status=? WHERE item_id=?";
  								psItemUpdate = connection.prepareStatement(updateItemsSql);

  								System.out.println("Statement created. Executing update query on items table...");
  								psItemUpdate.setString(1, "InStore");
  								psItemUpdate.setInt(1,lease_item_id);
  								int itemAction=0;
  								itemAction= psItemUpdate.executeUpdate();
  								
  								if(itemAction == 0){
  									System.out.println("Error occured while firing update query on items table");
  					                return;
  								}
  								
  								String insertStoreSql = "insert into store (store_item_id) values (?)"; //
  								System.out.println("Creating insert statement store table.....");
  								psStoreUpdate = connection.prepareStatement(insertStoreSql);

  								System.out.println("Statement created. Executing update query on store table.....");
  								psStoreUpdate.setInt(1,lease_item_id);
  								int storeAction=0;
  								storeAction = psStoreUpdate.executeUpdate();
  								
  								if(storeAction == 0){
  									System.out.println("Error occured while firing update query on store table");
  									return;
  								}
  								
  								AwsSESEmail newE = new AwsSESEmail();
  								RenewLeaseReqObj rq = new RenewLeaseReqObj();
  								rq.setItemId(lease_item_id);
  								rq.setFlag("close");
  								rq.setReqUserId(lease_requser_id);
  								rq.setUserId(lease_user_id);
  								newE.send(lease_user_id, FlsSendMail.Fls_Enum.FLS_MAIL_REJECT_LEASE_FROM, rq);
  								newE.send(lease_requser_id, FlsSendMail.Fls_Enum.FLS_MAIL_REJECT_LEASE_TO, rq);

  							}
  						}
  				}
    		  }
  				
		}catch (SQLException e) {
			// TODO: handle exception
			System.out.println("SQL Exception Occured");
			System.out.println(e.getStackTrace());
		}catch (Exception e){
			e.printStackTrace();
			System.out.println(e.getStackTrace());
		}finally {
			try {
				// close and reset connection to null
				
				if(resultLeases != null)resultLeases.close();
				if(dbResponseitems != null)dbResponseitems.close();
				
				if(psgetLeases != null)psgetLeases.close();
				if(psRenewUpdate != null)psRenewUpdate.close();
				if(psCloseUpdate != null)psCloseUpdate.close();
				if(psAddCredit != null)psAddCredit.close();
				if(psDebitCredit != null)psDebitCredit.close();
				
				if(psItemSelect != null)psItemSelect.close();
				if(psItemUpdate != null)psItemUpdate.close();
				if(psStoreUpdate != null)psStoreUpdate.close();
				
				if(connection != null)connection.close();
			
				connection = null;
			} catch (SQLException e){
				e.printStackTrace();
				System.out.println(e.getStackTrace());
			}
		}
    	 
      }
}
	