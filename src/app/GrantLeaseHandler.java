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
import util.Event;
import util.Event.Event_Type;
import util.Event.Notification_Type;
import util.FlsBadges;
import util.FlsConfig;
import util.FlsLogger;
import util.FlsPlan.Delivery_Plan;
import util.FlsPlan.Fls_Plan;
import util.LogCredit;
import util.LogItem;
import util.OAuth;

public class GrantLeaseHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GrantLeaseHandler.class.getName());

	private String URL = FlsConfig.prefixUrl;
	
	private static GrantLeaseHandler instance = null;

	public static GrantLeaseHandler getInstance() {
		if (instance == null)
			return new GrantLeaseHandler();
		return instance;
	}

	@Override
	public void init() {
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {

		LOGGER.info("Inside Process Method of GrantLeaseHandler");

		GrantLeaseReqObj rq = (GrantLeaseReqObj) req;
		GrantLeaseResObj rs = new GrantLeaseResObj();
		
		Connection hcp = getConnectionFromPool();
		hcp.setAutoCommit(false);
		
		PreparedStatement ps1 = null, ps2 = null, ps3 = null, ps4 = null, ps5 = null, ps6 = null;
		ResultSet rs2 = null, rs1 = null, rs6 = null;
		int rs3 = 0, rs4 = 0, rs5 = 0;
		
		try {
			
			OAuth oauth = new OAuth();
			String oauthcheck = oauth.CheckOAuth(rq.getAccessToken());
			if(!oauthcheck.equals(rq.getUserId())){
				rs.setCode(FLS_ACCESS_TOKEN_FAILED);
				rs.setMessage(FLS_ACCESS_TOKEN_FAILED_M);
				return rs;
			}
			
			// storing itemId
			int itemId = rq.getItemId();
			// saving requestorId
			String requestorId = rq.getReqUserId();
			// saving Items ownerId
			String ownerId = rq.getUserId();
			
			LOGGER.info("Getting ItemId : " + itemId + " - Details...");
			String sqlGetItemsDetails = "SELECT item_status, item_primary_image_link, item_name, item_uid FROM `items` WHERE item_id=?";
			ps1 = hcp.prepareStatement(sqlGetItemsDetails);
			ps1.setInt(1, itemId);
			rs1 = ps1.executeQuery();
			
			if(rs1.next()){
				if(!(rs1.getString("item_status")).equals("InStore")){
					rs.setCode(FLS_ITEM_ON_HOLD);
					rs.setMessage(FLS_ITEM_ON_HOLD_M);
					hcp.rollback();
					return rs;
				}
			}
			
			LOGGER.info("Getting Requestors Data...");
			String sqlGetUserData = "SELECT * FROM users WHERE user_id=?";
			ps2 = hcp.prepareStatement(sqlGetUserData);
			ps2.setString(1, requestorId);
			rs2 = ps2.executeQuery();
									
			if (!rs2.next()) {
				LOGGER.warning("UserId : " + requestorId + " not found.");
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				hcp.rollback();
				return rs;						
			}
			
			if (rs2.getInt("user_credit") < 10) {
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_CREDITS_INSUFFICIENT_FOR_LEASE);
				return rs;
			}

			LOGGER.info("Getting all active requests for ItemId : " + itemId + " and requestorId : " + requestorId);
			String sqlGetAllRequests = "SELECT * FROM requests WHERE request_status=? AND request_item_id=? AND request_requser_id=?";
			
			ps6 = hcp.prepareStatement(sqlGetAllRequests);
			ps6.setString(1, "Active");
			ps6.setInt(2, itemId);
			ps6.setString(3, requestorId);
			
			rs6 = ps6.executeQuery();
			
			if(!rs6.next()){
				LOGGER.warning("Not able to select active requests for itemId : " + itemId);
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				hcp.rollback();
				return rs;
			}
			
			// Updating badges data for owner and requester
			FlsBadges badges = new FlsBadges(ownerId);
			badges.updateLeasesCount();
			badges.updateRequestResponseTime(rs6.getString("request_lastmodified"));
			badges = new FlsBadges(requestorId);
			badges.updateLeasesCount();
			
			LOGGER.info("Archiving all the requests");
			String sqlArchivingRequests = "UPDATE requests SET request_status=? WHERE request_item_id=?";
				
			ps3 = hcp.prepareStatement(sqlArchivingRequests);
			ps3.setString(1, "Archived");
			ps3.setInt(2, itemId);
			
			rs3 = ps3.executeUpdate();

			if (rs3 == 0) {
				LOGGER.warning("Not able to find requests for itemId : " + itemId);
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				hcp.rollback();
				return rs;
			}
			
			LOGGER.info("Updating itemId - " + itemId + " status to LeaseReady");
			String sqlUpdatingItemStatus = "UPDATE items SET item_status=? WHERE item_id=?";
								
			ps4 = hcp.prepareStatement(sqlUpdatingItemStatus);
			ps4.setString(1, "LeaseReady");
			ps4.setInt(2, itemId);
								
			rs4 = ps4.executeUpdate();
								
			if (rs4 == 0) {
				LOGGER.warning("Error occured while updating item_status to LeaseReady");
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				hcp.rollback();
				return rs;
			}
			
			// logging item status to lease ready
			LogItem li = new LogItem();
			li.addItemLog(itemId, "LeaseReady", "", rs1.getString("item_primary_image_link"));
			
			LOGGER.info("Creating a new lease for itemId : " + itemId + " and userId : " + requestorId);
			int days;
            String term = getLeaseTerm((itemId));
			days = getDuration(term);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, days);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String date = sdf.format(cal.getTime());

			String sqlCreateLease = "insert into leases (lease_requser_id,lease_item_id,lease_user_id,lease_expiry_date,delivery_plan) values (?,?,?,?,?)";
			ps5 = hcp.prepareStatement(sqlCreateLease);

			ps5.setString(1, requestorId);
			ps5.setInt(2, itemId);
			ps5.setString(3, ownerId);
			ps5.setString(4, date);
			if(rs2.getString("user_plan").equals(Fls_Plan.FLS_SELFIE.name()))
				ps5.setString(5, Delivery_Plan.FLS_SELF.name());
			else
				ps5.setString(5, Delivery_Plan.FLS_NONE.name());
									
			rs5 = ps5.executeUpdate();
				
			if(rs5 == 0){
				LOGGER.warning("Not able to create a lease for itemId : " + itemId + " and userId : " + requestorId);
				rs.setCode(FLS_ENTRY_NOT_FOUND);
				rs.setMessage(FLS_ENTRY_NOT_FOUND_M);
				hcp.rollback();
				return rs;
			}
			
			hcp.commit();
			rs.setCode(FLS_SUCCESS);
			rs.setMessage(FLS_GRANT_LEASE);
			
			LogCredit lc = new LogCredit();
			lc.addLogCredit(ownerId,10,"Lease Granted","");
			// add credit to user giving item on lease
			lc.addCredit(ownerId, 10);
			
			lc.addLogCredit(requestorId,-10,"Lease Recieved","");
			// subtract credit from user getting a lease
			lc.subtractCredit(requestorId, 10);

			try {
				Event event = new Event();
				while(rs6.next()){
					event.createEvent(ownerId, rs6.getString("request_requser_id"), Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_REJECT_REQUEST_TO, itemId, "Your request for item <a href=\"" + URL + "/ItemDetails?uid=" + rs1.getString("item_uid") + "\">" + rs1.getString("item_name") + "</a> is closed by the owner as a lease has been granted to someone else.");
				}
				if(rs2.getString("user_plan").equals(Fls_Plan.FLS_SELFIE.name())){
					event.createEvent(requestorId, ownerId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_GRANT_LEASE_FROM_SELF, itemId, "You have sucessfully leased an item to <a href=\"" + URL + "/myapp.html#/myleasedoutitems\">" + requestorId + "</a> on Friend Lease ");
					event.createEvent(ownerId, requestorId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_GRANT_LEASE_TO_SELF, itemId, "An item has been leased by <a href=\"" + URL + "/myapp.html#/myleasedinitems\">" + ownerId + "</a> to you on Friend Lease ");
				}else{
					event.createEvent(requestorId, ownerId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_GRANT_LEASE_FROM_PRIME, itemId, "You have sucessfully leased an item to <a href=\"" + URL + "/myapp.html#/myleasedoutitems\">" + requestorId + "</a> on Friend Lease ");
					event.createEvent(ownerId, requestorId, Event_Type.FLS_EVENT_NOTIFICATION, Notification_Type.FLS_MAIL_GRANT_LEASE_TO_PRIME, itemId, "An item has been leased by <a href=\"" + URL + "/myapp.html#/myleasedinitems\">" + ownerId + "</a> to you on Friend Lease ");
				}
			} catch (Exception e) {
				rs.setCode(FLS_INVALID_OPERATION);
				rs.setMessage(FLS_INVALID_OPERATION_M);
				e.printStackTrace();
			}						
			
		} catch (SQLException e) {
			rs.setCode(FLS_SQL_EXCEPTION);
			rs.setMessage(FLS_SQL_EXCEPTION_M);
			e.printStackTrace();
		} catch (NullPointerException e) {
			rs.setCode(FLS_NULL_POINT);
			rs.setMessage(FLS_NULL_POINT_M);
			e.printStackTrace();
		} finally{
			if(rs2 != null) rs2.close();
			if(rs1 != null) rs1.close();

			if(ps5 != null) ps5.close();
			if(ps4 != null) ps4.close();
			if(ps3 != null) ps3.close();
			if(ps2 != null) ps2.close();
			if(ps1 != null) ps1.close();
			
			if(hcp != null)hcp.close();
		}
		LOGGER.info("Finished process method");
		// return the response
		return rs;
	}

	@Override
	public void cleanup() {
	}

	public String getLeaseTerm(int itemId) throws SQLException {
		String term = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
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
			if(rs != null) rs.close();
			if(stmt != null) stmt.close();
			if(hcp != null) hcp.close();
		}
		return term;
	}
	
	public int getDuration(String term) throws SQLException {
		int days = 0;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
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
			if(rs != null) rs.close();
			if(stmt != null) stmt.close();
			if(hcp != null) hcp.close();
		}
		return days;
	}
}
