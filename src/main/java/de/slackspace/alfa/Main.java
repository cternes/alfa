package de.slackspace.alfa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getName());
	
	public static void main(String[] args) {
		try {
			new Alfa();
		} catch (Exception e) {
			LOGGER.error("An error occurred", e);
			System.exit(-1);
		}
	}
}
