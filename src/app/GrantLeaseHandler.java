package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import connect.Connect;
import pojos.GrantLeaseReqObj;
import pojos.GrantLeaseResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.AwsSESEmail;
import util.FlsLogger;
import util.FlsSendMail;

public class GrantLeaseHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GrantLeaseHandler.class.getName());

	private static GrantLeaseHandler instance = null;

	public static GrantLeaseHandler getInstance() {
		if (instance == null)
			return new GrantLeaseHandler();
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

		GrantLeaseReqObj rq = (GrantLeaseReqObj) req;
		GrantLeaseResObj rs = new GrantLeaseResObj();
		
		Connection hcp = getConnectionFromPool();
		hcp.setAutoCommit(false);
		
		PreparedStatement psReqSelect = null, psReqUpdate = null, psStoreSelect = null, psStoreUpdate = null, psItemSelect = null, psItemUpdate = null, psLeaseSelect = null, psLeaseUpdate = null, psAddCredit = null, psDebitCredit = null;
		ResultSet result1 = null, result2 = null, result3 = null, result4 = null;
		int RequestAction = 0, StoreAction = 0, ItemAction = 0, LeaseAction = 0, addCredit = 0, subCredit = 0;
		
		try {

			String SelectRequestItemIdSql = "SELECT * FROM requests WHERE request_item_id=?";
			LOGGER.info("Creating 1st Statement of Grant Lease");

			psReqSelect = hcp.prepareStatement(SelectRequestItemIdSql);
			psReqSelect.setInt(1, rq.getItemId());
			LOGGER.info("Created statement...executing select query on Requests table");

			result1 = psReqSelect.executeQuery();
			LOGGER.info(result1.toString());

			if (!result1.next()) {
				//the else
				System.out.println("Empty result while firing select query on 1st table(requests)");
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setError("404");
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				hcp.rollback();
				return rs;
			}
				
			String UpdaterequestStatusSql = "UPDATE requests SET request_status=? WHERE request_item_id=?";
			LOGGER.info("Creating 2st Statement of Grant Lease");
				
			psReqUpdate = hcp.prepareStatement(UpdaterequestStatusSql);
			psReqUpdate.setString(1, "Archived");
			psReqUpdate.setInt(2, rq.getItemId());
			LOGGER.info("Created statement...executing update query on Requests table");
				
				
			RequestAction = psReqUpdate.executeUpdate();

			if (RequestAction == 0) {
				System.out.println("Error occured while firing update query on 1st table(requests)");
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setError("500");
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				hcp.rollback();
				return rs;
			}
			
			String SelectfromStoreSql = "SELECT * FROM store WHERE store_item_id=?";
			psStoreSelect = hcp.prepareStatement(SelectfromStoreSql);
		    psStoreSelect.setInt(1, rq.getItemId());
			LOGGER.info("Created statement...executing select query on Store table");
					
			result2 = psStoreSelect.executeQuery();
			LOGGER.info(result2.toString());
					
			if (!result2.next()) {
				System.out.println("Empty result while firing select query on 2nd table(store)");
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setError("404");
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				hcp.rollback();
				return rs;
			}
				
			String DeletefromStoreSql = "DELETE FROM store WHERE store_item_id=?";
			LOGGER.info("Creating 4th Statement of Grant Lease");
						
			psStoreUpdate = hcp.prepareStatement(DeletefromStoreSql);
			psStoreUpdate.setInt(1, rq.getItemId());
			LOGGER.info("Created statement...executing delete query on Store table");
						
			StoreAction = psStoreUpdate.executeUpdate();
						
			if (StoreAction == 0) {
				System.out.println("Error occured while firing update query on 2nd table(store)");
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setError("500");
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				hcp.rollback();
				return rs;
			}
			
			String SelectfromItemsSql = "SELECT * FROM items WHERE item_id=?";
			psItemSelect = hcp.prepareStatement(SelectfromItemsSql);
			psItemSelect.setInt(1, rq.getItemId());
			LOGGER.info("Created statement...executing select query on Items table");
							
			result3 = psItemSelect.executeQuery();
			LOGGER.info(result3.toString());
							
			if (!result3.next()) {
				System.out.println("Empty result while firing select query on 3rd table(items)");
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setError("404");
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				hcp.rollback();
				return rs;
			}
								
			String updateItemStatusSql = "UPDATE items SET item_status=? WHERE item_id=?";
			LOGGER.info("Creating 6th Statement of Grant Lease");
								
			psItemUpdate = hcp.prepareStatement(updateItemStatusSql);
			psItemUpdate.setString(1, "Leased");
			psItemUpdate.setInt(2, rq.getItemId());
			LOGGER.info("Created statement...executing update query on Items table");
								
			ItemAction = psItemUpdate.executeUpdate();
								
			if (ItemAction == 0) {
				System.out.println("Error occured while firing update query on 3rd table(items)");
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setError("500");
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				hcp.rollback();
				return rs;
			}
									
			int days;
            String term = getLeaseTerm((rq.getItemId()));
			days = getDuration(term);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, days);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String date = sdf.format(cal.getTime());

			int credit = 0;
			String sqlCheckCredit = "SELECT user_credit FROM users WHERE user_id=?";
			psLeaseSelect = hcp.prepareStatement(sqlCheckCredit);
			psLeaseSelect.setString(1, rq.getReqUserId());
			result4 = psLeaseSelect.executeQuery();
									
			if (!result4.next()) {
				System.out.println("Empty result while firing select query on 4th table(leases)");
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setError("404");
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				hcp.rollback();
				return rs;						
			}
			
			credit = result4.getInt("user_credit");
			
			if (credit >= 10) {
				String AddLeasesql = "insert into leases (lease_requser_id,lease_item_id,lease_user_id,lease_expiry_date) values (?,?,?,?)"; //
				LOGGER.info("Creating final statement.....");
				psLeaseUpdate = hcp.prepareStatement(AddLeasesql);

				LOGGER.info("Statement created. Executing insert query in lease table.....");
				psLeaseUpdate.setString(1, rq.getReqUserId());
				psLeaseUpdate.setInt(2, rq.getItemId());
				psLeaseUpdate.setString(3, rq.getUserId());
				psLeaseUpdate.setString(4, date);
										
				LeaseAction = psLeaseUpdate.executeUpdate();
					
				if(LeaseAction == 0){
					System.out.println("Error occured while firing update query on 4th table(leases)");
					rs.setCode(FLS_ENTRY_NOT_FOUND);
					rs.setError("500");
					rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
					hcp.rollback();
					return rs;
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
					return rs;
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
					return rs;
				}
				
				try {
					AwsSESEmail newE = new AwsSESEmail();
				    newE.send(rq.getUserId(), FlsSendMail.Fls_Enum.FLS_MAIL_GRANT_LEASE_FROM, rq);
					newE.send(rq.getReqUserId(), FlsSendMail.Fls_Enum.FLS_MAIL_GRANT_LEASE_TO, rq);
				} catch (Exception e) {
					e.printStackTrace();
				}
										
				rs.setCode(FLS_SUCCESS);
				rs.setId(rq.getReqUserId());
				rs.setMessage(FLS_SUCCESS_M);
				hcp.commit();
				
			} else {
				rs.setCode(FLS_ENTRY_NOT_FOUND);
			    rs.setId("0");
				rs.setMessage("Atleast 10 credits required by the requester");
				return rs;
			}
		} catch (SQLException e) {
			//res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setId("0");
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		}finally{
			
			result1.close();
			result2.close();
			result3.close();
			result4.close();
			
			psReqSelect.close();
			psReqUpdate.close();
			psStoreSelect.close();
			psStoreUpdate.close();
			psItemSelect.close();
			psItemUpdate.close();
			psLeaseSelect.close();
			psLeaseUpdate.close();
			psAddCredit.close();
			psDebitCredit.close();
			
			hcp.close();
		}
		LOGGER.info("Finished process method ");
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
	}

	String getLeaseTerm(int itemId) throws SQLException {
		String term = null;
		PreparedStatement stmt= null;
		ResultSet rs =null;
		
		LOGGER.info("Inside getItemLeaseTerm");
		String sql = "SELECT item_lease_term FROM items WHERE item_id=?";
		Connection hcp = getConnectionFromPool();
		
		try {
			LOGGER.info("executing getItemLesae Term query");
			stmt = hcp.prepareStatement(sql);
			stmt.setInt(1, itemId);

			rs = stmt.executeQuery();
			while (rs.next()) {
				term = rs.getString("item_lease_term");
				LOGGER.warning(term);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			rs.close();
			stmt.close();
			hcp.close();
		}
		return term;
	}
	
	int getDuration(String term) throws SQLException {
		int days = 0;
		PreparedStatement stmt= null;
		ResultSet rs =null;
		
		LOGGER.info("Inside getDuration");
		String sql = "SELECT term_duration FROM leaseterms WHERE term_name=?";
		Connection hcp = getConnectionFromPool();
		
		try {
			LOGGER.info("executing getDuration query...");
			stmt = hcp.prepareStatement(sql);
			stmt.setString(1, term);
			rs = stmt.executeQuery();
			while (rs.next()) {
				days = rs.getInt("term_duration");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			rs.close();
			stmt.close();
			hcp.close();
		}
		return days;
	}
}
