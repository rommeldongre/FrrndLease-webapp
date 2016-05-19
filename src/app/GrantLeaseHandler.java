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
		PreparedStatement ps1 = null,ps2 = null, ps3 = null, ps4 = null, ps5 = null, ps6 = null, ps7 = null, ps8 = null, ps9 = null, ps10 = null;
		ResultSet result1 = null, result2 = null, result3 = null, result4 = null, result5 = null;
		
		try {

			String SelectRequestItemIdSql = "SELECT * FROM requests WHERE request_item_id=?";
			LOGGER.info("Creating 1st Statement of Grant Lease");

			ps1 = hcp.prepareStatement(SelectRequestItemIdSql);
			ps1.setInt(1, rq.getItemId());
			LOGGER.info("Created statement...executing select query on Requests table");

			result1 = ps1.executeQuery();
			LOGGER.info(result1.toString());

			if (result1.next()) {
				String UpdaterequestStatusSql = "UPDATE requests SET request_status=? WHERE request_item_id=?";
				LOGGER.info("Creating 2st Statement of Grant Lease");
				
				ps2 = hcp.prepareStatement(UpdaterequestStatusSql);
				ps2.setString(1, "Archived");
				ps2.setInt(2, rq.getItemId());
				LOGGER.info("Created statement...executing update query on Requests table");
				
				int RequestAction=0;
				RequestAction = ps2.executeUpdate();

				if (RequestAction> 0) {
					
					String SelectfromStoreSql = "SELECT * FROM store WHERE store_item_id=?";
					ps3 = hcp.prepareStatement(SelectfromStoreSql);
					ps3.setInt(1, rq.getItemId());
					LOGGER.info("Created statement...executing select query on Store table");
					
					result2 = ps3.executeQuery();
					LOGGER.info(result2.toString());
					
					if (result2.next()) {
						String DeletefromStoreSql = "DELETE FROM store WHERE store_item_id=?";
						LOGGER.info("Creating 4th Statement of Grant Lease");
						
						ps4 = hcp.prepareStatement(DeletefromStoreSql);
						ps4.setInt(1, rq.getItemId());
						LOGGER.info("Created statement...executing delete query on Store table");
						
						int StoreAction =0;
						StoreAction = ps4.executeUpdate();
						
						if (StoreAction > 0) {
							String SelectfromItemsSql = "SELECT * FROM items WHERE item_id=?";
							ps5 = hcp.prepareStatement(SelectfromItemsSql);
							ps5.setInt(1, rq.getItemId());
							LOGGER.info("Created statement...executing select query on Items table");
							
							result3 = ps5.executeQuery();
							LOGGER.info(result3.toString());
							
							if (result3.next()) {
								String updateItemStatusSql = "UPDATE items SET item_status=? WHERE item_id=?";
								LOGGER.info("Creating 6th Statement of Grant Lease");
								
								ps6 = hcp.prepareStatement(updateItemStatusSql);
								ps6.setString(1, "Leased");
								ps6.setInt(2, rq.getItemId());
								LOGGER.info("Created statement...executing update query on Items table");
								
								int ItemAction =0;
								ItemAction = ps6.executeUpdate();
								
								if (ItemAction > 0) {
									int days;

									//Items item = new Items();
									String term = GetLeaseTerm((rq.getItemId()));

									//LeaseTerms Term = new LeaseTerms();
									days = getDuration(term);

									Calendar cal = Calendar.getInstance();
									cal.add(Calendar.DATE, days);
									SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									String date = sdf.format(cal.getTime());

									int credit = 0;
									String sqlCheckCredit = "SELECT user_credit FROM users WHERE user_id=?";
									ps7 = hcp.prepareStatement(sqlCheckCredit);
									ps7.setString(1, rq.getReqUserId());
									result4 = ps7.executeQuery();
									
									if (result4.next()) {
										credit = result4.getInt("user_credit");
									}

									if (credit >= 10) {
										String AddLeasesql = "insert into leases (lease_requser_id,lease_item_id,lease_user_id,lease_expiry_date) values (?,?,?,?)"; //
										LOGGER.info("Creating final statement.....");
										ps8 = hcp.prepareStatement(AddLeasesql);

										LOGGER.info("Statement created. Executing inser query in lease table.....");
										ps8.setString(1, rq.getReqUserId());
										ps8.setInt(2, rq.getItemId());
										ps8.setString(3, rq.getUserId());
										ps8.setString(4, date);
										
										int LeaseAction =0;
										LeaseAction = ps8.executeUpdate();
										//message = "Entry added into leases table";
										//LOGGER.warning(message);
										//Code = 15;
										//Id = reqUserId;

										// add credit to user giving item on lease
										String sqlAddCredit = "UPDATE users SET user_credit=user_credit+10 WHERE user_id=?";
										 ps9 = hcp.prepareStatement(sqlAddCredit);
										 ps9.setString(1, rq.getUserId());
										 ps9.executeUpdate();

										// subtract credit from user getting a lease
										String sqlSubCredit = "UPDATE users SET user_credit=user_credit-10 WHERE user_id=?";
										ps10 = hcp.prepareStatement(sqlSubCredit);
										ps10.setString(1, rq.getReqUserId());
										ps10.executeUpdate();

										try {
											AwsSESEmail newE = new AwsSESEmail();
											newE.send(rq.getUserId(), FlsSendMail.Fls_Enum.FLS_MAIL_GRANT_LEASE_FROM, rq);
											newE.send(rq.getReqUserId(), FlsSendMail.Fls_Enum.FLS_MAIL_GRANT_LEASE_TO, rq);
										} catch (Exception e) {
											e.printStackTrace();
										}
										//res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
										rs.setCode(FLS_SUCCESS);
										rs.setId(rq.getReqUserId());
										rs.setMessage(FLS_SUCCESS_M);
									} else {
										//res.setData(FLS_ENTRY_NOT_FOUND, "0", "Atleast 10 credits required by the requester");
										rs.setCode(FLS_ENTRY_NOT_FOUND);
										rs.setId("0");
										rs.setMessage("Atleast 10 credits required by the requester");
									}
									
								}else{
									System.out.println("Error occured while firing update query on 3rd table(items)");
									rs.setCode(FLS_ENTRY_NOT_FOUND);
									rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
								}
								
							}else{
								System.out.println("Empty result while firing select query on 3rd table(items)");
								rs.setCode(FLS_ENTRY_NOT_FOUND);
								rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
							}
						}else{
							System.out.println("Error occured while firing update query on 2nd table(store)");
							rs.setCode(FLS_ENTRY_NOT_FOUND);
							rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
						}
					}else{
						System.out.println("Empty result while firing select query on 2nd table(store)");
						rs.setCode(FLS_ENTRY_NOT_FOUND);
						rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
					}
					
				}else{
					System.out.println("Error occured while firing update query on 1st table(requests)");
					rs.setCode(FLS_ENTRY_NOT_FOUND);
					rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				}
			} else {
				System.out.println("Empty result while firing select query on 1st table(requests)");
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
			}

		} catch (SQLException e) {
			//res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setId("0");
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		}finally{
			ps1.close();
			ps2.close();
			ps3.close();
			ps4.close();
			ps5.close();
			ps6.close();
			ps7.close();
			ps8.close();
			ps9.close();
			ps10.close();
			
			hcp.close();
			
		}
		rs.setCode(FLS_SUCCESS);
		rs.setId("Sucess String");
		rs.setMessage("Success");
		LOGGER.info("Finished process method ");
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
	}

	String GetLeaseTerm(int itemId) throws SQLException {
		String term = null;
		LOGGER.info("Inside getItemLeaseTerm");
		String sql = "SELECT item_lease_term FROM items WHERE item_id=?";
		//getConnection();
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("executing getItemLesae Term query");
			PreparedStatement stmt = hcp.prepareStatement(sql);
			stmt.setInt(1, itemId);

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				term = rs.getString("item_lease_term");
				LOGGER.warning(term);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
		 hcp.close();
		}
		return term;
	}
	
	int getDuration(String term) throws SQLException {
		int days = 0;
		LOGGER.info("Inside getDuration");
		String sql = "SELECT term_duration FROM leaseterms WHERE term_name=?";
		//getConnection();
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("executing getDuration query...");
			PreparedStatement stmt = hcp.prepareStatement(sql);
			stmt.setString(1, term);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				days = rs.getInt("term_duration");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			hcp.close();
		}
		return days;
	}
}
