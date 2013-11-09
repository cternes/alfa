package de.slackspace.alfa.domain;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.windowsazure.services.table.models.Entity;

public class TableResultPartial {

	private String nextPartitionKey;
	private String nextRowKey;
	private List<Entity> entryList = new ArrayList<Entity>();
	
	public TableResultPartial() {
	}
	
	public TableResultPartial(List<Entity> entryList, String nextPartitionKey, String nextRowKey) {
		this.entryList = entryList;
		this.nextPartitionKey = nextPartitionKey;
		this.nextRowKey = nextRowKey;
	}
	
	public String getNextPartitionKey() {
		return nextPartitionKey;
	}
	public void setNextPartitionKey(String nextPartitionKey) {
		this.nextPartitionKey = nextPartitionKey;
	}
	public String getNextRowKey() {
		return nextRowKey;
	}
	public void setNextRowKey(String nextRowKey) {
		this.nextRowKey = nextRowKey;
	}
	public List<Entity> getEntryList() {
		return entryList;
	}
	public void setEntryList(List<Entity> entryList) {
		this.entryList = entryList;
	}
	
}
