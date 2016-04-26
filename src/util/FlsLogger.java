package util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FlsLogger{
	
	private Logger logger;
	
	public FlsLogger(String name){
		this.logger = Logger.getLogger(name);
		
		// Below code specifies the level of the logger. Change value in
		// setLevel() function according to environment
		
		this.logger.setLevel(Level.WARNING);
	}
	
	public void warning(String msg){
		this.logger.warning(msg);
	}
	
	public void info(String msg){
		this.logger.info(msg);
	}
}