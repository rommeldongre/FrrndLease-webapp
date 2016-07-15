package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import connect.Connect;
import util.FlsLogger;

public class LogCredit extends Connect{
	
	private FlsLogger LOGGER = new FlsLogger(LogCredit.class.getName());
	
	public void addLogCredit(String user, int credit, String type, String description) throws SQLException{
		
		int days = 0;
		PreparedStatement stmt= null;
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, days);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(cal.getTime());
		
		LOGGER.info("Inside Log Credit");
		String sql = "insert into credit_log (credit_user_id,credit_date,credit_amount,credit_type,credit_desc) values (?,?,?,?,?)";
		Connection hcp = getConnectionFromPool();
		
		try {
			LOGGER.info("executing insert query on credit_log...");
			stmt = hcp.prepareStatement(sql);
			stmt.setString(1, user);
			stmt.setString(2, date);
			stmt.setInt(3, credit);
			stmt.setString(4, type);
			stmt.setString(5, description);
			
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			stmt.close();
			hcp.close();
		}
	}
}
