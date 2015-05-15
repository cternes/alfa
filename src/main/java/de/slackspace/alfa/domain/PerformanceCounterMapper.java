package de.slackspace.alfa.domain;

import java.util.Map;

import com.microsoft.windowsazure.services.table.models.Entity;

import de.slackspace.alfa.date.DateFormatter;

public class PerformanceCounterMapper implements EntryMapper {

	@Override
	public ElasticSearchEntry mapToEntry(Entity entity,	Map<String, String> deploymentMap) {
		PerformanceCounter entry = new PerformanceCounter();
		entry.setDateTime(DateFormatter.toYYYYMMDDHHMMSS(entity.getTimestamp()));
		entry.setElasticIndex(DateFormatter.toYYYYMMDD(entity.getTimestamp()));
		entry.setPartitionKey(entity.getPartitionKey());
		entry.setRowKey(entity.getRowKey());
		entry.setDeploymentId(entity.getProperty("DeploymentId").getValue().toString());
		entry.setRole(entity.getProperty("Role").getValue().toString());
		entry.setRoleInstance(entity.getProperty("RoleInstance").getValue().toString());
		entry.setTimestamp(entity.getTimestamp().getTime());
		entry.setCounterName(entity.getProperty("CounterName").getValue().toString());
		
		double counterValue = Double.parseDouble(entity.getProperty("CounterValue").getValue().toString());
		entry.setCounterValue(counterValue);
		
		String name = deploymentMap.get(entry.getDeploymentId());
		entry.setEnvironment(name);
		
		return entry;
	}

}
