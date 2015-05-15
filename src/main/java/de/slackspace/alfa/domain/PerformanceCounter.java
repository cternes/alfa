package de.slackspace.alfa.domain;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

public class PerformanceCounter extends AbstractEntry implements ElasticSearchEntry {

	private String counterName;
	private double counterValue;
	
	private transient String elasticIndex;
	
	@Override
	public String getElasticIndex() {
		return "perfcounters-" + elasticIndex;
	}

	@Override
	public String getType() {
		return "Counter";
	}

	public void setElasticIndex(String elasticIndex) {
		this.elasticIndex = elasticIndex;
	}

	public String getCounterName() {
		return counterName;
	}

	public void setCounterName(String counterName) {
		this.counterName = counterName;
	}

	public double getCounterValue() {
		return counterValue;
	}

	public void setCounterValue(double counterValue) {
		this.counterValue = counterValue;
	}

	@Override
	public Map<String, XContentBuilder> getIndexMappings() throws IOException {
		Map<String, XContentBuilder> map = new HashMap<>();
		map.put("Counter", createMappingJson("Counter"));
		
		return map;
	}
	
	private XContentBuilder createMappingJson(String typeName) throws IOException {
		XContentBuilder mappingBuilder = XContentFactory.jsonBuilder().startObject().startObject(typeName)
                .startObject("properties")
                .startObject("dateTime").field("type", "date").endObject()
                .startObject("deploymentId").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("environment").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("counterName").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("counterValue").field("type", "double").field("index", "not_analyzed").endObject()
                .startObject("partitionKey").field("type", "long").endObject()
                .startObject("role").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("roleInstance").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("rowKey").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("timestamp").field("type", "long").endObject()
                .endObject()
                .endObject()
                .endObject();
		
		return mappingBuilder;
	}

}
