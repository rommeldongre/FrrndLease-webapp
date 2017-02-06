package util;

import connect.Connect;

public class FlsBot extends Connect{
	
	private FlsLogger LOGGER = new FlsLogger(FlsBot.class.getName());
	
	public String sendBotMessage(String userId, String text){
		String botMessage = null;
		
		LOGGER.info("Text from page is "+text);
		if(text.toLowerCase().contains("test")){
			botMessage = "Test Reply";
		}else{
			botMessage = "Thanks for showing interest in FrrndLease..We'll get back to you";
		}
		
		return botMessage;
		
	}
	

}
