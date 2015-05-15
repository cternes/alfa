package de.slackspace.alfa.domain;

import java.util.Map;

import com.microsoft.windowsazure.services.table.models.Entity;

import de.slackspace.alfa.date.DateFormatter;

public class LogEntryMapper implements EntryMapper {
	
	@Override
	public ElasticSearchEntry mapToEntry(Entity entity, Map<String, String> deploymentMap) {
		LogEntry entry = new LogEntry();
		entry.setLevel(entity.getProperty("Level").getValue().toString());
		entry.setPartitionKey(entity.getPartitionKey());
		entry.setRowKey(entity.getRowKey());
		entry.setDeploymentId(entity.getProperty("DeploymentId").getValue().toString());
		entry.setEventId(entity.getProperty("EventId").getValue().toString());
		entry.setRole(entity.getProperty("Role").getValue().toString());
		entry.setRoleInstance(entity.getProperty("RoleInstance").getValue().toString());
		entry.setTimestamp(entity.getTimestamp().getTime());
		entry.setDateTime(DateFormatter.toYYYYMMDDHHMMSS(entity.getTimestamp()));
		entry.setElasticIndex(DateFormatter.toYYYYMMDD(entity.getTimestamp()));
		
		String name = deploymentMap.get(entry.getDeploymentId());
		entry.setEnvironment(name);
		
		String message = entity.getProperty("Message").getValue().toString();
		entry.setMessage(message);

		// format message to avoid ugly messages (Azure SDK >2.5)
		if(message.startsWith("EventName=")) {
			entry.setFormattedMessage(replacePlaceholders(message));
		}
		
		return entry;
	}
	
	public String replacePlaceholders(String msg) {
		// remove everything until the second =
		int firstEqualIdx = msg.indexOf("=");
		int secondEqualIdx = msg.indexOf("=", firstEqualIdx + 1);
		msg = msg.substring(secondEqualIdx + 2);
		
		for (int i = 0; i < 20; i++) {
			if(msg.contains("{" + i + "}")) {
				
				// retrieve argument
				int argumentIdx = msg.indexOf("Argument" + i);
				int equalIdx = msg.indexOf("=", argumentIdx);
				
				int nextArgumentIdx =  msg.indexOf(" Argument", equalIdx);
				
				String argumentValue;
				if(nextArgumentIdx > 0) {
					// substring from =" until ", leave out quote
					argumentValue = msg.substring(equalIdx + 2, nextArgumentIdx - 1);
				}
				else {
					// leave out quote
					argumentValue = msg.substring(equalIdx + 2, msg.length() - 1);
				}
				
				// remove argument from msg
				msg = msg.replace("Argument" + i + "=", "");
				
				// remove argument value from msg
				msg = msg.replace("\"" + argumentValue + "\"", "");
				
				// replace placeholder with argument value
				msg = msg.replace("{" + i + "}", argumentValue);
			}
		}
		
		// remove quotes
		msg = msg.replace("\"", "");
		
		return msg.trim();
	}
}
