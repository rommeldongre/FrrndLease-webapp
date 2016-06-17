package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;
import pojos.GetLeasesByXReqObj;
import pojos.GetLeasesByXResObj;
import pojos.ReqObj;
import pojos.ResObj;
import util.FlsLogger;

public class GetLeasesByXHandler extends Connect implements AppHandler {

	private FlsLogger LOGGER = new FlsLogger(GetLeasesByXHandler.class.getName());
	
	private static GetLeasesByXHandler instance = null;

	public static GetLeasesByXHandler getInstance() {
		if (instance == null)
			instance = new GetLeasesByXHandler();
		return instance;
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ResObj process(ReqObj req) throws Exception {
		// TODO Auto-generated method stub
		GetLeasesByXReqObj rq = (GetLeasesByXReqObj) req;
		
		GetLeasesByXResObj rs = new GetLeasesByXResObj();
		
		Connection hcp = getConnectionFromPool();
		
		LOGGER.info("Inside process method leaseUserId:" + rq.getLeaseUserId() + " leaseReqUserId: " + rq.getLeaseReqUserId() + "Cookie: " + rq.getCookie());
		
		try{
			
			// Prepare SQL
			String sql = null;
			PreparedStatement sql_stmt = null;
			
			// request data getting from the front end
			String leaseUserId = rq.getLeaseUserId();
			String leaseReqUserId = rq.getLeaseReqUserId();
			int offset = rq.getCookie();
			
			sql = "SELECT tb1.*, tb2.*, tb3.user_full_name AS Owner, tb4.* FROM leases tb1 INNER JOIN users tb2 ON tb1.lease_requser_id = tb2.user_id INNER JOIN users tb3 ON tb1.lease_user_id = tb3.user_id INNER JOIN items tb4 ON tb1.lease_item_id = tb4.item_id WHERE ";
			
			if(leaseUserId != "")
				sql = sql + "tb1.lease_user_id='" + leaseUserId + "' AND ";
			
			if(leaseReqUserId != "")
				sql = sql + "tb1.lease_requser_id='" + leaseReqUserId + "' AND ";
			
			sql = sql + " tb1.lease_status='Active' ORDER BY tb1.lease_id LIMIT " + offset + ", 1";
			
			sql_stmt = hcp.prepareStatement(sql);

			ResultSet dbResponse = sql_stmt.executeQuery();
			
			if(dbResponse.next()){
				rs.setRequestorUserId(dbResponse.getString("lease_requser_id"));
				rs.setRequestorFullName(dbResponse.getString("user_full_name"));
				rs.setRequestorAddress(dbResponse.getString("user_address"));
				rs.setRequestorMobile(dbResponse.getString("user_mobile"));
				rs.setRequestorLocality(dbResponse.getString("user_locality"));
				rs.setRequestorSublocality(dbResponse.getString("user_sublocality"));
				
				rs.setOwnerUserId(dbResponse.getString("lease_user_id"));
				rs.setOwnerFullName(dbResponse.getString("Owner"));
				
				rs.setLeaseExpiryDate(dbResponse.getString("lease_expiry_date"));
				
				rs.setTitle(dbResponse.getString("item_name"));
				rs.setDescription(dbResponse.getString("item_desc"));
				rs.setCategory(dbResponse.getString("item_category"));
				rs.setLeaseValue(dbResponse.getString("item_lease_value"));
				rs.setLeaseTerm(dbResponse.getString("item_lease_term"));
				rs.setImage(dbResponse.getString("item_image"));
				rs.setUid(dbResponse.getString("item_uid"));
				
				offset = offset + 1;
				rs.setCookie(offset);
				
				rs.setCode(FLS_SUCCESS);
			}else{
				rs.setCode(FLS_END_OF_DB);
				rs.setMessage(FLS_END_OF_DB_M);
				LOGGER.warning("End of DB");
			}
			dbResponse.close();
			sql_stmt.close();
			
		}catch(SQLException e){
			rs.setCode(FLS_JSON_EXCEPTION);
			rs.setMessage(FLS_JSON_EXCEPTION_M);
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		}finally {
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
	
}
