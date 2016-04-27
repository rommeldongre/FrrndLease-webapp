package util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connect.Connect;

public class FlsConfig extends Connect{

	//This is the build of the app, hardcoded here.
	//Increase it on every change taht needs a upgrade hook
	public final int appBuild = 2001;			

	public static int dbBuild = 0;		//This holds the build of the db, got from the database
	public static String env = null;	//This holds the env, got from the db
	
	
	String getEnv() {

		//TODO: select value from config where option = "env"
		try {
			getConnection();
			String sql = null;
			PreparedStatement sql_stmt = null;
			sql = "SELECT value FROM `config` WHERE config.option='env'";
			sql_stmt = connection.prepareStatement(sql);
			
			ResultSet dbResponse = sql_stmt.executeQuery();
			System.out.println("executed query");
			
			if(dbResponse.next()){
				env = dbResponse.getString("value");
				
			}else {
				env ="Env is null";
			}
			
		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		} 
		
		if(env== null){
			System.out.println("env variable is null");
		}else{
		System.out.println(env);
		}
		
		//env return
		return env;
	}
	
	public boolean setEnv () {
		
		env = getEnv();
		//env = env.toString();
		
		/*if (env.equals("dev")) {
			//TODO: set up dev properties
			System.out.println("valid env: "+env);
			return true;
		} else if (env == "live") {
			//TODO: set up live properties
			return true;
		} else {
			System.out.println("Invalid env: "+env);
			return false;
		}*/
		return true;
	}
	
	int getDbBuild() {
		
		//TODO select value from config where option = "build"
		try {
			getConnection();
			String build = null;
			String sql = null;
			PreparedStatement sql_stmt = null;
			sql = "SELECT value FROM `config` WHERE config.option='build'";
			sql_stmt = connection.prepareStatement(sql);
			
			ResultSet dbResponse = sql_stmt.executeQuery();
			//System.out.println("executed build query");
			
			if(dbResponse.next()){
				build = dbResponse.getString("value");
				dbBuild = Integer.parseInt(build);
				
				//System.out.println(dbBuild);
				
			}else {
				//env ="Env is null";
			}
			
		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		} 
			
		return dbBuild;
	}
	
	public void setDbBuild() {
		
		dbBuild = getDbBuild();
		
		if (dbBuild < 2001) {
			//do new things - noop for this build
			
			//TODO: update config set value = 2001 where option = "build"
			dbBuild = 2001;
		}
		if(dbBuild == 2001){
			System.out.println("dbBuild in sync");
		}
		
		// add new build hooks one after the other in increasing order
		
		
	}
	
	
	
}
