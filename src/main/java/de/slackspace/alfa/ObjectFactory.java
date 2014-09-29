package de.slackspace.alfa;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.table.TableConfiguration;
import com.microsoft.windowsazure.services.table.TableContract;
import com.microsoft.windowsazure.services.table.TableService;

import de.slackspace.alfa.azure.AzureService;
import de.slackspace.alfa.azure.LogFetcher;
import de.slackspace.alfa.elasticsearch.LogForwarder;
import de.slackspace.alfa.exception.ConfigurationException;
import de.slackspace.alfa.properties.PropertyHandler;
import de.slackspace.alfa.properties.PropertyHandlerFactory;

public class ObjectFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(ObjectFactory.class);
	
	private ObjectFactory() {} 
	
	public static List<LogFetcher> constructLogFetcher(String configFile, Client client) {
		PropertyHandler propertyHandler = PropertyHandlerFactory.createPropertyHandler(configFile);
		LogForwarder logForwarder = new LogForwarder(client);
		
		List<LogFetcher> list = new ArrayList<>();
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("=== Starting alfa with these accounts ===");
		}
		
		for (int i = 1; i < propertyHandler.getNumberOfAccounts() + 1; i++) {
			int pollingIntervalMinutes = getPollingIntervalMinutes(i, propertyHandler.getProperty(PropertyHandler.POLLING_INTERVAL, i));
			AzureService azureService = createAzureService(propertyHandler, i);
			list.add(new LogFetcher(propertyHandler, logForwarder, azureService, i, pollingIntervalMinutes));
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("=========================================");
		}
		
		if(list.size() == 0) {
			throw new ConfigurationException(String.format("The properties file is missing a configured azure account. Please provide at least one property with name %s_1", PropertyHandler.ACCOUNT_NAME));
		}
		
		return list;
	}
	
	private static AzureService createAzureService(PropertyHandler propertyHandler, int currentInstance) {
		String accountName = propertyHandler.getProperty(PropertyHandler.ACCOUNT_NAME, currentInstance);
		String accountKey = propertyHandler.getProperty(PropertyHandler.ACCOUNT_KEY, currentInstance);
		String maxLogDays = propertyHandler.getProperty(PropertyHandler.MAX_LOG_DAYS, currentInstance);
		String pollingInterval = propertyHandler.getProperty(PropertyHandler.POLLING_INTERVAL, currentInstance);
		String accountUrl = propertyHandler.getAccountUrl(accountName);

		if(accountName == null || accountName.isEmpty()) {
			throw new ConfigurationException(String.format("The properties file is missing the %s.%s property.", PropertyHandler.ACCOUNT_NAME, currentInstance));
		}
		if(accountKey == null || accountKey.isEmpty()) {
			throw new ConfigurationException(String.format("The properties file is missing the %s.%s property.", PropertyHandler.ACCOUNT_KEY, currentInstance));
		}
		
		int maxLogDaysAsInteger = getMaxLogDays(currentInstance, maxLogDays);
		int pollingIntervalMinutesAsInteger = getPollingIntervalMinutes(currentInstance, pollingInterval);
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("    %s. Accountname: %s", currentInstance, accountName));
			LOGGER.debug(String.format("        Accounturl: %s", accountUrl));
			LOGGER.debug(String.format("        MaxLogDays: %s", maxLogDaysAsInteger));
			LOGGER.debug(String.format("        PollingIntervalMinutes: %s", pollingIntervalMinutesAsInteger));
		}

		Configuration config = Configuration.getInstance();
		config.setProperty(TableConfiguration.ACCOUNT_NAME, accountName);
		config.setProperty(TableConfiguration.ACCOUNT_KEY, accountKey);
		config.setProperty(TableConfiguration.URI, accountUrl);
		TableContract contract = TableService.create(config);

		return new AzureService(contract, maxLogDaysAsInteger);
	}

	private static int getMaxLogDays(int currentInstance, String maxLogDays) {
		int maxLogDaysAsInteger = 10; //default value is 10
		
		if(maxLogDays != null && !maxLogDays.isEmpty()) {
			try {
				maxLogDaysAsInteger = Integer.parseInt(maxLogDays);
			}
			catch(NumberFormatException e) {
				throw new ConfigurationException(String.format("The property %s.%s is provided but not as integer. Please provide an integer value.", PropertyHandler.MAX_LOG_DAYS, currentInstance));	
			}
		}
		
		return maxLogDaysAsInteger;
	}
	
	private static int getPollingIntervalMinutes(int currentInstance, String pollingInterval) {
		int pollingIntervalAsInteger = 2; //default value is 2
		
		if(pollingInterval != null && !pollingInterval.isEmpty()) {
			try {
				pollingIntervalAsInteger = Integer.parseInt(pollingInterval);
			}
			catch(NumberFormatException e) {
				throw new ConfigurationException(String.format("The property %s.%s is provided but not as integer. Please provide an integer value.", PropertyHandler.POLLING_INTERVAL, currentInstance));	
			}
		}
		
		return pollingIntervalAsInteger;
	}
}
