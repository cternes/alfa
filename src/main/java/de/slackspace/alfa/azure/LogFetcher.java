package de.slackspace.alfa.azure;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.windowsazure.services.table.models.Entity;

import de.slackspace.alfa.domain.DeploymentEntry;
import de.slackspace.alfa.domain.DeploymentEntryMapper;
import de.slackspace.alfa.domain.LogEntry;
import de.slackspace.alfa.domain.LogEntryMapper;
import de.slackspace.alfa.domain.TableResultPartial;
import de.slackspace.alfa.elasticsearch.LogForwarder;
import de.slackspace.alfa.exception.ConnectionException;
import de.slackspace.alfa.properties.PropertyHandler;

public class LogFetcher implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogFetcher.class);
	private static final String LAST_PARTITION_KEY = "lastPartitionKey";
	private static final String LAST_ROW_KEY = "lastRowKey";
	private PropertyHandler propertyHandler;
	private AzureService service;
	private LogForwarder logForwarder;
	
	public LogFetcher(PropertyHandler propertyHandler, LogForwarder logForwarder, AzureService azureService) {
		if(propertyHandler == null) {
			throw new IllegalArgumentException("Argument propertyHandler must not be null");
		}
		if(logForwarder == null) {
			throw new IllegalArgumentException("Argument logForwarder must not be null");
		}
		if(azureService == null) {
			throw new IllegalArgumentException("Argument azureService must not be null");
		}
		
		this.propertyHandler = propertyHandler;
		this.logForwarder = logForwarder;
		this.service = azureService;
	}
	
	public void run() {
		Map<String, String> map = getDeploymentMap();
		fetchAndStoreLogs(map);
	}

	private Map<String, String> getDeploymentMap() {
		Map<String, String> deploymentMap = new HashMap<String, String>();
		TableResultPartial deploymentEntries = service.getDeploymentEntries();
		for (Entity entity : deploymentEntries.getEntryList()) {
			DeploymentEntry deploymentEntry = DeploymentEntryMapper.mapToLogEntry(entity);
			deploymentMap.put(deploymentEntry.getDeploymentId(), deploymentEntry.getName());
		}
		
		return deploymentMap;
	}

	private void fetchAndStoreLogs(Map<String, String> deploymentMap) {
		Properties properties = propertyHandler.readProperties();
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Fetching logs from " + properties.getProperty(LAST_ROW_KEY));
		}
		
		TableResultPartial tableResultPartial = service.getLogEntries(properties.getProperty(LAST_PARTITION_KEY), properties.getProperty(LAST_ROW_KEY));
		removeDuplicateEvents(tableResultPartial, properties);
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Found " + tableResultPartial.getEntryList().size() + " events.");
		}
		
		if(tableResultPartial.getEntryList().size() > 0) {
			storeEvents(tableResultPartial, deploymentMap);
			writeTraceProperties(properties, tableResultPartial);
		}
		
		tableResultPartial.getEntryList().clear();
		
		//if there are more entries in the azure table fetch until reaching the end
		if(tableResultPartial.getNextPartitionKey() != null) {
			fetchAndStoreLogs(deploymentMap);
		}
		else {
			return;
		}
	}

	private void removeDuplicateEvents(TableResultPartial tableResultPartial, Properties properties) {
		if(tableResultPartial.getEntryList().size() > 0) {
			Entity firstEvent = tableResultPartial.getEntryList().get(0);
			
			if(firstEvent.getPartitionKey().equals(properties.getProperty(LAST_PARTITION_KEY))
					&& firstEvent.getRowKey().equals(properties.getProperty(LAST_ROW_KEY))) {
				tableResultPartial.getEntryList().remove(firstEvent);
			}
		}
	}

	private void writeTraceProperties(Properties properties, TableResultPartial tableResultPartial) {
		//if we have reached the end of the azure table, remember the last fetched event
		if(tableResultPartial.getNextPartitionKey() == null) {
			Entity lastEvent = tableResultPartial.getEntryList().get(tableResultPartial.getEntryList().size() - 1);
			
			properties.setProperty(LAST_PARTITION_KEY, lastEvent.getPartitionKey());
			properties.setProperty(LAST_ROW_KEY, lastEvent.getRowKey());
		}
		//if not at the end of the azure table, remember the partition key of the next event
		else {
			properties.setProperty(LAST_PARTITION_KEY, tableResultPartial.getNextPartitionKey());
			properties.setProperty(LAST_ROW_KEY, tableResultPartial.getNextRowKey());
		}
		
		propertyHandler.writeProperties(properties);
		
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info("Fetched until partition: " + properties.getProperty(LAST_PARTITION_KEY));
			LOGGER.info("Fetched until row: " + properties.getProperty(LAST_ROW_KEY));
		}
	}

	private void storeEvents(TableResultPartial tableResultPartial, Map<String,String> deploymentMap) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Storing events into ES...");
		}
		
		for (Entity entity : tableResultPartial.getEntryList()) {
			LogEntry logEntry = LogEntryMapper.mapToLogEntry(entity, deploymentMap);
			
			try {
				logForwarder.pushEvent(logEntry);
			} catch (ConnectionException e) {
				LOGGER.error("Could not write event to ES. Error was: ", e);
			}
		}
	}
	
}
