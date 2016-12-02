package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import pojos.GetLeadsByXResObj;
import connect.Connect;

public class FlsExportLeads extends Connect{
	
	private FlsLogger LOGGER = new FlsLogger(FlsExportLeads.class.getName());
	
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

}
