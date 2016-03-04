package connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import errorCat.ErrorCat;

public class Connect extends ErrorCat{
	
	protected static Connection connection = null;
	
	//Local - Database 
	private static String url = "jdbc:mysql://127.0.0.1:3306/fls";
	private static String name = "root";
	private static String pass = "root";
			
	//Amazon RDS Database
	//private static String url = "jdbc:mysql://greylabsdb.c2dfmnaqzg4x.ap-southeast-1.rds.amazonaws.com:3306/fls";
	//private static String name = "awsuser";
	//private static String pass = "greylabs123";

	//JDBC Driver
	private static String driver = "com.mysql.jdbc.Driver";
	
	protected static /*Connection*/void getConnection() {
		
		if (connection == null){
			System.out.println("Registering driver....");
			try {
				//Driver Registration
				Class.forName(driver).newInstance();
				System.out.println("Driver Registered successfully!!.");
				
				//Initiate a connection
				System.out.println("Connecting to database...");
				connection = DriverManager.getConnection(url, name, pass);
				System.out.println("Connected to database!!!");
				
			} catch (InstantiationException | IllegalAccessException
					| ClassNotFoundException e) {
				e.printStackTrace();
				System.out.println("Couldnt register driver...");
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Couldnt connect to database...");
			}
		}else {
			System.out.println("Connection exists....");
			
		}
		
		//return connection;
	}
	
	/*public static void main (String [] args){
		Connection con = new Connect().getConnection();
	}*/

}
