package de.slackspace.alfa.elasticsearch;

import java.io.IOException;
import java.util.Iterator;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import de.slackspace.alfa.domain.ElasticSearchEntry;
import de.slackspace.alfa.exception.ConnectionException;

public class LogForwarder {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogForwarder.class);
	
	private Client client;
	private Gson gson = new Gson();
	
	public LogForwarder(Client client) {
		this.client = client;
	}
	
	public void pushEvent(ElasticSearchEntry entry) throws ConnectionException, IOException {
		String json = gson.toJson(entry);
		String index = entry.getElasticIndex();
		
		if(!isIndexExisting(index)) {
			createIndexMapping(entry, index);
		}
		
		client.prepareIndex(index, entry.getType())
                .setSource(json)
                .execute()
                .actionGet();
	}
	
	private void createIndexMapping(ElasticSearchEntry entry, String index) throws IOException {
        CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate(index);
        
        Iterator<String> iter = entry.getIndexMappings().keySet().iterator();
        while(iter.hasNext()) {
        	String key = iter.next();
        	XContentBuilder value = entry.getIndexMappings().get(key);
        	
        	createIndexRequestBuilder.addMapping(key, value);
        }
        
        try {
        	createIndexRequestBuilder.execute().actionGet();
        }
        catch (ElasticsearchException e) {
        	LOGGER.error("Could not create Index mapping for index " + index, e);
        }
	}
	
	private boolean isIndexExisting(String index) {
		IndicesExistsRequest request = new IndicesExistsRequest(index);
		IndicesExistsResponse response = client.admin().indices().exists(request).actionGet();
		return response.isExists();
	}
	
}
