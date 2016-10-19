package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import connect.Connect;
import util.FlsLogger;

public class LogItem extends Connect{

	private FlsLogger LOGGER = new FlsLogger(LogItem.class.getName());
	
	public void addItemLog(int itemId, String type, String description, String image_link) throws SQLException{
		
		PreparedStatement stmt= null;
		
		LOGGER.info("Adding item log");
		String sqlAddItemLog = "insert into item_log (item_id,item_log_type,item_log_desc,item_log_image_link) values (?,?,?,?)";
		Connection hcp = getConnectionFromPool();
		
		try {
			LOGGER.info("executing insert query on item_log...");
			stmt = hcp.prepareStatement(sqlAddItemLog);
			stmt.setInt(1, itemId);
			stmt.setString(2, type);
			stmt.setString(3, description);
			stmt.setString(4, image_link);
			
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
