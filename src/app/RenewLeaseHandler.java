package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import connect.Connect;
import pojos.RenewLeaseReqObj;
import pojos.RenewLeaseResObj;
import pojos.ReqObj;
import pojos.ResObj;
import app.GrantLeaseHandler;
import util.AwsSESEmail;
import util.FlsLogger;
import util.FlsSendMail;

public class RenewLeaseHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(RenewLeaseHandler.class.getName());

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
		
			int leaseAction = 0, itemAction = 0, storeAction = 0;
			PreparedStatement psLeaseSelect = null, psLeaseUpdate = null, psItemSelect = null, psItemUpdate = null, psStoreUpdate = null;
			ResultSet dbResponseLease =  null, dbResponseitems = null;
			Connection hcp = getConnectionFromPool();
			hcp.setAutoCommit(false);
			
			LOGGER.info("inside edit method");
			
			try {
				LOGGER.info("Creating Statement....");
				String sqlrf = "SELECT * FROM leases WHERE lease_item_id=?";
				psLeaseSelect = hcp.prepareStatement(sqlrf);
				psLeaseSelect.setInt(1, rq.getItemId());

				LOGGER.info("Statement created. Executing select query on lease table");
				dbResponseLease = psLeaseSelect.executeQuery();
				
				if (!dbResponseLease.next()) {
					System.out.println("Empty result while firing select query on 1st table(leases)");
					rs.setCode(FLS_ENTRY_NOT_FOUND);
					rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
					hcp.rollback();
					hcp.close();
					return rs;
				}
				
				LOGGER.info("Creating Update Statement....");
				String sql = "UPDATE leases SET lease_status = ? WHERE lease_requser_id=? AND lease_item_id=? AND lease_status=?"; //
				psLeaseUpdate = hcp.prepareStatement(sql);
		
				LOGGER.info("Statement created. Executing edit query on lease table...");
				psLeaseUpdate.setString(1, "Archived");
				psLeaseUpdate.setString(2, rq.getReqUserId());
				psLeaseUpdate.setInt(3, rq.getItemId());
				psLeaseUpdate.setString(4, "Active");
				leaseAction = psLeaseUpdate.executeUpdate();
		
				if(leaseAction == 0){
					System.out.println("Error occured while firing edit query on lease table");
					rs.setCode(FLS_ENTRY_NOT_FOUND);
					rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
					hcp.rollback();
					hcp.close();
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
					hcp.close();
					return rs;
				}
				
				String updateItemsSql = "UPDATE items SET item_status=? WHERE item_id=?";
				psItemUpdate = hcp.prepareStatement(updateItemsSql);

				LOGGER.info("Statement created. Executing update query on items table...");
				psItemUpdate.setString(1, "InStore");
				psItemUpdate.setInt(2, rq.getItemId());
				itemAction= psItemUpdate.executeUpdate();
				
				if(itemAction == 0){
					System.out.println("Error occured while firing update query on items table");
					rs.setCode(FLS_ENTRY_NOT_FOUND);
					rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
					hcp.rollback();
					hcp.close();
					return rs;
				}
				
				String insertStoreSql = "insert into store (store_item_id) values (?)"; //
				LOGGER.info("Creating insert statement store table.....");
				psStoreUpdate = hcp.prepareStatement(insertStoreSql);

				LOGGER.info("Statement created. Executing update query on store table.....");
				psStoreUpdate.setInt(1, rq.getItemId());
				storeAction = psStoreUpdate.executeUpdate();
				
				if(storeAction == 0){
					System.out.println("Error occured while firing update query on store table");
					rs.setCode(FLS_ENTRY_NOT_FOUND);
					rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
					hcp.rollback();
					hcp.close();
					return rs;
				}

				AwsSESEmail newE = new AwsSESEmail();
				newE.send(rq.getUserId(), FlsSendMail.Fls_Enum.FLS_MAIL_REJECT_LEASE_FROM, rq);
				newE.send(rq.getReqUserId(), FlsSendMail.Fls_Enum.FLS_MAIL_REJECT_LEASE_TO, rq);
				
				rs.setCode(FLS_SUCCESS);
				rs.setMessage(FLS_SUCCESS_M);
				hcp.commit();
				//res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
					
			} catch (SQLException e) {
				LOGGER.info("SQL Exception encountered....");
				rs.setCode(FLS_SQL_EXCEPTION);
				rs.setMessage(FLS_SQL_EXCEPTION_M);
				e.printStackTrace();
			}catch (Exception e) {
				LOGGER.info("AWS SES Exception encountered....");
				e.printStackTrace();
			}finally{
				
				dbResponseLease.close();
				dbResponseitems.close();
				
				psLeaseSelect.close();
				psLeaseUpdate.close();
				psItemSelect.close();
				psItemUpdate.close();
				psStoreUpdate.close();
				
				hcp.close();
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
		
		PreparedStatement psRenewSelect = null,psRenewUpdate = null, psAddCredit = null, psDebitCredit = null;
		ResultSet result1 = null;
		Connection hcp = getConnectionFromPool();
		int addCredit = 0, subCredit = 0;
		
		Calendar cal = new GregorianCalendar();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		LOGGER.info("inside Renew method of RenewLease App Handler");

		String SelectRenewLeasesql = "SELECT lease_expiry_date,lease_id FROM leases WHERE lease_requser_id=? AND lease_item_id=? AND lease_status=?";
		
		try {
			psRenewSelect = hcp.prepareStatement(SelectRenewLeasesql);
			psRenewSelect.setString(1, rq.getReqUserId());
			psRenewSelect.setInt(2, rq.getItemId());
			psRenewSelect.setString(3, "Active");

			result1 = psRenewSelect.executeQuery();
			if (!result1.next()) {
				System.out.println("Empty result while firing select query on lease table for Renew Lease");
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				hcp.close();
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
					//rs.setId("Error");
					rs.setMessage("Renewal Failed as Item not in Grace Peroid");
					hcp.close();
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
					hcp.close();
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
					hcp.close();
					return false;
				}

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
					LOGGER.info("renew Lease query executed successfully...");
					rs.setCode(FLS_SUCCESS);
					rs.setId(rq.getReqUserId());
					rs.setMessage(FLS_SUCCESS_M);
					hcp.commit();
		} catch (SQLException e1) {
			// TODO: handle exception
		}catch(NullPointerException e) {
		   e.printStackTrace();
		}finally{
			try {
				result1.close();
				psRenewSelect.close();
				if(psRenewUpdate!=null) psRenewUpdate.close();	
				hcp.close();
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
			gracedays = -37;
			break;
		case "Season":
			gracedays = -10;
			break;
		case "Month":
			gracedays = -3;
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
