package de.slackspace.alfa.elasticsearch;

import org.elasticsearch.client.Client;

import com.google.gson.Gson;

import de.slackspace.alfa.domain.LogEntry;
import de.slackspace.alfa.exception.ConnectionException;

public class LogForwarder {

	private Client client;
	private Gson gson = new Gson();
	
	public LogForwarder(Client client) {
		this.client = client;
	}
	
	public void pushEvent(LogEntry entry) throws ConnectionException {
		String json = gson.toJson(entry);
		
		client.prepareIndex("logs-" + entry.getElasticIndex(), entry.getSeverity())
                .setSource(json)
                .setOperationThreaded(false)
                .execute()
                .actionGet();
	}
}
