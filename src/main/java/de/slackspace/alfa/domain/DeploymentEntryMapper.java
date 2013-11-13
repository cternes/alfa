package de.slackspace.alfa.domain;

import com.microsoft.windowsazure.services.table.models.Entity;

public class DeploymentEntryMapper {

	private DeploymentEntryMapper() {}
	
	public static DeploymentEntry mapToLogEntry(Entity entity) {
		String deploymentId = entity.getProperty("DeploymentId").getValue().toString();
		String name = entity.getProperty("Name").getValue().toString();
		return new DeploymentEntry(name, deploymentId);
	}
}
