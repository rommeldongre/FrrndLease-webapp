package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import app.GrantLeaseHandler;
import util.Event.Event_Type;
import util.Event.Notification_Type;
import util.FlsCredit.Credit;
import util.FlsPlan.Delivery_Plan;
import connect.Connect;

import org.quartz.*;

import pojos.RenewLeaseReqObj;


public class FlsJob extends Connect implements org.quartz.Job {
	
	private FlsLogger LOGGER = new FlsLogger(FlsJob.class.getName());
	private int gracetime=7;

	private String URL = FlsConfig.prefixUrl;
	
    public FlsJob() {
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.err.println("Its Showtime!!  FlsJob is now executing ...");
        
        LOGGER.info("Lease Task job Called");
        leasetask();
    }
      
   
    public void leasetask(){
    	  
    	LOGGER.info("Inside Lease Task job");
  		String lease_requser_id=null,lease_requser_name=null,lease_user_id=null,lease_expiry_date=null,term,date, item_name=null;
  	    int user_credit=0,lease_item_id=0,lease_id=0,days=0;
  	    
  	    Calendar futureCal = Calendar.getInstance();
		SimpleDateFormat futureSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		futureCal.add(Calendar.DATE, gracetime);
		String futureDate = futureSdf.format(futureCal.getTime());
		LOGGER.warning("Future Date in Query "+futureDate);
		
		
		Connection hcp = getConnectionFromPool();
  	    PreparedStatement psgetLeases=null;
  	    ResultSet resultLeases =null;
		Event event = new Event();
  	    
    	try {
    		String getLeases =" SELECT tb1.lease_requser_id, tb1.lease_id, tb1.lease_item_id, tb1.lease_user_id, tb1.lease_status, tb1.delivery_plan, tb1.lease_expiry_date, tb1.owner_pickup_status,tb1.leasee_pickup_status, tb2.user_credit,  tb2.user_full_name, tb3.item_name, tb3.item_status FROM leases tb1 INNER JOIN users tb2 on tb1.lease_requser_id = tb2.user_id INNER JOIN items tb3 ON tb1.lease_item_id = tb3.item_id WHERE tb1.lease_status=? AND tb1.lease_expiry_date< ? AND tb3.item_status IN (?,?)";
    		psgetLeases= hcp.prepareStatement(getLeases);
    		psgetLeases.setString(1, "Active");
    		psgetLeases.setString(2, futureDate);
    		psgetLeases.setString(3, "LeaseStarted");
    		psgetLeases.setString(4, "LeaseEnded");
    		
    		resultLeases = psgetLeases.executeQuery();
    		
    		LOGGER.info("1st Select Query Fired");
    		if (!resultLeases.next()) {
  			System.out.println("Empty result while firing select query on lease table");
  			return;
  			}
    		  
    		LOGGER.info("Checking Resultset if query returned anything");
    		resultLeases.beforeFirst();
  			while (resultLeases.next()) {
  				
  			LOGGER.info("Result Set not Empty..Getting data one by one");
  			lease_requser_id = resultLeases.getString("lease_requser_id");
  			lease_requser_name = resultLeases.getString("user_full_name");
  			lease_user_id = resultLeases.getString("lease_user_id");
  			item_name = resultLeases.getString("item_name");
  			lease_id = resultLeases.getInt("lease_id");
  			lease_item_id = resultLeases.getInt("lease_item_id");
  			lease_expiry_date = resultLeases.getString("lease_expiry_date");
  			user_credit = resultLeases.getInt("user_credit");
  			
  			if(resultLeases.getString("item_status").equals("LeaseStarted")){
  				LOGGER.info("Lease Started State");
  			String lease_check=null;
  			lease_check = checkGracePeroid(lease_expiry_date);
  			switch (lease_check) {
  				case "Expired":
  					closeOrRenewLease(lease_requser_id,lease_user_id,lease_item_id,lease_id,lease_expiry_date,user_credit);
  					break;
  				case "Grace":
  					sendGraceMail(lease_requser_id, lease_user_id,lease_item_id);
					break;
  				case "Not Grace":
  					break;
  				default:
  					LOGGER.warning("Method to check Grace Period returned invalid value please check");
  					break;
  				}	
  			}else if (resultLeases.getString("item_status").equals("LeaseEnded")){
				if(resultLeases.getString("delivery_plan").equals("FLS_SELF")){
					LOGGER.info("Lease Ended State");
					if(!resultLeases.getBoolean("owner_pickup_status")){
						event.createEvent(lease_requser_id, lease_user_id, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_LEASE_ENDED_OWNER, lease_item_id, "Lease has ended.Please update pick up status of your item <a href=\"" + URL + "/myapp.html#/myleasedoutitems" + "\">" + item_name + "</a> and leasee <strong>" + lease_requser_name + "</strong> on Friend Lease");
					}
					if(!resultLeases.getBoolean("leasee_pickup_status")){
						event.createEvent(lease_user_id, lease_requser_id, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_LEASE_ENDED_REQUESTOR, lease_item_id, "Lease has ended. Please update pick up status of the item you leased <a href=\"" + URL + "/myapp.html#/myleasedinitems" + "\">" + item_name + "</a> ");
					}
				}
			}else if(resultLeases.getString("item_status").equals("LeaseReady")){
				if(resultLeases.getString("delivery_plan").equals("FLS_SELF")){
					LOGGER.info("Lease Ready State");
					if(!resultLeases.getBoolean("owner_pickup_status")){
					 event.createEvent(lease_requser_id, lease_user_id, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_LEASE_READY_OWNER, lease_item_id, "Lease is ready .Please update pick up status of your item <a href=\"" + URL + "/myapp.html#/myleasedoutitems" + "\">" + item_name + "</a> and leasee <strong>" + lease_requser_name + "</strong> on Friend Lease");
					}
					if(!resultLeases.getBoolean("leasee_pickup_status")){
						event.createEvent(lease_user_id, lease_requser_id, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_LEASE_READY_REQUESTOR, lease_item_id, "Lease is ready. Please update pick up status of the item you leased <a href=\"" + URL + "/myapp.html#/myleasedinitems" + "\">" + item_name + "</a> ");
					}
				}
			}
  		 }
  			
		}catch (SQLException e) {
			// TODO: handle exception
			LOGGER.warning("SQL Exception Occured in Lease Task Method");
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			try {
				if(resultLeases != null)resultLeases.close();
				if(psgetLeases != null)psgetLeases.close();
				
				if(hcp != null)hcp.close();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
    	 
    }
      
    private void closeOrRenewLease(String lease_requser_id, String lease_user_id,int lease_item_id,int lease_id, String lease_expiry_date, int user_credit){
    	
    	LOGGER.info("Inside Expire Method");
  		String term,date,item_primary_image_link=null;
  	    int days=0;
  	    Calendar gracePeroidCal = Calendar.getInstance();
  	    
  	    Connection hcp = getConnectionFromPool();
		
  	    PreparedStatement psRenewUpdate=null,psCloseUpdate=null,psItemSelect=null,psItemUpdate=null, ps1 = null, ps2 = null;
  	    ResultSet dbResponseitems=null, rs1 = null, rs2 = null;
		SimpleDateFormat sdfCal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		GrantLeaseHandler GLH = new GrantLeaseHandler();
		FlsCredit credits = new FlsCredit();
		LogItem li = new LogItem();
  	    
    	try {
    		
    		// fetching the uid
			String sqlUid = "SELECT item_uid,item_name,item_primary_image_link FROM items WHERE item_id=?";
			ps1 = hcp.prepareStatement(sqlUid);
			ps1.setInt(1, lease_item_id);
			rs1 = ps1.executeQuery();
			String uid = null;
			String title = null;
			if(rs1.next()){
				uid = rs1.getString("item_uid");
				title = rs1.getString("item_name");
				item_primary_image_link = rs1.getString("item_primary_image_link");
			}
			
    		hcp.setAutoCommit(false);
			LOGGER.info("Grace Condition is false ...");
		    if(user_credit >=10){
		    	term = GLH.getLeaseTerm(lease_item_id);
				days = GLH.getDuration(term);
					
				try {
					gracePeroidCal.setTime(sdfCal.parse(lease_expiry_date));
				}catch (ParseException e2) {
					e2.printStackTrace();
					LOGGER.warning("Error parsing Date info inside Renew Lease ...");
				}
				
				gracePeroidCal.add(Calendar.DATE, days);
				date = sdfCal.format(gracePeroidCal.getTime());
				
				String UpdateRenewLeasesql = "UPDATE`leases` SET lease_expiry_date=? WHERE lease_requser_id=? AND lease_item_id=? AND lease_id=? AND lease_status =?"; //
				
				psRenewUpdate = hcp.prepareStatement(UpdateRenewLeasesql);
				
				LOGGER.info("Statement created. Executing renew query ...");
				psRenewUpdate.setString(1, date);
				psRenewUpdate.setString(2, lease_requser_id);
				psRenewUpdate.setInt(3, lease_item_id);
				psRenewUpdate.setInt(4, lease_id);
				psRenewUpdate.setString(5, "Active");
				
				int renewAction =0;
				renewAction = psRenewUpdate.executeUpdate();
				
				if(renewAction == 0 ){
					LOGGER.warning("Error occured while firing Update query on lease table for Renew Lease");
					hcp.rollback();
					return;
				}
					
				// add credit to user giving item on lease
				credits.logCredit(lease_user_id, 10, "Lease Renewed", "", Credit.ADD);
				// subtract credit from user getting a lease
				credits.logCredit(lease_requser_id, 10, "Lease Renewed", "", Credit.SUB);
				
				// logging item status to renewed
				li.addItemLog(lease_item_id, "Lease Renewed", "", item_primary_image_link);
				
				RenewLeaseReqObj rq = new RenewLeaseReqObj();
				rq.setItemId(lease_item_id);
				rq.setFlag("renew");
				rq.setReqUserId(lease_requser_id);
				rq.setUserId(lease_user_id);
				Event event = new Event();
				event.createEvent(lease_user_id, lease_requser_id, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_RENEW_LEASE_REQUESTOR, lease_item_id, "Lease has been renewed by the owner of item having item id <a href=\"" + URL + "/ItemDetails?uid=" + uid + "\">" + title + "</a>");
				event.createEvent(lease_requser_id, lease_user_id, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_RENEW_LEASE_OWNER, lease_item_id, "Lease has been renewed for item having id <a href=\"" + URL + "/ItemDetails?uid=" + uid + "\">" + title + "</a> and leasee <strong>" + lease_requser_id + "</strong> on Friend Lease");
					
			}else{
				
				String CloseRenewLeasesql = "UPDATE`leases` SET lease_status=? WHERE lease_requser_id=? AND lease_item_id=? AND lease_id=? AND lease_status =?"; //
				
				psCloseUpdate = hcp.prepareStatement(CloseRenewLeasesql);
				psCloseUpdate.setString(1, "Archived");
				psCloseUpdate.setString(2, lease_requser_id);
				psCloseUpdate.setInt(3, lease_item_id);
				psCloseUpdate.setInt(4, lease_id);
				psCloseUpdate.setString(5, "Active");
				
				int leaseAction=0;
				leaseAction = psCloseUpdate.executeUpdate();
				
				if(leaseAction == 0){
					LOGGER.warning("Error occured while firing edit query on lease table");
					hcp.rollback();
					return;
				}
					
				String selectItemSql = "SELECT * FROM items WHERE item_id=?";
				psItemSelect = hcp.prepareStatement(selectItemSql);
				psItemSelect.setInt(1,lease_item_id);
				dbResponseitems = psItemSelect.executeQuery();
				
				if(!dbResponseitems.next()) {
					LOGGER.warning("Empty result while firing select query on 2nd table(items)");
					hcp.rollback();
				    return;
				}
				
				String updateItemsSql = "UPDATE items SET item_status=? WHERE item_id=?";
				psItemUpdate = hcp.prepareStatement(updateItemsSql);

				LOGGER.info("Statement created. Executing update query on items table...");
				psItemUpdate.setString(1, "InStore");
				psItemUpdate.setInt(2,lease_item_id);
				int itemAction=0;
				itemAction= psItemUpdate.executeUpdate();
				
				if(itemAction == 0){
					LOGGER.warning("Error occured while firing update query on items table");
					hcp.rollback();
		            return;
				}
					
				li.addItemLog(lease_item_id, "LeaseEnded", "", item_primary_image_link);
				
				//getting lease delivery plan
				String sqlDeliveryPlan = "SELECT delivery_plan FROM leases WHERE lease_item_id=? AND lease_status=?";
				ps2 = hcp.prepareStatement(sqlDeliveryPlan);
				ps2.setInt(1, lease_item_id);
				ps2.setString(2, "Active");
				
				rs2 = ps2.executeQuery();
				
				if(rs2.next()){
					
					String deliveryPlan = rs2.getString("delivery_plan");
					
					Event event = new Event();
					
					if(Delivery_Plan.FLS_SELF.name().equals(deliveryPlan)){
						event.createEvent(lease_requser_id, lease_user_id, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_CLOSE_LEASE_FROM_SELF, lease_item_id, "You have closed leased of item <a href=\"" + URL + "/ItemDetails?uid=" + uid + "\">" + title + "</a> and leasee <strong>" + lease_requser_id + "</strong> on Friend Lease ");
						event.createEvent(lease_user_id, lease_requser_id, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_CLOSE_LEASE_TO_SELF, lease_item_id, "Lease has been closed by the Owner for the item <a href=\"" + URL + "/ItemDetails?uid=" + uid + "\">" + title + "</a> ");
					}else{
						event.createEvent("admin@frrndlease.com", "ops@frrndlease.com", Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_OPS_PICKUP_CLOSE, lease_item_id, "The lease item - " + lease_item_id + " is ready to be picked up.");
						event.createEvent("ops@frrndlease.com", "admin@frrndlease.com", Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_OPS_PICKUP_CLOSE, lease_item_id, "The lease item - " + lease_item_id + " is ready to be picked up.");
					}

				}
			}
		    
			hcp.commit();
    	}catch (SQLException e) {
  			// TODO: handle exception
  			LOGGER.warning("SQL Exception Occured in Expire Method");
  			e.printStackTrace();
  		}catch (Exception e){
  			e.printStackTrace();
  		}finally {
  			try {
  				
  				if(rs2 != null) rs2.close();
  				if(ps2 != null) ps2.close();
  				if(rs1 != null)rs1.close();
  				if(ps1 != null)ps1.close();
  				if(dbResponseitems != null)dbResponseitems.close();
  				
  				if(psRenewUpdate != null)psRenewUpdate.close();
  				if(psCloseUpdate != null)psCloseUpdate.close();
  				
  				if(psItemSelect != null)psItemSelect.close();
  				if(psItemUpdate != null)psItemUpdate.close();
  				
  				if(hcp != null)hcp.close();
  			} catch (SQLException e){
  				LOGGER.warning("Exception Encountered while closing Connections in Expire Method");
  				e.printStackTrace();
  			}catch (Exception e1){
  			e1.printStackTrace();
			}
  		}
    }

	private void sendGraceMail(String lease_requser_id, String lease_user_id,int lease_item_id) {
		LOGGER.info("Inside check block for 7 day period");
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		Connection hcp = getConnectionFromPool();
		try {
			// fetching the uid
			String sqlUid = "SELECT item_uid,item_name FROM items WHERE item_id=?";
			ps1 = hcp.prepareStatement(sqlUid);
			ps1.setInt(1, lease_item_id);
			rs1 = ps1.executeQuery();
			String uid = null;
			String title = null;
			if(rs1.next()){
				uid = rs1.getString("item_uid");
				title = rs1.getString("item_name");
			}
			
			LOGGER.info("Inside Block to send Grace Period Notification");
			RenewLeaseReqObj rlrq = new RenewLeaseReqObj();
				rlrq.setItemId(lease_item_id);
				rlrq.setFlag("renew");
				rlrq.setReqUserId(lease_requser_id);
				rlrq.setUserId(lease_user_id);
				
				Event event = new Event();
				event.createEvent(lease_requser_id, lease_user_id, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_GRACE_PERIOD_OWNER, lease_item_id, "Less than 5 days left for lease to close.Please consider renewing the lease of item <a href=\"" + URL + "/ItemDetails?uid=" + uid + "\">" + title + "</a> and leasee <strong>" + lease_requser_id + "</strong> on Friend Lease");
				event.createEvent(lease_user_id, lease_requser_id, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_GRACE_PERIOD_REQUESTOR, lease_item_id, "Less than 5 days left for lease to close. Please consider renewing the lease of item <a href=\"" + URL + "/ItemDetails?uid=" + uid + "\">" + title + "</a> ");
				
		} catch (Exception e) {
				e.printStackTrace();
		}finally{
			try {
  				if(rs1 != null)rs1.close();
  				if(ps1 != null)ps1.close();
  				if(hcp != null)hcp.close();
  			}catch (Exception e1){
  				e1.printStackTrace();
			}
		}
	}
      
      public String checkGracePeroid(String ExpDate){
  		
  		LOGGER.info("Inside Method to check Grace Peroid...");
  		Calendar gracePeroidCal = Calendar.getInstance();
  		SimpleDateFormat sdfCal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  		String expDate = ExpDate;
  		String condition = null,startDate=null,startDate1=null;
  		try {
  			gracePeroidCal.setTime(sdfCal.parse(expDate));
  		} catch (ParseException e2) {
  			LOGGER.warning("Can't parse calender time...");
  			e2.printStackTrace();
  		}
  		
  		startDate = sdfCal.format(gracePeroidCal.getTime());
  		Calendar currentCal = Calendar.getInstance();
  		SimpleDateFormat currentSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  		String currentDate = currentSdf.format(currentCal.getTime());
  		LOGGER.info("Start date :"+startDate+" Current Date :"+currentDate);
  		try {
  			if(!sdfCal.parse(startDate).before(sdfCal.parse(currentDate))){
  					gracePeroidCal.add(Calendar.DATE, -gracetime);
  					startDate1 = sdfCal.format(gracePeroidCal.getTime());
  					LOGGER.info("new start date: "+startDate1);
  					if(sdfCal.parse(startDate1).before(sdfCal.parse(currentDate))){
  						condition=  "Grace";
  						LOGGER.info("7 day Grace period is true ...");
  					}else{
  						condition = "Not Grace";
  						LOGGER.info("7 day Grace period is false ...");
  					}
  			}else{
  				condition = "Expired";
  				LOGGER.info(" 7 day Grace Condition not applicable ...");
  			}
  		} catch (ParseException e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  		}
  		return condition;
  	}
}
	