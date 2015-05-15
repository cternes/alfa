package de.slackspace.alfa;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.slackspace.alfa.azure.LogFetcher;
import de.slackspace.alfa.elasticsearch.ElasticSearchServer;
import de.slackspace.alfa.elasticsearch.LogCleaner;
import de.slackspace.alfa.exception.ConnectionException;
import de.slackspace.alfa.properties.PropertyHandler;
import de.slackspace.alfa.properties.PropertyHandlerFactory;

public class Alfa {

	private static final Logger LOGGER = LoggerFactory.getLogger(Alfa.class);
	private static final String ALFA_CONFIG = "conf/alfa.properties";
	private static final String ELASTICSEARCH_CONFIG = "conf/elasticsearch-server.properties";
	private ElasticSearchServer elasticSearchServer;
	private List<LogFetcher> logFetchers;
	private LogCleaner logCleaner;
	
	public void start(boolean runAsService) throws IOException, ConnectionException {
		startElasticSearchServer(runAsService);
		
		String configFile = getAlfaConfig(runAsService);
		
		initializeLogFetcher(configFile);
		initializeLogCleaner(configFile);
		fetchAndStoreLogs();
	}
	
	public void stop() {
		if(elasticSearchServer != null) {
			elasticSearchServer.stop();
		}
	}

	private void initializeLogFetcher(String configFile) throws ConnectionException {
		this.logFetchers = ObjectFactory.constructLogFetcher(configFile, elasticSearchServer.getClient());
	}

	private void initializeLogCleaner(String configFile) {
		PropertyHandler propertyHandler = PropertyHandlerFactory.createPropertyHandler(configFile);
		this.logCleaner = ObjectFactory.constructLogCleaner(propertyHandler, elasticSearchServer.getClient());
	}
	
	private String getAlfaConfig(boolean runAsService) {
		String configFile = ALFA_CONFIG;
		if(runAsService) {
			configFile = "../../" + configFile;
		}
		return configFile;
	}

	private void startElasticSearchServer(boolean runAsService) throws IOException {
		String configFile = ELASTICSEARCH_CONFIG;
		if(runAsService) {
			configFile = "../../" + configFile;
		}
				
		Properties config = new Properties();
		config.load(new FileReader(configFile));
		
		elasticSearchServer = new ElasticSearchServer(config);
		elasticSearchServer.start();
	}
	
	private void fetchAndStoreLogs() {
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(logFetchers.size());
		
		//this will execute the logFetcher every n minutes, if a logFetcher run exceeds n minutes, 
		//the next start will be blocked until the first one has finished 
		for (LogFetcher logFetcher : logFetchers) {
			ScheduledFuture<?> logFetcherHandle = scheduledExecutorService.scheduleAtFixedRate(logFetcher, 0, logFetcher.getPollingIntervalMinutes(), TimeUnit.MINUTES);
			
			try {
				logFetcherHandle.get();
			}
			catch(Exception e) {
				LOGGER.error("Uncaught exception", e);
			}
		}
		
		//this will execute the logCleaner every 24 hours
		ScheduledFuture<?> logCleanerHandle = scheduledExecutorService.scheduleAtFixedRate(logCleaner, 0, 24, TimeUnit.HOURS);
		
		try {
			logCleanerHandle.get();
		}
		catch(Exception e) {
			LOGGER.error("Uncaught exception", e);
		}
	}

}
