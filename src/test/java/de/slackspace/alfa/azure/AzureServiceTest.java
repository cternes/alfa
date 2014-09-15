package de.slackspace.alfa.azure;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.table.TableContract;
import com.microsoft.windowsazure.services.table.models.QueryEntitiesOptions;
import com.microsoft.windowsazure.services.table.models.QueryEntitiesResult;

import de.slackspace.alfa.domain.TableResultPartial;

public class AzureServiceTest extends TestbaseAzure {

	@Test
	public void testGetDeploymentEntities() throws ServiceException {
		TableContract contract = mock(TableContract.class);
		QueryEntitiesResult result = new QueryEntitiesResult();
		result.setEntities(getDeploymentEntities());
		when(contract.queryEntities("deployments")).thenReturn(result);
		
		AzureService cut = new AzureService(contract, 10);
		TableResultPartial deploymentEntries = cut.getDeploymentEntries();
		
		assertEquals(getDeploymentEntities().size(), deploymentEntries.getEntryList().size());
		verify(contract).queryEntities("deployments");
	}
	
	@Test
	public void testGetDeploymentEntitiesWithException() throws ServiceException {
		TableContract contract = mock(TableContract.class);
		QueryEntitiesResult result = new QueryEntitiesResult();
		result.setEntities(getDeploymentEntities());
		when(contract.queryEntities("deployments")).thenThrow(new ServiceException());
		
		AzureService cut = new AzureService(contract, 10);
		TableResultPartial deploymentEntries = cut.getDeploymentEntries();
		
		assertEquals(0, deploymentEntries.getEntryList().size());
		verify(contract).queryEntities("deployments");
	}
	
	@Test
	public void testGetLogEntries() throws ServiceException {
		TableContract contract = mock(TableContract.class);
		QueryEntitiesResult result = new QueryEntitiesResult();
		result.setEntities(getLogEntities());
		when(contract.queryEntities(eq("WADLogsTable"), Mockito.any(QueryEntitiesOptions.class))).thenReturn(result);
		
		AzureService cut = new AzureService(contract, 10);
		TableResultPartial deploymentEntries = cut.getLogEntries(null, null);
		
		assertEquals(getDeploymentEntities().size(), deploymentEntries.getEntryList().size());
		verify(contract).queryEntities(eq("WADLogsTable"), Mockito.any(QueryEntitiesOptions.class));
	}
	
	@Test
	public void testGetLogEntriesWithPartitionKey() throws ServiceException {
		TableContract contract = mock(TableContract.class);
		QueryEntitiesResult result = new QueryEntitiesResult();
		result.setEntities(getLogEntities());
		when(contract.queryEntities(eq("WADLogsTable"), Mockito.any(QueryEntitiesOptions.class))).thenReturn(result);
		
		AzureService cut = new AzureService(contract, 10);
		TableResultPartial deploymentEntries = cut.getLogEntries("PartitionKey", "RowKey");
		
		assertEquals(getDeploymentEntities().size(), deploymentEntries.getEntryList().size());
		verify(contract).queryEntities(eq("WADLogsTable"), Mockito.any(QueryEntitiesOptions.class));
	}
	
	@Test
	public void testGetLogEntriesWithException() throws ServiceException {
		TableContract contract = mock(TableContract.class);
		QueryEntitiesResult result = new QueryEntitiesResult();
		result.setEntities(getLogEntities());
		when(contract.queryEntities(eq("WADLogsTable"), Mockito.any(QueryEntitiesOptions.class))).thenThrow(new ServiceException());
		
		AzureService cut = new AzureService(contract, 10);
		TableResultPartial deploymentEntries = cut.getLogEntries(null, null);
		
		assertEquals(0, deploymentEntries.getEntryList().size());
		verify(contract).queryEntities(eq("WADLogsTable"), Mockito.any(QueryEntitiesOptions.class));
	}
}
