package connect;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import errorCat.ErrorCat;

public class Connect extends ErrorCat {

	protected static Connection connection = null;
	protected static HikariDataSource HikariDS = null;

	//Cannot use LOGGER class because it is being used on startup 
	//private static FlsLogger LOGGER = new FlsLogger(Connect.class.getName());

	// Local - Database
	private static String url = "jdbc:mysql://127.0.0.1:3306/fls?autoReconnect=true";
	private static String name = "root";
	private static String pass = "root";

	// Amazon RDS Database
	// private static String url =
	// "jdbc:mysql://greylabsdb.c2dfmnaqzg4x.ap-southeast-1.rds.amazonaws.com:3306/fls";
	// private static String name = "awsuser";
	// private static String pass = "greylabs123";

	// JDBC Driver
	private static String driver = "com.mysql.jdbc.Driver";

	protected static /* Connection */void getConnection() {

		if (connection == null) {
			try {
				// Driver Registration
				Class.forName(driver).newInstance();

				// Initiate a connection
				connection = DriverManager.getConnection(url, name, pass);
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				System.out.println("Couldnt register driver...");
				e.printStackTrace();
			} catch (SQLException e) {
				System.out.println("Couldnt connect to database...");
				e.printStackTrace();
			}
		} else {
			System.out.println("Connection exists....");

		}
	}

	/*
	 * public static void main (String [] args){ Connection con = new
	 * Connect().getConnection(); }
	 */


    public Connection getConnectionFromPool() {
    	
    	Connection conn = null;
    	try {
    		DataSource ds = getDataSource();
    		conn = ds.getConnection();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    	return conn;
    }

    private DataSource getDataSource() {
    	if (HikariDS == null) {
    		HikariConfig config = new HikariConfig();
    		config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/fls");
    		config.setUsername("root");
    		config.setPassword("root");
    		config.setMaximumPoolSize(10);
    		config.setMinimumIdle(2);
    		config.setIdleTimeout(10);
    		config.setConnectionTimeout(5000);
    		config.setValidationTimeout(1000);
    		
    		HikariDS = new HikariDataSource(config);
    	}
    	return HikariDS;
    }
    
}
