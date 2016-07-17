package util;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import org.quartz.*;


public class FlsJob implements org.quartz.Job {

      public FlsJob() {
      }

      public void execute(JobExecutionContext context) throws JobExecutionException {
          System.err.println("Its Showtime!!  FlsJob is now executing ...");
      }
}
	