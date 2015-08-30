package de.slackspace.alfa.azure;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.windowsazure.services.table.models.Entity;

import de.slackspace.alfa.domain.DeploymentEntry;
import de.slackspace.alfa.domain.DeploymentEntryMapper;
import de.slackspace.alfa.domain.ElasticSearchEntry;
import de.slackspace.alfa.domain.EntryMapper;
import de.slackspace.alfa.domain.LogEntryMapper;
import de.slackspace.alfa.domain.PerformanceCounterMapper;
import de.slackspace.alfa.domain.TableResultPartial;
import de.slackspace.alfa.elasticsearch.LogForwarder;
import de.slackspace.alfa.exception.ConnectionException;
import de.slackspace.alfa.properties.PropertyHandler;

public class LogFetcher implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogFetcher.class);
	private PropertyHandler propertyHandler;
	private AzureService service;
	private LogForwarder logForwarder;
	private int instance;
	private int pollingIntervalMinutes;
	private boolean fetchPerformanceCounters;
	private String accountName;
	
	private final LogEntryMapper logEntryMapper = new LogEntryMapper();
	private final PerformanceCounterMapper performanceCounterMapper = new PerformanceCounterMapper();
	
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
		this.accountName = propertyHandler.getProperty(PropertyHandler.ACCOUNT_NAME, instance);
	}
	
	public void run() {
		Map<String, String> map = getDeploymentMap();

		// fetch logs
		fetchAndStoreEvents(map, "logs", AzureService.WADLOGSTABLE, logEntryMapper);
		
		if(fetchPerformanceCounters) {
			fetchAndStoreEvents(map, "performance counters", AzureService.PERFORMANCETABLE, performanceCounterMapper);
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
	
	private void fetchAndStoreEvents(Map<String, String> deploymentMap, String name, String storageTable, EntryMapper entryMapper) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("[" + accountName + "] - Fetching " + name + " from " + propertyHandler.getProperty(PropertyHandler.LAST_ROW_KEY, instance));
		}
		
		TableResultPartial tableResultPartial = service.getEntries(propertyHandler.getProperty(PropertyHandler.LAST_PARTITION_KEY, instance),
				propertyHandler.getProperty(PropertyHandler.LAST_ROW_KEY, instance), storageTable);
		removeDuplicateEvents(tableResultPartial);
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("[" + accountName + "] - Found " + tableResultPartial.getEntryList().size() + " events.");
		}
		
		if(tableResultPartial.getEntryList().size() > 0) {
			storeEvents(tableResultPartial, deploymentMap, entryMapper);
			writeTraceProperties(tableResultPartial);
		}
		
		tableResultPartial.getEntryList().clear();
		
		//if there are more entries in the azure table fetch until reaching the end
		if(tableResultPartial.getNextPartitionKey() != null) {
			fetchAndStoreEvents(deploymentMap, name, storageTable, entryMapper);
		}
		else {
			return;
		}
	}

	private void removeDuplicateEvents(TableResultPartial tableResultPartial) {
		if(tableResultPartial.getEntryList().size() > 0) {
			Entity firstEvent = tableResultPartial.getEntryList().get(0);
			
			if(firstEvent.getPartitionKey().equals(propertyHandler.getProperty(PropertyHandler.LAST_PARTITION_KEY, instance))
					&& firstEvent.getRowKey().equals(propertyHandler.getProperty(PropertyHandler.LAST_ROW_KEY, instance))) {
				tableResultPartial.getEntryList().remove(firstEvent);
			}
		}
	}

	private void writeTraceProperties(TableResultPartial tableResultPartial) {
		//if we have reached the end of the azure table, remember the last fetched event
		if(tableResultPartial.getNextPartitionKey() == null) {
			Entity lastEvent = tableResultPartial.getEntryList().get(tableResultPartial.getEntryList().size() - 1);
			
			propertyHandler.setProperty(PropertyHandler.LAST_PARTITION_KEY, lastEvent.getPartitionKey(), instance);
			propertyHandler.setProperty(PropertyHandler.LAST_ROW_KEY, lastEvent.getRowKey(), instance);
		}
		//if not at the end of the azure table, remember the partition key of the next event
		else {
			propertyHandler.setProperty(PropertyHandler.LAST_PARTITION_KEY, tableResultPartial.getNextPartitionKey(), instance);
			propertyHandler.setProperty(PropertyHandler.LAST_ROW_KEY, tableResultPartial.getNextRowKey(), instance);
		}
		
		propertyHandler.writeProperties();
		
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info("[" + accountName + "] - Fetched until partition: " + propertyHandler.getProperty(PropertyHandler.LAST_PARTITION_KEY, instance));
			LOGGER.info("[" + accountName + "] - Fetched until row: " + propertyHandler.getProperty(PropertyHandler.LAST_ROW_KEY, instance));
		}
	}

	private void storeEvents(TableResultPartial tableResultPartial, Map<String,String> deploymentMap, EntryMapper mapper) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("[" + accountName + "] - Storing events into ES...");
		}
		
		for (Entity entity : tableResultPartial.getEntryList()) {
			ElasticSearchEntry entry = mapper.mapToEntry(entity, deploymentMap);
			
			try {
				logForwarder.pushEvent(entry);
			} catch (ConnectionException e) {
				LOGGER.error("[" + accountName + "] - Could not write event to ES. Error was: ", e);
			} catch (IOException e) {
				LOGGER.error("[" + accountName + "] - Could not write create ES mapping. Error was: ", e);
			}
		}
	}

	public int getPollingIntervalMinutes() {
		return pollingIntervalMinutes;
	}

}
