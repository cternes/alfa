package de.slackspace.alfa.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import org.junit.Test;

import com.microsoft.windowsazure.services.table.models.Entity;

public class LogEntryMapperTest {

	LogEntryMapper cut = new LogEntryMapper();
	
	@Test
	public void testMessageProcessingForAzureSdk2_5() {
		Entity entity = createEntity();
		entity.setProperty("Message", "string", "EventName=\"FormattedMessageEvent\" FormattedMessage=\"An unexpected error occured: {0}\" Argument0=\"SqlException: Test\" TraceSource=\"WaWorkerHost.exe\"");
		
		Map<String, String> deploymentMap = new HashMap<>();
		LogEntry entry = (LogEntry) cut.mapToEntry(entity, deploymentMap);
		
		Assert.assertEquals("An unexpected error occured: SqlException: Test TraceSource=WaWorkerHost.exe", entry.getMessage());
	}
	
	@Test
	public void testMessageProcessingWithMultipleArgumentsForAzureSdk2_5() {
		Entity entity = createEntity();
		entity.setProperty("Message", "string", "EventName=\"FormattedMessageEvent\" FormattedMessage=\"ReliableDatabaseManager - Retry #{0}: Exception which caused the retry: {1}\" Argument0=\"3\" Argument1=\"Database\"");
		
		Map<String, String> deploymentMap = new HashMap<>();
		LogEntry entry = (LogEntry) cut.mapToEntry(entity, deploymentMap);
		
		Assert.assertEquals("ReliableDatabaseManager - Retry #3: Exception which caused the retry: Database", entry.getMessage());
	}
	
	private Entity createEntity() {
		Entity entity = new Entity();
		entity.setProperty("Level", "int", "1");
		entity.setProperty("DeploymentId", "int", "123");
		entity.setProperty("EventId", "int", "1");
		entity.setProperty("Role", "string", "Web");
		entity.setProperty("RoleInstance", "string", "Web_0");
		entity.setTimestamp(new Date());
		
		return entity;
	}
}
