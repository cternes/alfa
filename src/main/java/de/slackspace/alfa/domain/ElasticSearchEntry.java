package de.slackspace.alfa.domain;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.common.xcontent.XContentBuilder;

public interface ElasticSearchEntry {

	public String getElasticIndex();
	
	public String getType();
	
	public Map<String, XContentBuilder> getIndexMappings() throws IOException;
}
