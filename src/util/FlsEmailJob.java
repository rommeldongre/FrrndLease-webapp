package util;

import connect.Connect;
import util.Event.Delivery_Status;

import org.quartz.*;

@DisallowConcurrentExecution
public class FlsEmailJob extends Connect implements org.quartz.Job {
	
	private FlsLogger LOGGER = new FlsLogger(FlsEmailJob.class.getName());

    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.warning("Fls Email Job Started");
        try {
        	
        	Event event = new Event();
        	int id = event.getNextUndeliveredEvent();
        	while(id != -1){
        		if(event.SendNotifications(id))
        			event.changeDeliveryStatus(id, Delivery_Status.FLS_DELIVERED);
        		id = event.getNextUndeliveredEvent();
        	}
        		
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.warning("Fls Email Job Failed");
		} finally{
			LOGGER.warning("Fls Email Job Ended");
		}
    }
}
	