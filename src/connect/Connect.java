package connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import errorCat.ErrorCat;

public class Connect extends ErrorCat {

	protected static Connection connection = null;

	//Cannot use LOGGER class because it is being used on startup 
	//private static FlsLogger LOGGER = new FlsLogger(Connect.class.getName());

	// Local - Database
	private static String url = "jdbc:mysql://127.0.0.1:3306/fls";
	private static String name = "root";
	private static String pass = "";

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

}
