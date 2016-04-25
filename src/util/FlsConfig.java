package util;



public class FlsConfig {

	//This is the build of the app, hardcoded here.
	//Increase it on every change taht needs a upgrade hook
	public final int appBuild = 2001;			

	
	
	public static int dbBuild = 0;		//This holds the build of the db, got from the database
	public static String env = null;	//This holds the env, got from the db
	
	String getEnv() {

		//TODO: select value from config where option = "env"
		
		//dummy return
		return "dev";
	}
	
	public boolean setEnv () {
		
		env = getEnv();
		
		if (env == "dev") {
			//TODO: set up dev properties
			return true;
		} else if (env == "live") {
			//TODO: set up live properties
			return true;
		} else {
			System.out.println("Invalid env");
			return false;
		}
	}
	
	int getDbBuild() {
		
		//TODO select value from config where option = "build"
		
		//dummy return. replace with real int value
		return 2000;		
	}
	
	public void setDbBuild() {
		
		dbBuild = getDbBuild();
		
		if (dbBuild < 2001) {
			//do new things - noop for this build
			
			//TODO: update config set value = 2001 where option = "build"
			dbBuild = 2001;
		}
		
		// add new build hooks one after the other in increasing order
		
		
	}
	
	
	
}
