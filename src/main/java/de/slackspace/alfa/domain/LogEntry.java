package de.slackspace.alfa.domain;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

public class LogEntry extends AbstractEntry implements ElasticSearchEntry {

	private String level;
	private String message;
	private String formattedMessage;
	private String eventId;
	private String severity;
	
	private transient String elasticIndex;
	
	public String getLevel() {
		return level;
	}
	
	public void setLevel(String level) {
		this.level = level;
		setSeverity(mapSeverity(level));
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		if(message != null) {
			message = message.replaceAll("\r\n", "");
		}
		
		this.message = message;
	}
	
	public String getEventId() {
		return eventId;
	}
	
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	
	public String mapSeverity(String lvl) {
		switch (lvl) {
			case "0": return "Undefined";
			case "1": return "Critical";
			case "2": return "Error";
			case "3": return "Warning";
			case "4": return "Information";
			case "5": return "Verbose";
			default: return "Undefined";
		}
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}
	
	public String getElasticIndex() {
		return "logs-" + elasticIndex;
	}

	public void setElasticIndex(String elasticIndex) {
		this.elasticIndex = elasticIndex;
	}

	@Override
	public String toString() {
		return "LogEntry [partitionKey=" + getPartitionKey() + ", message="
				+ message + "]";
	}

	@Override
	public String getType() {
		return severity;
	}
	
	public Map<String, XContentBuilder> getIndexMappings() throws IOException {
		Map<String, XContentBuilder> map = new HashMap<>();
		map.put("Information", createMappingJson("Information"));
		map.put("Error", createMappingJson("Error"));
		map.put("Warning", createMappingJson("Warning"));
		map.put("Verbose", createMappingJson("Verbose"));
		
		return map;
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
                .startObject("formattedMessage").field("type", "string").endObject()
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

	public String getFormattedMessage() {
		return formattedMessage;
	}

	public void setFormattedMessage(String formattedMessage) {
		this.formattedMessage = formattedMessage;
	}
	
}
