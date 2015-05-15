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
	public void testSimple() {
		PropertyHandler propertyHandler = mock(PropertyHandler.class);
		
		LogForwarder logForwarder = mock(LogForwarder.class);
		AzureService azureService = mockAzureService();
		
		new LogFetcher(propertyHandler, logForwarder, azureService, 1, 1, false).run();
		
		Mockito.verify(azureService).getDeploymentEntries();
		Mockito.verify(azureService).getEntries(null, null, AzureService.WADLOGSTABLE);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testEmptyPropertyHandler() {
		AzureService azureService = mockAzureService();
		Client client = mock(Client.class);
		new LogFetcher(null, new LogForwarder(client), azureService, 0, 0, false);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testEmptyLogForwarder() {
		AzureService azureService = mockAzureService();
		new LogFetcher(new PropertyHandler(""), null, azureService, 0, 0, false);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testEmptyAzureService() {
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
