package de.slackspace.alfa.azure;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.microsoft.windowsazure.services.table.models.Entity;
import com.microsoft.windowsazure.services.table.models.Property;

public abstract class TestbaseAzure {

	public List<Entity> getDeploymentEntities() {
		List<Entity> list = new ArrayList<Entity>();
		
		Entity deploymentEntry = new Entity();
		deploymentEntry.setProperty("DeploymentId", new Property().setValue("123"));
		deploymentEntry.setProperty("Name", new Property().setValue("environment1"));
		
		list.add(deploymentEntry);
		return list;
	}
	
	public List<Entity> getLogEntities() {
		List<Entity> list = new ArrayList<Entity>();
		
		Entity logEntry = new Entity();
		logEntry.setProperty("Level", new Property().setValue("Error"));
		logEntry.setProperty("Message", new Property().setValue("My test error message"));
		logEntry.setProperty("DeploymentId", new Property().setValue("123"));
		logEntry.setProperty("EventId", new Property().setValue("0"));
		logEntry.setProperty("Role", new Property().setValue("MyCompany.MyApp.MyRole"));
		logEntry.setProperty("RoleInstance", new Property().setValue("MyCompany.MyApp.MyRole_IN_0"));
		logEntry.setTimestamp(new Date());
		logEntry.setRowKey(UUID.randomUUID().toString());
		logEntry.setPartitionKey(UUID.randomUUID().toString());
		
		list.add(logEntry);
		return list;
	}
}
