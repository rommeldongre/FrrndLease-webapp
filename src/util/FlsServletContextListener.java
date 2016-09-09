package util;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class FlsServletContextListener implements ServletContextListener {

	 private Scheduler scheduler;
	 
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// Notification that the servlet context is about to be shut down.
		try {
			scheduler.shutdown(true);	
		} catch (SchedulerException e) {
			// TODO: handle exception
		}
		
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
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			    
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
	      						    .withSchedule(CronScheduleBuilder.cronSchedule("0 0 1 * * ?"))
	      						    .build();
	  		
	      	// Tell quartz to schedule the job using our trigger
	  		scheduler.scheduleJob(job, trigger);
	      	
	      	// FlsEmailJob key
	      	JobKey jobKey = JobKey.jobKey("FlsEmailJob", "FlsEmailGroup");
	      	// FlsEmailJob declaration
	      	JobDetail emailJob = newJob(FlsEmailJob.class).withIdentity(jobKey).storeDurably().build();
	      	// adding the job to the scheduler without any trigger
	  		scheduler.addJob(emailJob, true);
	  		
	  		// FlsMatchFbId key
	      	JobKey matchjobKey = JobKey.jobKey("FlsMatchFbIdJob", "FlsMatchFbIdGroup");
	      	// FlsEmailJob declaration
	      	JobDetail matchFbJob = newJob(FlsMatchFbIdJob.class).withIdentity(matchjobKey).storeDurably().build();
	      	// adding the job to the scheduler without any trigger
	  		scheduler.addJob(matchFbJob, true);
	  		
		} catch (SchedulerException e) {
			e.printStackTrace();
		}

		System.out.println("=====> Scheduler started ...");
		
		// Notification that the web application initialization process is
		// starting
		System.out.println("=====> Startup code called");
	}
	
}
