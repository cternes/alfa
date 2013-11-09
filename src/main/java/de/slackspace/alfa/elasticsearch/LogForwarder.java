package de.slackspace.alfa.elasticsearch;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.google.gson.Gson;

import de.slackspace.alfa.domain.LogEntry;
import de.slackspace.alfa.exception.ConnectionException;

public class LogForwarder {

	private static final String HOST = "localhost";
	private static final int PORT = 9300;
	
	private Client client;
	
	public LogForwarder() throws ConnectionException {
	    try{
	        client = new TransportClient().addTransportAddress(new InetSocketTransportAddress(HOST, PORT));
	    }
	    catch(Exception e) {
	    	throw new ConnectionException("Could not connect to elasticsearch", e);
	    }
	}
	
	public void pushEvent(LogEntry entry) throws ConnectionException {
		Gson gson = new Gson();
		String json = gson.toJson(entry);
		
		client.prepareIndex("logs-" + entry.getElasticIndex(), entry.getSeverity())
                .setSource(json)
                .setOperationThreaded(false)
                .execute()
                .actionGet();
	}
}
