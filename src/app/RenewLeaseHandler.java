package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import connect.Connect;
import pojos.RenewLeaseReqObj;
import pojos.RenewLeaseResObj;
import pojos.ReqObj;
import pojos.ResObj;
import app.GrantLeaseHandler;
import util.Event;
import util.Event.Event_Type;
import util.Event.Notification_Type;
import util.FlsConfig;
import util.FlsLogger;
import util.FlsPlan.Delivery_Plan;
import util.LogCredit;
import util.LogItem;
import util.OAuth;

public class RenewLeaseHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(RenewLeaseHandler.class.getName());

	private String URL = FlsConfig.prefixUrl;
	
	private static RenewLeaseHandler instance = null;

	public static RenewLeaseHandler getInstance() {
		if (instance == null)
			return new RenewLeaseHandler();
		return instance;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub

		LOGGER.info("Inside Post Method");

		RenewLeaseReqObj rq = (RenewLeaseReqObj) req;
		RenewLeaseResObj rs = new RenewLeaseResObj();
		
		String flag = rq.getFlag();
		
		switch (flag) {
		case "renew":
			renewLease(rq, rs);
			break;
			
		case "close":
		
			int itemAction = 0;
			String item_image_link=null;
			PreparedStatement psItemSelect = null, psItemUpdate = null, ps1 = null, ps2 = null;
			ResultSet dbResponseitems = null, rs1 = null, rs2 = null;
			Connection hcp = getConnectionFromPool();
			hcp.setAutoCommit(false);
			
			LOGGER.info("inside edit method");
			
			try {
				
				OAuth oauth = new OAuth();
				String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
				if(!oauthcheck.equals(rq.getUserLoggedIn())){
					rs.setCode(FLS_ACCESS_TOKEN_FAILED);
					rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
					return rs;
				}
				
				LOGGER.info("Creating statement...");

				String selectItemSql = "SELECT * FROM items WHERE item_id=?";
				psItemSelect = hcp.prepareStatement(selectItemSql);
				psItemSelect.setInt(1, rq.getItemId());
				dbResponseitems = psItemSelect.executeQuery();
				
				if(!dbResponseitems.next()) {
					System.out.println("Empty result while firing select query on 2nd table(items)");
					rs.setCode(FLS_ENTRY_NOT_FOUND);
					rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
					hcp.rollback();
					return rs;
				}else{
					item_image_link = dbResponseitems.getString("item_primary_image_link");
				}
				
				String updateItemsSql = "UPDATE items SET item_status=? WHERE item_id=?";
				psItemUpdate = hcp.prepareStatement(updateItemsSql);

				LOGGER.info("Statement created. Executing update query on items table...");
				psItemUpdate.setString(1, "LeaseEnded");
				psItemUpdate.setInt(2, rq.getItemId());
				itemAction= psItemUpdate.executeUpdate();
				
				if(itemAction == 0){
					System.out.println("Error occured while firing update query on items table");
					rs.setCode(FLS_ENTRY_NOT_FOUND);
					rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
					hcp.rollback();
					return rs;
				}
				
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_CLOSE_LEASE);
				hcp.commit();
				//res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
				
				// logging item status to lease ended
				LogItem li = new LogItem();
				li.addItemLog(rq.getItemId(), "LeaseEnded", "", item_image_link);
				
				//getting lease delivery plan
				String sqlDeliveryPlan = "SELECT delivery_plan FROM leases WHERE lease_item_id=? AND lease_status=?";
				ps2 = hcp.prepareStatement(sqlDeliveryPlan);
				ps2.setInt(1, rq.getItemId());
				ps2.setString(2, "Active");
				
				rs2 = ps2.executeQuery();
				
				if(rs2.next()){
					
					String deliveryPlan = rs2.getString("delivery_plan");
					
					// fetching the uid and title
					String uid = dbResponseitems.getString("item_uid");
					String title = dbResponseitems.getString("item_name");
					
					Event event = new Event();
					
					if(Delivery_Plan.FLS_SELF.name().equals(deliveryPlan)){
						event.createEvent(rq.getReqUserId(), rq.getUserId(), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_CLOSE_LEASE_FROM_SELF, rq.getItemId(), "You have closed leased of item <a href=\"" + URL + "/ItemDetails?uid=" + uid + "\">" + title + "</a> and leasee <strong>" + rq.getReqUserId() + "</strong> on Friend Lease ");
						event.createEvent(rq.getUserId(), rq.getReqUserId(), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_CLOSE_LEASE_TO_SELF, rq.getItemId(), "Lease has been closed by the Owner for the item <a href=\"" + URL + "/ItemDetails?uid=" + uid + "\">" + title + "</a> ");
					}else{
						event.createEvent("admin@frrndlease.com", "ops@frrndlease.com", Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_OPS_PICKUP_CLOSE, rq.getItemId(), "The lease item - " + rq.getItemId() + " is ready to be picked up.");
						event.createEvent("ops@frrndlease.com", "admin@frrndlease.com", Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_OPS_PICKUP_CLOSE, rq.getItemId(), "The lease item - " + rq.getItemId() + " is ready to be picked up.");
					}

				}

					
			} catch (SQLException e) {
				LOGGER.warning("SQL Exception encountered....");
				rs.setCode(FLS_SQL_EXCEPTION);
				rs.setMessage(FLS_SQL_EXCEPTION_M);
				e.printStackTrace();
			} catch (NullPointerException e) {
				rs.setCode(FLS_NULL_POINT);
				rs.setMessage(FLS_NULL_POINT_M);
				e.printStackTrace();
			} catch (Exception e) {
				LOGGER.warning("AWS SES Exception encountered....");
				rs.setCode(FLS_INVALID_OPERATION);
				rs.setMessage(FLS_INVALID_OPERATION_M);
				e.printStackTrace();
			}finally{
				if(rs2 != null) rs2.close();
				if(ps2 != null) ps2.close();
				if(rs1 != null)rs1.close();
				if(ps1 != null)ps1.close();
				if(dbResponseitems != null)dbResponseitems.close();
				if(psItemSelect != null)psItemSelect.close();
				if(psItemUpdate != null)psItemUpdate.close();
				if(hcp != null)hcp.close();
			}
			break;

		default:
			break;
		}
		LOGGER.info("Finished process method ");
		// return the response
		return rs;
	}

	private boolean renewLease(RenewLeaseReqObj rq, RenewLeaseResObj rs) throws SQLException {
		
		PreparedStatement psRenewSelect = null,psRenewUpdate = null, psUpdateLeaseStatus = null, psAddCredit = null, psDebitCredit = null, ps1 = null;
		ResultSet result1 = null, rs1 = null;
		Connection hcp = getConnectionFromPool();
		int addCredit = 0, subCredit = 0;
		
		Calendar cal = new GregorianCalendar();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		LOGGER.info("inside Renew method of RenewLease App Handler");

		String SelectRenewLeasesql = "SELECT lease_expiry_date,lease_id FROM leases WHERE lease_requser_id=? AND lease_item_id=? AND lease_status=?";
		
		try {
			
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if(!oauthcheck.equals(rq.getUserId())){
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return false;
			}
			
			psRenewSelect = hcp.prepareStatement(SelectRenewLeasesql);
			psRenewSelect.setString(1, rq.getReqUserId());
			psRenewSelect.setInt(2, rq.getItemId());
			psRenewSelect.setString(3, "Active");

			result1 = psRenewSelect.executeQuery();
			if (!result1.next()) {
				System.out.println("Empty result while firing select query on lease table for Renew Lease");
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				return false;
			}
				
				String date1 = null;
				date1 = result1.getString("lease_expiry_date");
				LOGGER.warning(date1);
				GrantLeaseHandler GLH = new GrantLeaseHandler();
				String term = GLH.getLeaseTerm(rq.getItemId());
				int days = GLH.getDuration(term);
				
				LOGGER.info("Executing Check Grace Period Method ..."+date1+" "+term);
				if(!checkGracePeroid(date1,term)){
					LOGGER.info("Renew Lease not done as lease not in grace period");
					rs.setCode(FLS_ENTRY_NOT_FOUND);
					rs.setMessage("Renewal Failed as Item not in Grace Peroid");
					return false;
				}
				
				LOGGER.info(" After Executing Check Grace Period Method ...");
				try {
					cal.setTime(sdf.parse(date1));
				} catch (ParseException e2) {
					e2.printStackTrace();
					LOGGER.info("Error parsing Date info inside Renew Lease ...");
				}
				cal.add(Calendar.DATE, days);
				String date = sdf.format(cal.getTime());
				LOGGER.warning(date);
				
				hcp.setAutoCommit(false);
				String UpdateRenewLeasesql = "UPDATE`leases` SET lease_expiry_date=? WHERE lease_requser_id=? AND lease_item_id=? AND lease_status =?"; //
				
				psRenewUpdate = hcp.prepareStatement(UpdateRenewLeasesql);
	
				LOGGER.info("Statement created. Executing renew query ...");
				psRenewUpdate.setString(1, date);
				psRenewUpdate.setString(2, rq.getReqUserId());
				psRenewUpdate.setInt(3, rq.getItemId());
				psRenewUpdate.setString(4, "Active");
				
				int renewAction =0;
				renewAction = psRenewUpdate.executeUpdate();
				
				if(renewAction == 0 ){
					System.out.println("Error occured while firing Update query on lease table for Renew Lease");
					rs.setCode(FLS_ENTRY_NOT_FOUND);
					rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
					hcp.rollback();
					return false;
				}
				
				String updateItemStatussql = "UPDATE`items` SET item_status=? WHERE item_id=?"; //
				
				psUpdateLeaseStatus = hcp.prepareStatement(updateItemStatussql);
	
				LOGGER.info("Statement created. Executing renew query ...");
				psUpdateLeaseStatus.setString(1, "LeaseStarted");
				psUpdateLeaseStatus.setInt(2, rq.getItemId());
				
				int updateStatusAction =0;
				updateStatusAction = psUpdateLeaseStatus.executeUpdate();
				
				if(updateStatusAction == 0 ){
					System.out.println("Error occured while firing Update query on Items table for Renewing Item Status");
					rs.setCode(FLS_ENTRY_NOT_FOUND);
					rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
					hcp.rollback();
					return false;
				}
				
				// add credit to user giving item on lease
				String sqlAddCredit = "UPDATE users SET user_credit=user_credit+10 WHERE user_id=?";
				psAddCredit = hcp.prepareStatement(sqlAddCredit);
				psAddCredit.setString(1, rq.getUserId());
				addCredit = psAddCredit.executeUpdate();
				
				if(addCredit == 0){
					System.out.println("Error occured while firing 1st update credit query on 5th table(users)");
					rs.setCode(FLS_ENTRY_NOT_FOUND);
					rs.setError("500");
					rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
					hcp.rollback();
					return false;
				}
				LOGGER.info("Add Credit query executed successfully...");
				
				LogCredit lc = new LogCredit();
				lc.addLogCredit(rq.getUserId(),10,"Lease Renewed","");
				
				// subtract credit from user getting a lease
				   String sqlSubCredit = "UPDATE users SET user_credit=user_credit-10 WHERE user_id=?";
				psDebitCredit = hcp.prepareStatement(sqlSubCredit);
				psDebitCredit.setString(1, rq.getReqUserId());
				subCredit = psDebitCredit.executeUpdate();

				if(subCredit == 0){
					System.out.println("Error occured while firing 2nd update credit query on 5th table(users)");
					rs.setCode(FLS_ENTRY_NOT_FOUND);
					rs.setError("500");
					rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
					hcp.rollback();
					return false;
				}
				lc.addLogCredit(rq.getReqUserId(),-10,"Lease Renewed","");
				
				// logging item status to renewed
				LogItem li = new LogItem();
				li.addItemLog(rq.getItemId(), "Lease Renewed", "", "");
				
				LOGGER.info("Debit Credit query executed successfully...");
				rs.setCode(FLS_SUCCESS);
				rs.setId(rq.getReqUserId());
				rs.setMessage(FLS_RENEW_LEASE);
				LOGGER.info("renew Lease query executed successfully...");
				hcp.commit();
				
				try {
					// fetching the uid
					String sqlUid = "SELECT item_uid,item_name FROM items WHERE item_id=?";
					ps1 = hcp.prepareStatement(sqlUid);
					ps1.setInt(1, rq.getItemId());
					rs1 = ps1.executeQuery();
					String uid = null;
					String title = null;
					if(rs1.next()){
						uid = rs1.getString("item_uid");
						title = rs1.getString("item_name");
					}
					
					Event event = new Event();
					event.createEvent(rq.getUserId(), rq.getReqUserId(), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_RENEW_LEASE_REQUESTOR, rq.getItemId(), "Lease has been renewed by the owner of item having item id <a href=\"" + URL + "/ItemDetails?uid=" + uid + "\">" + title + "</a>");
					event.createEvent(rq.getReqUserId(), rq.getUserId(), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_RENEW_LEASE_OWNER, rq.getItemId(), "Lease has been renewed for item having id <a href=\"" + URL + "/ItemDetails?uid=" + uid + "\">" + title + "</a> and leasee <strong>" + rq.getReqUserId() + "</strong> on Friend Lease");
				} catch (Exception e) {
					// TODO: handle exception
					 e.printStackTrace();
				}
		} catch (SQLException e1) {
			// TODO: handle exception
		}  catch (NullPointerException e) {
			rs.setCode(FLS_NULL_POINT);
			rs.setMessage(FLS_NULL_POINT_M);
		}finally{
			try {
				if(rs1 != null)rs1.close();
				if(ps1 != null)ps1.close();
				if(result1 != null)result1.close();
				if(psRenewSelect != null)psRenewSelect.close();
				if(psRenewUpdate!=null) psRenewUpdate.close();	
				if(psAddCredit!=null) psAddCredit.close();
				if(psDebitCredit!=null) psDebitCredit.close();
				if(hcp != null)hcp.close();
			} catch (Exception e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}
		}
		return true;
	}
	
	private boolean checkGracePeroid(String ExpDate, String lTerm){
		
		System.out.println("Inside Method to check Grace Peroid...");
		Calendar gracePeroidCal = Calendar.getInstance();
		SimpleDateFormat sdfCal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int gracedays = 0;
		String graceDays = lTerm;
		String expDate = ExpDate;
		boolean condition = false;
		switch (graceDays) {
		case "Annual":
			gracedays = -7;
			break;
		case "Season":
			gracedays = -7;
			break;
		case "Month":
			gracedays = -7;
			break;
		default:
			break;
		}
		try {
			gracePeroidCal.setTime(sdfCal.parse(expDate));
		} catch (ParseException e2) {
			System.out.println("Can't parse calender time...");
			e2.printStackTrace();
		}
		gracePeroidCal.add(Calendar.DATE, gracedays);
		
		String startDate = sdfCal.format(gracePeroidCal.getTime());
		Calendar currentCal = Calendar.getInstance();
		 SimpleDateFormat currentSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentDate = currentSdf.format(currentCal.getTime());
		try {
			if(sdfCal.parse(startDate).before(sdfCal.parse(currentDate))){
				condition=  true;
				System.out.println("Grace Condition is true ...");
				
			}else{
				condition = false;
				System.out.println("Grace Condition is false ...");
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return condition;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
		
	}

}
