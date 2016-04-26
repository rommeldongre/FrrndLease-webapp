package connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import errorCat.ErrorCat;
import util.FlsLogger;

public class Connect extends ErrorCat {

	protected static Connection connection = null;

	private static FlsLogger LOGGER = new FlsLogger(Connect.class.getName());

	// Local - Database
	private static String url = "jdbc:mysql://127.0.0.1:3306/fls";
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
			LOGGER.info("Registering driver....");
			try {
				// Driver Registration
				Class.forName(driver).newInstance();
				LOGGER.info("Driver Registered successfully!!.");

				// Initiate a connection
				LOGGER.info("Connecting to database...");
				connection = DriverManager.getConnection(url, name, pass);
				LOGGER.info("Connected to database!!!");

			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				e.printStackTrace();
				LOGGER.info("Couldnt register driver...");
			} catch (SQLException e) {
				e.printStackTrace();
				LOGGER.info("Couldnt connect to database...");
			}
		} else {
			LOGGER.info("Connection exists....");

		}
	}

	/*
	 * public static void main (String [] args){ Connection con = new
	 * Connect().getConnection(); }
	 */

}
