package de.slackspace.alfa.domain;

public class DeploymentEntry {

	private String name;
	private String deploymentId;
	
	public DeploymentEntry(String name, String deploymentId) {
		this.name = name;
		this.deploymentId = deploymentId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDeploymentId() {
		return deploymentId;
	}
	
	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}
	
}
