package util;

import java.util.logging.Level;
import java.util.logging.Logger;
import util.FlsConfig;

public class FlsLogger{
	
	private Logger logger;
	private String name;
	
	public FlsLogger(String n){
		this.logger = Logger.getLogger(n);
		this.name = n;
		
		// Below code specifies the level of the logger. Change value in
		// setLevel() function according to environment
				
		try {
			if(FlsConfig.env.equals("dev")){
				this.logger.setLevel(Level.WARNING);
			}else {
				this.logger.setLevel(Level.WARNING);
			}
		} catch (Exception e) {
		 e.printStackTrace();
			// TODO: handle exception
		}
		
		
	}
	
	public void warning(String msg){
		this.logger.warning("FROM " + this.name + " -----> " + msg);
	}
	
	public void info(String msg){
		this.logger.info("FROM " + this.name + " -----> " +msg);
	}
}
