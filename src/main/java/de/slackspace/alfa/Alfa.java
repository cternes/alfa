package de.slackspace.alfa;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.slackspace.alfa.azure.LogFetcher;
import de.slackspace.alfa.elasticsearch.ElasticSearchServer;
import de.slackspace.alfa.exception.ConnectionException;

public class Alfa {

	private static final String ELASTICSEARCH_CONFIG = "elasticsearch-server.properties";
	private ElasticSearchServer elasticSearchServer;
	private LogFetcher logFetcher;
	
	public void start(boolean runAsService) throws IOException, ConnectionException {
		startElasticSearchServer();
		initializeLogFetcher(runAsService);
		fetchAndStoreLogs();
	}
	
	public void stop() {
		if(elasticSearchServer != null) {
			elasticSearchServer.stop();
		}
	}

	private void initializeLogFetcher(boolean runAsService) throws ConnectionException {
		String configFile = "conf/alfa.properties";
		if(runAsService) {
			configFile = "../../" + configFile;
		}
		
		this.logFetcher = ObjectFactory.constructLogFetcher(configFile, elasticSearchServer.getClient());
	}

	private void startElasticSearchServer() throws IOException {
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(ELASTICSEARCH_CONFIG);
		Properties config = new Properties();
		config.load(inputStream);
		
		elasticSearchServer = new ElasticSearchServer(config);
		elasticSearchServer.start();
	}
	
	private void fetchAndStoreLogs() {
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
		
		//this will execute the logfetcher every 2 minutes, if a logfetcher run exceeds 2 minutes, 
		//the next start will be blocked until the first one has finished 
		scheduledExecutorService.scheduleAtFixedRate(logFetcher, 0, 2, TimeUnit.MINUTES);
	}

}
