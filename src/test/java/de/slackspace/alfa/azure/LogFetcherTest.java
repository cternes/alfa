package de.slackspace.alfa.azure;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.elasticsearch.client.Client;
import org.junit.Test;
import org.mockito.Mockito;

import de.slackspace.alfa.domain.TableResultPartial;
import de.slackspace.alfa.elasticsearch.LogForwarder;
import de.slackspace.alfa.properties.PropertyHandler;

public class LogFetcherTest extends TestbaseAzure {

	@Test
	public void shouldRunAndNotThrow() {
		PropertyHandler propertyHandler = mock(PropertyHandler.class);
		
		LogForwarder logForwarder = mock(LogForwarder.class);
		AzureService azureService = mockAzureService();
		
		new LogFetcher(propertyHandler, logForwarder, azureService, 1, 1, false).run();
		
		Mockito.verify(azureService).getDeploymentEntries();
		Mockito.verify(azureService).getEntries(null, null, AzureService.WADLOGSTABLE);
	}
	
	@Test
	public void shouldFetchMultiplePartitionsUntilEnd() {
		int pollingInterval = 5;
		int instance = 1;
		String nextPartitionKeyFirstIteration = "1000";
		String nextPartitionKeySecondIteration = "2000";
		String nextRowKeyFirstIteration = "5000";
		String nextRowKeySecondIteration = "6000";
		
		PropertyHandler propertyHandler = mock(PropertyHandler.class);
		when(propertyHandler.getProperty(PropertyHandler.LAST_PARTITION_KEY, instance))
			.thenReturn(null)
			.thenReturn(nextPartitionKeyFirstIteration)
			.thenReturn(nextPartitionKeySecondIteration);
		when(propertyHandler.getProperty(PropertyHandler.LAST_ROW_KEY, instance))
			.thenReturn(null)
			.thenReturn(nextRowKeyFirstIteration)
			.thenReturn(nextRowKeySecondIteration);
		
		LogForwarder logForwarder = mock(LogForwarder.class);
		
		AzureService azureService = mock(AzureService.class);
		
		TableResultPartial deploymentEntries = createDeploymentTable();
		when(azureService.getDeploymentEntries()).thenReturn(deploymentEntries);

		// create entries for first query
		TableResultPartial logEntriesFirstQuery = new TableResultPartial();
		logEntriesFirstQuery.setNextPartitionKey(nextPartitionKeyFirstIteration);
		logEntriesFirstQuery.setNextRowKey(nextRowKeyFirstIteration);
		logEntriesFirstQuery.setEntryList(getLogEntities());
		when(azureService.getEntries(null, null, AzureService.WADLOGSTABLE)).thenReturn(logEntriesFirstQuery);
		
		// create entries second query
		TableResultPartial logEntriesSecondQuery = new TableResultPartial();
		logEntriesSecondQuery.setNextPartitionKey(nextPartitionKeySecondIteration);
		logEntriesSecondQuery.setNextRowKey(nextRowKeySecondIteration);
		logEntriesSecondQuery.setEntryList(getLogEntities());
		when(azureService.getEntries(nextPartitionKeyFirstIteration, nextRowKeyFirstIteration, AzureService.WADLOGSTABLE)).thenReturn(logEntriesSecondQuery);
		
		// create entries last query
		TableResultPartial logEntriesLastQuery = new TableResultPartial();
		logEntriesSecondQuery.setEntryList(getLogEntities());
		when(azureService.getEntries(nextPartitionKeySecondIteration, nextRowKeySecondIteration, AzureService.WADLOGSTABLE)).thenReturn(logEntriesLastQuery);
		
		// act
		new LogFetcher(propertyHandler, logForwarder, azureService, instance, pollingInterval, false).run();
		
		// assert
		Mockito.verify(azureService).getDeploymentEntries();
		Mockito.verify(azureService).getEntries(null, null, AzureService.WADLOGSTABLE);
		Mockito.verify(azureService).getEntries(nextPartitionKeyFirstIteration, nextRowKeyFirstIteration, AzureService.WADLOGSTABLE);
		Mockito.verify(azureService).getEntries(nextPartitionKeySecondIteration, nextRowKeySecondIteration, AzureService.WADLOGSTABLE);
		
		Mockito.verify(propertyHandler).setProperty(PropertyHandler.LAST_PARTITION_KEY, nextPartitionKeyFirstIteration, instance);
		Mockito.verify(propertyHandler).setProperty(PropertyHandler.LAST_PARTITION_KEY, nextPartitionKeySecondIteration, instance);
		Mockito.verify(propertyHandler).setProperty(PropertyHandler.LAST_ROW_KEY, nextRowKeyFirstIteration, instance);
		Mockito.verify(propertyHandler).setProperty(PropertyHandler.LAST_ROW_KEY, nextRowKeySecondIteration, instance);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenIsEmptyPropertyHandlerShouldThrow() {
		AzureService azureService = mockAzureService();
		Client client = mock(Client.class);
		new LogFetcher(null, new LogForwarder(client), azureService, 0, 0, false);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenIsEmptyLogForwarderShouldThrow() {
		AzureService azureService = mockAzureService();
		new LogFetcher(new PropertyHandler(""), null, azureService, 0, 0, false);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenIsEmptyAzureServiceShouldThrow() {
		Client client = mock(Client.class);
		new LogFetcher(new PropertyHandler(""), new LogForwarder(client), null, 0, 0, false);
	}
	
	private TableResultPartial createLogEntryTable() {
		TableResultPartial logEntries = new TableResultPartial();
		logEntries.setEntryList(getLogEntities());
		return logEntries;
	}

	private TableResultPartial createDeploymentTable() {
		TableResultPartial deploymentEntries = new TableResultPartial();
		deploymentEntries.setEntryList(getDeploymentEntities());
		return deploymentEntries;
	}
	
	private AzureService mockAzureService() {
		AzureService azureService = mock(AzureService.class);
		
		TableResultPartial deploymentEntries = createDeploymentTable();
		TableResultPartial logEntries = createLogEntryTable();
		when(azureService.getDeploymentEntries()).thenReturn(deploymentEntries);
		when(azureService.getEntries(null, null, AzureService.WADLOGSTABLE)).thenReturn(logEntries);
		return azureService;
	}
}
