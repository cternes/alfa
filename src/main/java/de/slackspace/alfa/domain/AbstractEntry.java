package de.slackspace.alfa.domain;

public class AbstractEntry {

	protected String partitionKey;
	protected String rowKey;
	protected String deploymentId;
	protected String role;
	protected String roleInstance;
	protected String dateTime;
	protected Long timestamp;
	protected String environment;
	
	public String getDeploymentId() {
		return deploymentId;
	}
	
	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}
	
	public String getRole() {
		return role;
	}
	
	public void setRole(String role) {
		this.role = role;
	}
	
	public String getRoleInstance() {
		return roleInstance;
	}
	
	public void setRoleInstance(String roleInstance) {
		this.roleInstance = roleInstance;
	}
	
	public String getPartitionKey() {
		return partitionKey;
	}

	public void setPartitionKey(String partitionKey) {
		this.partitionKey = partitionKey;
	}

	public String getRowKey() {
		return rowKey;
	}

	public void setRowKey(String rowKey) {
		this.rowKey = rowKey;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

}
