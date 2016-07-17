package util;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import org.quartz.*;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;

public class FlsServletContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// Notification that the servlet context is about to be shut down.
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// do all the tasks that you need to perform just after the server
		// starts
		FlsConfig c = new FlsConfig();
		c.setEnv();
		c.setDbBuild();
		//Can't use logger in startup code.
		System.out.println("=====> Database reconciled ...");

		try {
			// Grab the Scheduler instance from the Factory
  			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
	  			
	  		// and start it off
	  		scheduler.start();
	
	 		// define the job and tie it to our FlsJob class
	  		JobDetail job = newJob(FlsJob.class)
	      					.withIdentity("FlsJob1", "FlsGroup1")
	      					.build();
	
	 
	  		//For testing every 30 seconds.
	  		/*
	  		Trigger trigger = newTrigger()
	      					.withIdentity("FlsTrigger1", "FlsGroup1")
	      						.startNow()
	      						.withSchedule(simpleSchedule()
	      								.withIntervalInSeconds(30)
	      								.repeatForever())
	      						.build();
	      	*/
	  					   
	     	//For production every day at 5am			   
	      	Trigger trigger = newTrigger()
	      						    .withIdentity("FlsTrigger1", "FlsGroup1")
	      						    .startNow()
	      						    .withSchedule(simpleSchedule()
	      						    			.withIntervalInHours(24)
	      						    			.repeatForever())
	      						    .build();
	  		
	  		
	  		// Tell quartz to schedule the job using our trigger
	  		scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}

		System.out.println("=====> Scheduler started ...");

		// Notification that the web application initialization process is
		// starting
		System.out.println("=====> Startup code called");
	}

}
