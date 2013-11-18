package de.slackspace.alfa;

import java.util.Properties;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.table.TableConfiguration;
import com.microsoft.windowsazure.services.table.TableContract;
import com.microsoft.windowsazure.services.table.TableService;

import de.slackspace.alfa.azure.AzureService;
import de.slackspace.alfa.azure.LogFetcher;
import de.slackspace.alfa.elasticsearch.LogForwarder;
import de.slackspace.alfa.exception.ConfigurationException;
import de.slackspace.alfa.properties.PropertyHandler;

public class ObjectFactory {

	private ObjectFactory() {} 
	
	public static LogFetcher constructLogFetcher(String configFile) {
		PropertyHandler propertyHandler = new PropertyHandler(configFile);
		LogForwarder logForwarder = new LogForwarder();
		AzureService azureService = createAzureService(propertyHandler);
		
		return new LogFetcher(propertyHandler, logForwarder, azureService);
	}
	
	private static AzureService createAzureService(PropertyHandler propertyHandler) {
		Properties properties = propertyHandler.readProperties();
		String accountName = properties.getProperty("accountName");
		String accountKey = properties.getProperty("accountKey");
		String accountUrl = properties.getProperty("accountUrl");

		if(accountName == null || accountName.isEmpty()) {
			throw new ConfigurationException("The properties file is missing the accountName property.");
		}
		if(accountKey == null || accountKey.isEmpty()) {
			throw new ConfigurationException("The properties file is missing the accountKey property.");
		}
		if(accountUrl == null || accountUrl.isEmpty()) {
			throw new ConfigurationException("The properties file is missing the accountUrl property.");
		}

		Configuration config = Configuration.getInstance();
		config.setProperty(TableConfiguration.ACCOUNT_NAME, accountName);
		config.setProperty(TableConfiguration.ACCOUNT_KEY, accountKey);
		config.setProperty(TableConfiguration.URI, accountUrl);
		TableContract contract = TableService.create(config);

		return new AzureService(contract);
	}
}
