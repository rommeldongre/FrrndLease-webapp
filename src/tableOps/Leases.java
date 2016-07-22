package tableOps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.json.JSONException;
import org.json.JSONObject;

import connect.Connect;
import pojos.LeasesModel;
import pojos.RenewLeaseReqObj;
import adminOps.Response;
import tableOps.LeaseTerms;
import util.FlsSendMail;
import util.LogItem;
import util.AwsSESEmail;
import util.FlsLogger;

public class Leases extends Connect {

	private FlsLogger LOGGER = new FlsLogger(Leases.class.getName());

	private String check = null, Id = null, token, reqUserId, itemId, userId, operation, message, status;
	private int Code;
	private LeasesModel lm;
	private Response res = new Response();

	public Response selectOp(String Operation, LeasesModel lsm, JSONObject obj){
		operation = Operation.toLowerCase();
		lm = lsm;

		switch (operation) {

		case "closelease":
			LOGGER.info("Closing the lease..And making the item back InStore");
			CloseLease();
			break;
		
		case "add":
			LOGGER.info("Add op is selected..");
			Add();
			break;

		case "delete":
			LOGGER.info("Delete operation is selected");
			Delete();
			break;

		case "edit":
			LOGGER.info("Edit operation is selected.");
			Edit();
			break;

		case "editstat":
			LOGGER.info("Edit operation is selected.");
			EditStat();
			break;

		case "getnext":
			LOGGER.info("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getNext();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;

		case "getprevious":
			LOGGER.info("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getPrevious();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;

		case "getnextactive":
			LOGGER.info("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getNextActive();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;

		case "getpreviousactive":
			LOGGER.info("Get Next operation is selected.");
			try {
				token = obj.getString("token");
				getPreviousActive();
			} catch (JSONException e) {
				res.setData(FLS_JSON_EXCEPTION, String.valueOf(token), FLS_JSON_EXCEPTION_M);
				e.printStackTrace();
			}
			break;

		case "renewlease":
			RenewLease();
			break;

		default:
			res.setData(FLS_INVALID_OPERATION, "0", FLS_INVALID_OPERATION_M);
			break;
		}

		return res;
	}
	
	private void CloseLease(){
		int leaseAction = 0, itemAction = 0, storeAction = 0;
		PreparedStatement psLeaseSelect = null, psLeaseUpdate = null, psItemSelect = null, psItemUpdate = null, psStoreUpdate = null;
		ResultSet dbResponseLease =  null, dbResponseitems = null;
		Connection hcp = getConnectionFromPool();
		try {
			hcp.setAutoCommit(false);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LOGGER.info("inside close lease method");
		
		try {
			LOGGER.info("Creating Statement....");
			String sqlrf = "SELECT * FROM leases WHERE lease_item_id=?";
			psLeaseSelect = hcp.prepareStatement(sqlrf);
			psLeaseSelect.setInt(1, Integer.parseInt(lm.getItemId()));

			LOGGER.info("Statement created. Executing select query on lease table");
			dbResponseLease = psLeaseSelect.executeQuery();
			
			if (!dbResponseLease.next()) {
				System.out.println("Empty result while firing select query on 1st table(leases)");
				hcp.rollback();
				hcp.close();
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}
			
			LOGGER.info("Creating Update Statement....");
			String sql = "UPDATE leases SET lease_status = ? WHERE lease_requser_id=? AND lease_item_id=? AND lease_status=?"; //
			psLeaseUpdate = hcp.prepareStatement(sql);
	
			LOGGER.info("Statement created. Executing edit query on lease table...");
			psLeaseUpdate.setString(1, "Archived");
			psLeaseUpdate.setString(2, lm.getReqUserId());
			psLeaseUpdate.setInt(3, Integer.parseInt(lm.getItemId()));
			psLeaseUpdate.setString(4, "Active");
			leaseAction = psLeaseUpdate.executeUpdate();
	
			if(leaseAction == 0){
				System.out.println("Error occured while firing edit query on lease table");
				hcp.rollback();
				hcp.close();
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}
					
			LOGGER.info("Creating statement...");

			String selectItemSql = "SELECT * FROM items WHERE item_id=?";
			psItemSelect = hcp.prepareStatement(selectItemSql);
			psItemSelect.setInt(1, Integer.parseInt(lm.getItemId()));
			dbResponseitems = psItemSelect.executeQuery();
			
			if(!dbResponseitems.next()) {
				System.out.println("Empty result while firing select query on 2nd table(items)");
				hcp.rollback();
				hcp.close();
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}
			
			String updateItemsSql = "UPDATE items SET item_status=? WHERE item_id=?";
			psItemUpdate = hcp.prepareStatement(updateItemsSql);

			LOGGER.info("Statement created. Executing update query on items table...");
			psItemUpdate.setString(1, "InStore");
			psItemUpdate.setInt(2, Integer.parseInt(lm.getItemId()));
			itemAction= psItemUpdate.executeUpdate();
			
			if(itemAction == 0){
				System.out.println("Error occured while firing update query on items table");
				hcp.rollback();
				hcp.close();
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}
			
			String insertStoreSql = "insert into store (store_item_id) values (?)"; //
			LOGGER.info("Creating insert statement store table.....");
			psStoreUpdate = hcp.prepareStatement(insertStoreSql);

			LOGGER.info("Statement created. Executing update query on store table.....");
			psStoreUpdate.setInt(1, Integer.parseInt(lm.getItemId()));
			storeAction = psStoreUpdate.executeUpdate();
			
			if(storeAction == 0){
				System.out.println("Error occured while firing update query on store table");
				hcp.rollback();
				hcp.close();
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}
			
			res.setData(FLS_SUCCESS, "0", FLS_SUCCESS_M);
			hcp.commit();
			//res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
			
			// logging item status to back instore
			LogItem li = new LogItem();
			li.addItemLog(Integer.parseInt(lm.getItemId()), "InStore", "", "");

			AwsSESEmail newE = new AwsSESEmail();
			RenewLeaseReqObj rq = new RenewLeaseReqObj();
			rq.setItemId(Integer.parseInt(lm.getItemId()));
			rq.setFlag("close");
			rq.setReqUserId(lm.getReqUserId());
			rq.setUserId(lm.getUserId());
			newE.send(lm.getUserId(), FlsSendMail.Fls_Enum.FLS_MAIL_REJECT_LEASE_FROM, rq);
			newE.send(lm.getReqUserId(), FlsSendMail.Fls_Enum.FLS_MAIL_REJECT_LEASE_TO, rq);
				
		} catch (SQLException e) {
			LOGGER.info("SQL Exception encountered....");
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}catch (Exception e) {
			LOGGER.info("AWS SES Exception encountered....");
			e.printStackTrace();
		}finally{
			try {
				if(dbResponseLease != null)dbResponseLease.close();
				if(dbResponseitems != null)dbResponseitems.close();
				
				if(psLeaseSelect != null)psLeaseSelect.close();
				if(psLeaseUpdate != null)psLeaseUpdate.close();
				if(psItemSelect != null)psItemSelect.close();
				if(psItemUpdate != null)psItemUpdate.close();
				if(psStoreUpdate != null)psStoreUpdate.close();
				
				if(hcp != null)hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void Add() {
		reqUserId = lm.getReqUserId();
		itemId = lm.getItemId();
		userId = lm.getUserId();
		int days;

		Items item = new Items();
		String term = item.GetLeaseTerm(Integer.parseInt(itemId));

		LeaseTerms Term = new LeaseTerms();

		days = Term.getDuration(term);

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, days);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(cal.getTime());

		String sql = "insert into leases (lease_requser_id,lease_item_id,lease_user_id,lease_expiry_date) values (?,?,?,?)"; //
		PreparedStatement stmt = null,s3 = null,s1 = null,s2 = null;
		ResultSet rs1 = null;
		Connection hcp = getConnectionFromPool();

		try {
			// check if there are credits in the users account
			int credit = 0;
			String sqlCheckCredit = "SELECT user_credit FROM users WHERE user_id=?";
			s1 = hcp.prepareStatement(sqlCheckCredit);
			s1.setString(1, reqUserId);
			rs1 = s1.executeQuery();
			if (rs1.next()) {
				credit = rs1.getInt("user_credit");
			}

			if (credit >= 10) {

				LOGGER.info("Creating statement.....");
				stmt = hcp.prepareStatement(sql);

				LOGGER.info("Statement created. Executing query.....");
				stmt.setString(1, reqUserId);
				stmt.setString(2, itemId);
				stmt.setString(3, userId);
				stmt.setString(4, date);
				stmt.executeUpdate();
				message = "Entry added into leases table";
				LOGGER.warning(message);
				Code = 15;
				Id = reqUserId;

				// add credit to user giving item on lease
				String sqlAddCredit = "UPDATE users SET user_credit=user_credit+10 WHERE user_id=?";
				s2 = hcp.prepareStatement(sqlAddCredit);
				s2.setString(1, userId);
				s2.executeUpdate();

				// subtract credit from user getting a lease
				String sqlSubCredit = "UPDATE users SET user_credit=user_credit-10 WHERE user_id=?";
				s3 = hcp.prepareStatement(sqlSubCredit);
				s3.setString(1, reqUserId);
				s3.executeUpdate();

				try {
					AwsSESEmail newE = new AwsSESEmail();
					newE.send(userId, FlsSendMail.Fls_Enum.FLS_MAIL_GRANT_LEASE_FROM, lm);
					newE.send(reqUserId, FlsSendMail.Fls_Enum.FLS_MAIL_GRANT_LEASE_TO, lm);
				} catch (Exception e) {
					e.printStackTrace();
				}
				res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
			} else {
				res.setData(FLS_ENTRY_NOT_FOUND, "0", "Atleast 10 credits required by the requester");
			}
		} catch (SQLException e) {
			LOGGER.warning("Couldn't create statement");
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				rs1.close();
				
				stmt.close();
				s1.close();
				s2.close();
				s3.close();
				
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void Delete() {
		reqUserId = lm.getReqUserId();
		itemId = lm.getItemId();
		check = null;
		LOGGER.info("Inside delete method....");

		PreparedStatement stmt = null,stmt2 = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		String sql = "DELETE FROM leases WHERE lease_requser_id=?,lease_item_id=?"; //
		String sql2 = "SELECT * FROM leases WHERE lease_requser_id=?,lease_item_id=?"; //

		try {
			LOGGER.info("Creating statement...");

			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setString(1, reqUserId);
			stmt2.setString(2, itemId);
			rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getString("lease_requser_id");
			}

			if (check != null) {
				stmt = hcp.prepareStatement(sql);

				LOGGER.info("Statement created. Executing delete query on ..." + check);
				stmt.setString(1, reqUserId);
				stmt.setString(2, itemId);
				stmt.executeUpdate();
				message = "operation successfull deleted lease Req User id : " + reqUserId;
				LOGGER.warning(message);
				Code = 16;
				Id = check;
				res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
			} else {
				LOGGER.warning("Entry not found in database!!");
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				stmt2.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void Edit() {
		reqUserId = lm.getReqUserId();
		itemId = lm.getItemId();
		userId = lm.getUserId();
		check = null;

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(cal.getTime());

		LOGGER.info("inside edit method");
		PreparedStatement stmt = null,stmt2 = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		String sql = "UPDATE leases SET lease_user_id=?,lease_expiry_date=? WHERE lease_requser_id=? AND lease_item_id=?"; //
		String sql2 = "SELECT * FROM leases WHERE lease_requser_id=? AND lease_item_id=?"; //

		try {
			LOGGER.info("Creating Statement....");
			stmt2 = hcp.prepareStatement(sql2);
			stmt2.setString(1, reqUserId);
			stmt2.setString(2, itemId);
			rs = stmt2.executeQuery();
			while (rs.next()) {
				check = rs.getString("lease_requser_id");
			}

			if (check != null) {
				stmt = hcp.prepareStatement(sql);

				LOGGER.info("Statement created. Executing edit query on ..." + check);
				stmt.setString(1, userId);
				stmt.setString(2, date);
				stmt.setString(3, reqUserId);
				stmt.setString(4, itemId);
				stmt.executeUpdate();
				message = "operation successfull edited lease Req User id : " + reqUserId;
				LOGGER.warning(message);
				Code = 17;
				Id = check;
				res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);
			} else {
				LOGGER.warning("Entry not found in database!!");
				res.setData(FLS_ENTRY_NOT_FOUND, "0", FLS_ENTRY_NOT_FOUND_M);
			}
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				stmt2.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void EditStat() {
		reqUserId = lm.getReqUserId();
		itemId = lm.getItemId();
		status = lm.getStatus();

		LOGGER.info("inside edit method");
		PreparedStatement stmt = null,stmtrf = null ;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		String sql = "UPDATE leases SET lease_status = ? WHERE lease_requser_id=? AND lease_item_id=? AND lease_status=?"; //

		// code for populating lease pojo for sending owner email...
		LeasesModel lm1 = new LeasesModel();
		String sqlrf = "SELECT * FROM leases WHERE lease_item_id=?";
		try {
			LOGGER.info("Creating Statement....");
			stmtrf = hcp.prepareStatement(sqlrf);
			stmtrf.setString(1, itemId);

			LOGGER.info("Statement created. Executing select query on ..." + check);
			ResultSet dbResponse = stmtrf.executeQuery();

			if (dbResponse.next()) {

				if (dbResponse.getString("lease_item_id") != null) {
					LOGGER.info("Inside Nested check statement for FLS_MAIL_REJECT_LEASE_FROM");

					// Populate the response
					try {
						JSONObject obj1 = new JSONObject();
						obj1.put("reqUserId", dbResponse.getString("lease_requser_id"));
						obj1.put("itemId", dbResponse.getString("lease_item_id"));
						obj1.put("userId", dbResponse.getString("lease_user_id"));
						obj1.put("status", dbResponse.getString("lease_status"));

						lm1.getData(obj1);
						LOGGER.warning("Json parsed for FLS_MAIL_REJECT_LEASE_FROM");
					} catch (JSONException e) {
						LOGGER.warning("Couldn't parse/retrieve JSON for FLS_MAIL_REJECT_LEASE_FROM");
						e.printStackTrace();
					}
				}
			}
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}
		// code for populating lease pojo for sending owner email ends here...

		try {
			LOGGER.info("Creating Statement....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing edit query on ..." + check);
			stmt.setString(1, status);
			stmt.setString(2, reqUserId);
			stmt.setString(3, itemId);
			stmt.setString(4, "Active");
			stmt.executeUpdate();
			message = "operation successfull edited lease Req User id : " + reqUserId;
			LOGGER.warning(message);
			Code = 17;
			Id = check;
			res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);

			try {
				AwsSESEmail newE = new AwsSESEmail();
				userId = lm1.getUserId();
				newE.send(userId, FlsSendMail.Fls_Enum.FLS_MAIL_REJECT_LEASE_FROM, lm1);
				newE.send(reqUserId, FlsSendMail.Fls_Enum.FLS_MAIL_REJECT_LEASE_TO, lm);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				stmtrf.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getNext() {
		check = null;
		LOGGER.info("Inside GetNext method");
		String sql = "SELECT * FROM leases WHERE lease_requser_id > ? ORDER BY lease_requser_id LIMIT 1"; //

		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating a statement .....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getNext query...");
			stmt.setString(1, token);

			rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("reqUserId", rs.getString("lease_requser_id"));
				json.put("itemId", rs.getString("lease_item_id"));
				json.put("userId", rs.getString("lease_user_id"));
				json.put("expiry", rs.getString("lease_expiry_date"));

				message = json.toString();
				LOGGER.warning(message);
				check = rs.getString("lease_requser_id");
			}

			if (check != null) {
				Code = FLS_SUCCESS;
				Id = check;
			}

			else {
				Id = "0";
				message = FLS_END_OF_DB_M;
				Code = FLS_END_OF_DB;
			}

			res.setData(Code, Id, message);
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (JSONException e) {
			res.setData(FLS_JSON_EXCEPTION, "0", FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getPrevious() {
		check = null;
		LOGGER.info("Inside GetPrevious method");
		String sql = "SELECT * FROM leases WHERE lease_requser_id < ? ORDER BY lease_requser_id DESC LIMIT 1"; //

		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating a statement .....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getPrevious query...");
			stmt.setString(1, token);

			rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("reqUserId", rs.getString("lease_requser_id"));
				json.put("itemId", rs.getString("lease_item_id"));
				json.put("userId", rs.getString("lease_user_id"));
				json.put("expiry", rs.getString("lease_expiry_date"));

				message = json.toString();
				LOGGER.warning(message);
				check = rs.getString("lease_requser_id");
			}

			if (check != null) {
				Code = FLS_SUCCESS;
				Id = check;
			}

			else {
				Id = "0";
				message = FLS_END_OF_DB_M;
				Code = FLS_END_OF_DB;
			}

			res.setData(Code, Id, message);
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (JSONException e) {
			res.setData(FLS_JSON_EXCEPTION, "0", FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getNextActive() {
		int t = Integer.parseInt(token);
		check = null;
		LOGGER.info("Inside GetNext Active method");
		String sql = "SELECT tb1.*, tb2.user_full_name, tb3.user_full_name AS Owner FROM leases tb1 INNER JOIN users tb2 ON tb1.lease_requser_id = tb2.user_id INNER JOIN users tb3 ON tb1.lease_user_id = tb3.user_id WHERE lease_id > ? AND lease_status=? ORDER BY lease_id LIMIT 1"; //

		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating a statement .....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getNext query...");
			stmt.setInt(1, t);
			stmt.setString(2, "Active");

			rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("reqUserId", rs.getString("lease_requser_id"));
				json.put("itemId", rs.getString("lease_item_id"));
				json.put("userId", rs.getString("lease_user_id"));
				json.put("expiry", rs.getString("lease_expiry_date"));
				json.put("requestorFullName", rs.getString("user_full_name"));
				json.put("OwnerFullName", rs.getString("Owner"));

				message = json.toString();
				LOGGER.warning(message);
				check = String.valueOf(rs.getInt("lease_id"));
			}

			if (check != null) {
				Code = FLS_SUCCESS;
				Id = check;
			}

			else {
				Id = "0";
				message = FLS_END_OF_DB_M;
				Code = FLS_END_OF_DB;
			}

			res.setData(Code, Id, message);
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (JSONException e) {
			res.setData(FLS_JSON_EXCEPTION, "0", FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getPreviousActive() {
		check = null;
		int t = Integer.parseInt(token);
		LOGGER.info("Inside GetPrevious method");
		String sql = "SELECT * FROM leases WHERE lease_id < ? AND lease_status=? ORDER BY lease_id DESC LIMIT 1"; //

		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();
		try {
			LOGGER.info("Creating a statement .....");
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing getPrevious query...");
			stmt.setInt(1, t);
			stmt.setString(2, "Active");

			rs = stmt.executeQuery();
			while (rs.next()) {
				JSONObject json = new JSONObject();
				json.put("reqUserId", rs.getString("lease_requser_id"));
				json.put("itemId", rs.getString("lease_item_id"));
				json.put("userId", rs.getString("lease_user_id"));
				json.put("expiry", rs.getString("lease_expiry_date"));

				message = json.toString();
				LOGGER.warning(message);
				check = String.valueOf(rs.getInt("lease_id"));
			}

			if (check != null) {
				Code = FLS_SUCCESS;
				Id = check;
			}

			else {
				Id = "0";
				message = FLS_END_OF_DB_M;
				Code = FLS_END_OF_DB;
			}

			res.setData(Code, Id, message);
		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (JSONException e) {
			res.setData(FLS_JSON_EXCEPTION, "0", FLS_JSON_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void RenewLease() {
		reqUserId = lm.getReqUserId();
		itemId = lm.getItemId();
		check = null;
		String date1 = null;

		Calendar cal = new GregorianCalendar();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// String date = sdf.format(cal.getTime());

		LOGGER.info("inside edit method");
		PreparedStatement stmt = null,stmt1 = null ;
		ResultSet rs = null;
		Connection hcp = getConnectionFromPool();

		String sql1 = "SELECT lease_expiry_date,lease_id FROM leases WHERE lease_requser_id=? AND lease_item_id=?";

		try {
			stmt1 = hcp.prepareStatement(sql1);
			stmt1.setString(1, reqUserId);
			stmt1.setString(2, itemId);

			rs = stmt1.executeQuery();
			while (rs.next()) {
				date1 = rs.getString("lease_expiry_date");
				check = String.valueOf(rs.getInt("lease_id"));
				LOGGER.warning(date1);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		Items item = new Items();
		String term = item.GetLeaseTerm(Integer.parseInt(itemId));

		LeaseTerms Term = new LeaseTerms();
		int days = Term.getDuration(term);

		try {
			cal.setTime(sdf.parse(date1));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		cal.add(Calendar.DATE, days);
		String date = sdf.format(cal.getTime());
		LOGGER.warning(date);

		String sql = "UPDATE leases SET lease_expiry_date=? WHERE lease_requser_id=? AND lease_item_id=?"; //

		try {
			stmt = hcp.prepareStatement(sql);

			LOGGER.info("Statement created. Executing renew query ...");
			stmt.setString(1, date);
			stmt.setString(2, reqUserId);
			stmt.setString(3, itemId);
			stmt.executeUpdate();
			message = "operation successfull edited lease Req User id : " + reqUserId;
			LOGGER.warning(message);
			Code = 17;
			Id = check;
			res.setData(FLS_SUCCESS, Id, FLS_SUCCESS_M);

		} catch (SQLException e) {
			res.setData(FLS_SQL_EXCEPTION, "0", FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				stmt.close();
				stmt1.close();
				hcp.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
