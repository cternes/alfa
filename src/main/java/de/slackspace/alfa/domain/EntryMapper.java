package de.slackspace.alfa.domain;

import java.util.Map;

import com.microsoft.windowsazure.services.table.models.Entity;

public interface EntryMapper {

	public ElasticSearchEntry mapToEntry(Entity entity, Map<String,String> deploymentMap);
}
