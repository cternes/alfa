package de.slackspace.alfa.azure;

import java.util.Properties;

import com.microsoft.windowsazure.services.table.models.Entity;

import de.slackspace.alfa.domain.LogEntry;
import de.slackspace.alfa.domain.LogEntryMapper;
import de.slackspace.alfa.domain.TableResultPartial;
import de.slackspace.alfa.elasticsearch.LogForwarder;
import de.slackspace.alfa.exception.ConnectionException;
import de.slackspace.alfa.properties.PropertyHandler;

public class LogFetcher implements Runnable {

	private static final String LAST_PARTITION_KEY = "lastPartitionKey";
	private static final String LAST_ROW_KEY = "lastRowKey";
	private AzureService service;
	private LogForwarder logForwarder;
	
	public LogFetcher() throws ConnectionException {
		Properties properties = PropertyHandler.getInstance().readProperties();
		String accountName = properties.getProperty("accountName");
		String accountKey = properties.getProperty("accountKey");
		String accountUrl = properties.getProperty("accountUrl");
		
		if(accountName == null || accountName.isEmpty()) {
			throw new RuntimeException("The properties file is missing the accountName property.");
		}
		if(accountKey == null || accountKey.isEmpty()) {
			throw new RuntimeException("The properties file is missing the accountKey property.");
		}
		if(accountUrl == null || accountUrl.isEmpty()) {
			throw new RuntimeException("The properties file is missing the accountUrl property.");
		}
		
		service = AzureService.create(accountName, accountKey, accountUrl);
		logForwarder = new LogForwarder();
	}
	
	public void run() {
		fetchAndStoreLogs();
	}

	private void fetchAndStoreLogs() {
		Properties properties = PropertyHandler.getInstance().readProperties();
		TableResultPartial tableResultPartial = service.getLogEntries(properties.getProperty(LAST_PARTITION_KEY), properties.getProperty(LAST_ROW_KEY));
		
		System.out.println("Fetching logs from " + properties.getProperty(LAST_PARTITION_KEY));
		
		storeEvents(tableResultPartial);
		
		if(tableResultPartial.getNextPartitionKey() != null) {
			properties.setProperty(LAST_PARTITION_KEY, tableResultPartial.getNextPartitionKey());
			properties.setProperty(LAST_ROW_KEY, tableResultPartial.getNextRowKey());
			PropertyHandler.getInstance().writeProperties(properties);
			
			fetchAndStoreLogs();
		}
		else {
			return;
		}
	}

	private void storeEvents(TableResultPartial tableResultPartial) {
		for (Entity entity : tableResultPartial.getEntryList()) {
			LogEntry logEntry = LogEntryMapper.mapToLogEntry(entity);
			
			try {
				logForwarder.pushEvent(logEntry);
			} catch (ConnectionException e) {
				e.printStackTrace();
			}
		}
	}
	
}
