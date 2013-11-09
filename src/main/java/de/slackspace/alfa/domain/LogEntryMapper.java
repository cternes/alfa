package de.slackspace.alfa.domain;

import com.microsoft.windowsazure.services.table.models.Entity;

import de.slackspace.alfa.date.DateFormatter;

public class LogEntryMapper {

	public static LogEntry mapToLogEntry(Entity entity) {
		LogEntry entry = new LogEntry();
		entry.setLevel(entity.getProperty("Level").getValue().toString());
		entry.setMessage(entity.getProperty("Message").getValue().toString());
		entry.setPartitionKey(entity.getPartitionKey());
		entry.setRowKey(entity.getRowKey());
		entry.setDeploymentId(entity.getProperty("DeploymentId").getValue().toString());
		entry.setEventId(entity.getProperty("EventId").getValue().toString());
		entry.setRole(entity.getProperty("Role").getValue().toString());
		entry.setRoleInstance(entity.getProperty("RoleInstance").getValue().toString());
		entry.setTimestamp(entity.getTimestamp().getTime());
		entry.setDateTime(DateFormatter.toYYYYMMDDHHMMSS(entity.getTimestamp()));
		entry.setElasticIndex(DateFormatter.toYYYYMMDD(entity.getTimestamp()));
		
		return entry;
	}
}
