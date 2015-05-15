package de.slackspace.alfa.azure;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
	private static final String LAST_PARTITION_KEY_PERFORMANCE = "lastPartitionKey_performance";
	private static final String LAST_ROW_KEY_PERFORMANCE = "lastRowKey_performance";
	private PropertyHandler propertyHandler;
	private AzureService service;
	private LogForwarder logForwarder;
	private int instance;
	private int pollingIntervalMinutes;
	private boolean fetchPerformanceCounters;
	
	public LogFetcher(PropertyHandler propertyHandler, LogForwarder logForwarder, AzureService azureService,
			int instance, int pollingIntervalMinutes, boolean fetchPerformanceCounters) {
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
		this.instance = instance;
		this.pollingIntervalMinutes = pollingIntervalMinutes;
		this.fetchPerformanceCounters = fetchPerformanceCounters;
	}
	
	public void run() {
		Map<String, String> map = getDeploymentMap();

		// fetch logs
		fetchAndStoreEvents(map, "logs", LAST_ROW_KEY, LAST_PARTITION_KEY, AzureService.WADLOGSTABLE);
		
		if(fetchPerformanceCounters) {
			fetchAndStoreEvents(map, "performance counters", LAST_ROW_KEY_PERFORMANCE, LAST_PARTITION_KEY_PERFORMANCE, AzureService.PERFORMANCETABLE);
		}
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
	
	private void fetchAndStoreEvents(Map<String, String> deploymentMap, String name, String lastRowKeyProperty, String lastPartitionKeyProperty, String storageTable) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Fetching " + name + " from " + propertyHandler.getProperty(lastRowKeyProperty, instance));
		}
		
		TableResultPartial tableResultPartial = service.getEntries(propertyHandler.getProperty(lastPartitionKeyProperty, instance),
				propertyHandler.getProperty(lastRowKeyProperty, instance), storageTable);
		removeDuplicateEvents(tableResultPartial);
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Found " + tableResultPartial.getEntryList().size() + " events.");
		}
		
		if(tableResultPartial.getEntryList().size() > 0) {
			storeEvents(tableResultPartial, deploymentMap);
			writeTraceProperties(tableResultPartial, lastRowKeyProperty, lastPartitionKeyProperty);
		}
		
		tableResultPartial.getEntryList().clear();
		
		//if there are more entries in the azure table fetch until reaching the end
		if(tableResultPartial.getNextPartitionKey() != null) {
			fetchAndStoreEvents(deploymentMap, name, lastRowKeyProperty, lastPartitionKeyProperty, storageTable);
		}
		else {
			return;
		}
	}

	private void removeDuplicateEvents(TableResultPartial tableResultPartial) {
		if(tableResultPartial.getEntryList().size() > 0) {
			Entity firstEvent = tableResultPartial.getEntryList().get(0);
			
			if(firstEvent.getPartitionKey().equals(propertyHandler.getProperty(LAST_PARTITION_KEY, instance))
					&& firstEvent.getRowKey().equals(propertyHandler.getProperty(LAST_ROW_KEY, instance))) {
				tableResultPartial.getEntryList().remove(firstEvent);
			}
		}
	}

	private void writeTraceProperties(TableResultPartial tableResultPartial, String lastRowKeyProperty, String lastPartitionKeyProperty) {
		//if we have reached the end of the azure table, remember the last fetched event
		if(tableResultPartial.getNextPartitionKey() == null) {
			Entity lastEvent = tableResultPartial.getEntryList().get(tableResultPartial.getEntryList().size() - 1);
			
			propertyHandler.setProperty(lastPartitionKeyProperty, lastEvent.getPartitionKey(), instance);
			propertyHandler.setProperty(lastRowKeyProperty, lastEvent.getRowKey(), instance);
		}
		//if not at the end of the azure table, remember the partition key of the next event
		else {
			propertyHandler.setProperty(lastPartitionKeyProperty, tableResultPartial.getNextPartitionKey(), instance);
			propertyHandler.setProperty(lastRowKeyProperty, tableResultPartial.getNextRowKey(), instance);
		}
		
		propertyHandler.writeProperties();
		
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info("Fetched until partition: " + propertyHandler.getProperty(lastPartitionKeyProperty, instance));
			LOGGER.info("Fetched until row: " + propertyHandler.getProperty(lastRowKeyProperty, instance));
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
			} catch (IOException e) {
				LOGGER.error("Could not write create ES mapping. Error was: ", e);
			}
		}
	}

	public int getPollingIntervalMinutes() {
		return pollingIntervalMinutes;
	}

}
