package de.slackspace.alfa.domain;

public class LogEntry {

	private String partitionKey;
	private String rowKey;
	private String level;
	private String message;
	private String deploymentId;
	private String eventId;
	private String role;
	private String roleInstance;
	private String dateTime;
	private Long timestamp;
	private String severity;
	private String environment;
	
	private transient String elasticIndex;
	
	public String getLevel() {
		return level;
	}
	
	public void setLevel(String level) {
		this.level = level;
		setSeverity(mapSeverity(level));
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		if(message != null) {
			message = message.replaceAll("\r\n", "");
		}
		
		this.message = message;
	}
	
	public String getDeploymentId() {
		return deploymentId;
	}
	
	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}
	
	public String getEventId() {
		return eventId;
	}
	
	public void setEventId(String eventId) {
		this.eventId = eventId;
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
	
	public String mapSeverity(String lvl) {
		switch (lvl) {
			case "0": return "Undefined";
			case "1": return "Critical";
			case "2": return "Error";
			case "3": return "Warning";
			case "4": return "Information";
			case "5": return "Verbose";
			default: return "Undefined";
		}
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
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

	public String getElasticIndex() {
		return elasticIndex;
	}

	public void setElasticIndex(String elasticIndex) {
		this.elasticIndex = elasticIndex;
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
