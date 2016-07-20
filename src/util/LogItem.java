package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import connect.Connect;
import util.FlsLogger;

public class LogItem extends Connect{

	private FlsLogger LOGGER = new FlsLogger(LogItem.class.getName());
	
	public void addItemLog(int itemId, String type, String description, String image) throws SQLException{
		
		int days = 0;
		PreparedStatement stmt= null;
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, days);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(cal.getTime());
		
		LOGGER.info("Adding item log");
		String sqlAddItemLog = "insert into item_log (item_id,item_log_date,item_log_type,item_log_desc,item_log_image) values (?,?,?,?,?)";
		Connection hcp = getConnectionFromPool();
		
		try {
			LOGGER.info("executing insert query on credit_log...");
			stmt = hcp.prepareStatement(sqlAddItemLog);
			stmt.setInt(1, itemId);
			stmt.setString(2, date);
			stmt.setString(3, type);
			stmt.setString(4, description);
			stmt.setString(5, image);
			
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(stmt != null)stmt.close();
			if(hcp != null)hcp.close();
		}
		
	}
}
