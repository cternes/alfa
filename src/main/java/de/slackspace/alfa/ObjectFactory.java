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
		
		for (int i = 1; i < propertyHandler.getNumberOfAccounts() + 1; i++) {
			AzureService azureService = createAzureService(propertyHandler, i);
			list.add(new LogFetcher(propertyHandler, logForwarder, azureService, i));
		}
		
		if(list.size() == 0) {
			throw new ConfigurationException(String.format("The properties file is missing a configured azure account. Please provide at least one property with name %s_1", PropertyHandler.ACCOUNT_URL));
		}
		
		return list;
	}
	
	private static AzureService createAzureService(PropertyHandler propertyHandler, int currentInstance) {
		String accountName = propertyHandler.getProperty(PropertyHandler.ACCOUNT_NAME, currentInstance);
		String accountKey = propertyHandler.getProperty(PropertyHandler.ACCOUNT_KEY, currentInstance);
		String accountUrl = propertyHandler.getProperty(PropertyHandler.ACCOUNT_URL, currentInstance);

		if(accountName == null || accountName.isEmpty()) {
			throw new ConfigurationException(String.format("The properties file is missing the %s_%s property.", PropertyHandler.ACCOUNT_NAME, currentInstance));
		}
		if(accountKey == null || accountKey.isEmpty()) {
			throw new ConfigurationException(String.format("The properties file is missing the %s_%s property.", PropertyHandler.ACCOUNT_KEY, currentInstance));
		}
		if(accountUrl == null || accountUrl.isEmpty()) {
			throw new ConfigurationException(String.format("The properties file is missing the %s_%s property.", PropertyHandler.ACCOUNT_URL, currentInstance));
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Azure account '%s' added to monitored accounts", accountName));
		}

		Configuration config = Configuration.getInstance();
		config.setProperty(TableConfiguration.ACCOUNT_NAME, accountName);
		config.setProperty(TableConfiguration.ACCOUNT_KEY, accountKey);
		config.setProperty(TableConfiguration.URI, accountUrl);
		TableContract contract = TableService.create(config);

		return new AzureService(contract);
	}
}
