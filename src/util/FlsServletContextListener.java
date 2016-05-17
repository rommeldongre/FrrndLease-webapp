package services;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

import util.FlsConfig;

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
		System.out.println("=====> Startup code called");

		// Notification that the web application initialization process is
		// starting
	}

}
