package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import pojos.GetLeadsByXResObj;
import connect.Connect;

public class FlsExport extends Connect{
	
	private FlsLogger LOGGER = new FlsLogger(FlsExport.class.getName());
	
	public StringBuffer getLeads(String leadType, String fDate, String tDate){
		StringBuffer output = new StringBuffer();
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement sql_stmt = null;
		ResultSet dbResponse = null;
		
		try {

			// Prepare SQL
			String sql = null;
			
			// storing the front end data in appropriate variables
			String fromDate = fDate;
			String toDate = tDate;
			String type = leadType;
			 
			//already getting all data from events table
			sql = "SELECT tb1.* FROM leads tb1 WHERE ";
			
			if(fromDate.equals(toDate)){
				sql = sql + "lead_datetime LIKE '"+fromDate+"%' AND ";
			}else{
				sql = sql + "lead_datetime BETWEEN '"+fromDate+"' AND '"+toDate+"' AND ";
			}
			
			if(type.equals("all")){
				sql = sql + "tb1.lead_type LIKE '%' ORDER BY tb1.lead_id";
			}else{
				sql = sql + "tb1.lead_type='" + type + "' ORDER BY tb1.lead_id";
			}
					
			sql_stmt = hcp.prepareStatement(sql);

			dbResponse = sql_stmt.executeQuery();
			
			if (dbResponse.isBeforeFirst()) {
				while (dbResponse.next()) {			
					output.append(dbResponse.getInt("lead_id"));
					output.append(",");
					output.append(dbResponse.getString("lead_datetime"));
					output.append(",");
					output.append(dbResponse.getString("lead_email"));
					output.append(",");
					output.append(dbResponse.getString("lead_type"));
					output.append(",");
					output.append(dbResponse.getString("lead_url"));
					output.append("\n");
					LOGGER.info("Success");
				}
			} else {
				output = null;
				LOGGER.warning(FLS_END_OF_DB_M);
			}
		} catch (SQLException e) {
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		} finally {
			try {
				if(dbResponse!=null) dbResponse.close();
				if(sql_stmt!=null) sql_stmt.close();
				if(hcp!=null) hcp.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return output;
		
	}
	
	
	public StringBuffer getUsers(int verification,int liveStatus,int userStatus){
		StringBuffer output = new StringBuffer();
		
		Connection hcp = getConnectionFromPool();
		PreparedStatement sql_stmt = null;
		ResultSet dbResponse = null;
		
		try {

			// Prepare SQL
			String sql = null;
			
			sql = "SELECT * FROM `users` WHERE user_id NOT IN ('admin@frrndlease.com', 'ops@frrndlease.com')";
			
			if(verification != -1)
				sql = sql + " AND user_verified_flag=" + verification;
			
			if(liveStatus != -1)
				sql = sql + " AND user_live_status=" + liveStatus;
			
			if(userStatus == 0){
				sql = sql + " AND user_fee_expiry IS NULL";
			}else if(userStatus == 1){
				sql = sql + " AND user_fee_expiry IS NOT NULL";
			}
				
			
			sql = sql + " ORDER BY user_signup_date DESC";
					
			sql_stmt = hcp.prepareStatement(sql);

			dbResponse = sql_stmt.executeQuery();
			
			if (dbResponse.isBeforeFirst()) {
				while (dbResponse.next()) {			
					output.append(dbResponse.getString("user_signup_date"));
					output.append(",");
					output.append(dbResponse.getString("user_fee_expiry"));
					output.append(",");
					output.append(dbResponse.getString("user_id"));
					output.append(",");
					output.append(dbResponse.getString("user_uid"));
					output.append(",");
					output.append(dbResponse.getString("user_profile_picture"));
					output.append(",");
					output.append(dbResponse.getString("user_plan"));
					output.append(",");
					output.append(dbResponse.getString("user_full_name"));
					output.append(",");
					output.append(dbResponse.getString("user_mobile"));
					output.append(",");
					output.append(dbResponse.getString("user_email"));
					output.append(",");
					output.append(dbResponse.getString("user_sec_status"));
					output.append(",");
					output.append("\"" + dbResponse.getString("user_location") + "\"");
					output.append(",");
					output.append(dbResponse.getString("user_sublocality"));
					output.append(",");
					output.append(dbResponse.getString("user_locality"));
					output.append(",");
					output.append(dbResponse.getString("user_referral_code"));
					output.append(",");
					output.append(dbResponse.getString("user_referrer_code"));
					output.append(",");
					output.append(dbResponse.getString("user_photo_id"));
					output.append(",");
					output.append(dbResponse.getString("user_notification"));
					output.append(",");
					output.append(dbResponse.getString("user_credit"));
					output.append(",");
					output.append(dbResponse.getFloat("user_lat"));
					output.append(",");
					output.append(dbResponse.getFloat("user_lng"));
					output.append(",");
					output.append(dbResponse.getBoolean("user_verified_flag"));
					output.append(",");
					output.append(dbResponse.getBoolean("user_live_status"));
					output.append(",");
					output.append(dbResponse.getString("user_status"));
					output.append(",");
					output.append(dbResponse.getInt("user_items"));
					output.append(",");
					output.append(dbResponse.getInt("user_leases"));
					output.append(",");
					output.append(dbResponse.getInt("user_response_time"));
					output.append(",");
					output.append(dbResponse.getInt("user_response_count"));
					output.append(",");
					output.append(dbResponse.getString("about"));
					output.append(",");
					output.append(dbResponse.getString("website"));
					output.append(",");
					output.append(dbResponse.getString("email"));
					output.append(",");
					output.append(dbResponse.getString("phone_no"));
					output.append(",");
					output.append(dbResponse.getString("business_hours"));
					output.append("\n");
					
					LOGGER.info("Success");
				}
			} else {
				output = null;
				LOGGER.warning(FLS_END_OF_DB_M);
			}
		} catch (SQLException e) {
			LOGGER.warning("Error Check Stacktrace");
			e.printStackTrace();
		} finally {
			try {
				if(dbResponse!=null) dbResponse.close();
				if(sql_stmt!=null) sql_stmt.close();
				if(hcp!=null) hcp.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return output;
		
	}

}
