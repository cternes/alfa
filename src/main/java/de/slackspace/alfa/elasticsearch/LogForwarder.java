package de.slackspace.alfa.elasticsearch;

import java.io.IOException;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import de.slackspace.alfa.domain.LogEntry;
import de.slackspace.alfa.exception.ConnectionException;

public class LogForwarder {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogForwarder.class);
	
	private Client client;
	private Gson gson = new Gson();
	
	public LogForwarder(Client client) {
		this.client = client;
	}
	
	public void pushEvent(LogEntry entry) throws ConnectionException, IOException {
		String json = gson.toJson(entry);
		String index = "logs-" + entry.getElasticIndex();
		
		if(!isIndexExisting(index)) {
			createIndexMapping(index);
		}
		
		client.prepareIndex(index, entry.getSeverity())
                .setSource(json)
                .setOperationThreaded(false)
                .execute()
                .actionGet();
	}
	
	private void createIndexMapping(String index) throws IOException {
        CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate(index);
        createIndexRequestBuilder.addMapping("Information", createMappingJson("Information"));
        createIndexRequestBuilder.addMapping("Error", createMappingJson("Error"));
        createIndexRequestBuilder.addMapping("Warning", createMappingJson("Warning"));
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
	
	private XContentBuilder createMappingJson(String typeName) throws IOException {
		XContentBuilder mappingBuilder = XContentFactory.jsonBuilder().startObject().startObject(typeName)
                .startObject("properties")
                .startObject("dateTime").field("type", "date").endObject()
                .startObject("deploymentId").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("environment").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("eventId").field("type", "string").endObject()
                .startObject("level").field("type", "long").endObject()
                .startObject("message").field("type", "string").endObject()
                .startObject("partitionKey").field("type", "long").endObject()
                .startObject("role").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("roleInstance").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("rowKey").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("severity").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("timestamp").field("type", "long").endObject()
                .endObject()
                .endObject()
                .endObject();
		
		return mappingBuilder;
	}
}
