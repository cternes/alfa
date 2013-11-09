package de.slackspace.alfa;

import de.slackspace.alfa.azure.LogFetcher;
import de.slackspace.alfa.exception.ConnectionException;

public class Alfa {

	private LogFetcher logFetcher = new LogFetcher();
	
	public Alfa() throws ConnectionException {
		fetchAndStoreLogs();
	}
	
	private void fetchAndStoreLogs() {
		logFetcher.run();
		
		//TODO: sleep here
	}
	
}
