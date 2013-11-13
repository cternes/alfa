package de.slackspace.alfa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

	private static final String SERVICE_START = "serviceStart";
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getName());
	private static Alfa alfa;

	public static void main(String[] args) {
		String cmd = parseArguments(args);
		
		try {
			alfa = new Alfa();
			
			if(SERVICE_START.equals(cmd)) {
				LOGGER.info("Starting in service mode");
				alfa.start(true);
			}
			else {
				LOGGER.info("Starting in command line mode");
				alfa.start(false);
			}
			
		} catch (Exception e) {
			LOGGER.error("An error occurred.", e);
		}
	}
	
	public static void stop(String[] args) {
		LOGGER.info("Shutting down...");
		alfa.stop();
		
		System.exit(0);
	}
	
	private static String parseArguments(String[] args) {
		String cmd = "start";
		if(args.length > 0) {
			cmd = args[0];
		}
		return cmd;
	}
}
