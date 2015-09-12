package de.slackspace.alfa.elasticsearch;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import de.slackspace.alfa.date.DateFormatter;

public class LogCleanerTest {

	@Test
	public void shouldNotDeleteIndicesToKeep() {
		Client client = mock(Client.class);
		IndicesAdminClient indicesAdminClient = mock(IndicesAdminClient.class);
		
		// setup indices
		Calendar cal = Calendar.getInstance();
		String logEntry = "logs-" + DateFormatter.toYYYYMMDD(cal.getTime());
		String perfEntry = "perfcounters-" + DateFormatter.toYYYYMMDD(cal.getTime());
		
		Map<String, IndexMetaData> hashMap = new HashMap<>();
		hashMap.put("kibana-int", mock(IndexMetaData.class));
		hashMap.put(".kibana", mock(IndexMetaData.class));
		hashMap.put(logEntry, mock(IndexMetaData.class));
		hashMap.put(perfEntry, mock(IndexMetaData.class));
		
		ImmutableOpenMap<String, IndexMetaData> map = new ImmutableOpenMap.Builder<String, IndexMetaData>().putAll(hashMap).build();
		SetupMocks(client, indicesAdminClient, map);
		
		LogCleaner cut = new LogCleaner(client, 10);
		
		// act
		cut.deleteOldLogs();
		
		// assert
		verify(indicesAdminClient, never()).delete(new DeleteIndexRequest("kibana-int"));
		verify(indicesAdminClient, never()).delete(new DeleteIndexRequest(".kibana"));
		verify(indicesAdminClient, never()).delete(new DeleteIndexRequest(logEntry));
		verify(indicesAdminClient, never()).delete(new DeleteIndexRequest(perfEntry));
	}
	
	@Test
	public void shouldDeleteOldIndices() {
		Client client = mock(Client.class);
		IndicesAdminClient indicesAdminClient = mock(IndicesAdminClient.class);
		
		// setup indices, creating one old logEntry & one old performanceEntry
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		String logEntry = "logs-" + DateFormatter.toYYYYMMDD(cal.getTime());
		String perfEntry = "perfcounters-" + DateFormatter.toYYYYMMDD(cal.getTime());
		
		Map<String, IndexMetaData> hashMap = new HashMap<>();
		hashMap.put("kibana-int", mock(IndexMetaData.class));
		hashMap.put(".kibana", mock(IndexMetaData.class));
		hashMap.put(logEntry, mock(IndexMetaData.class));
		hashMap.put(perfEntry, mock(IndexMetaData.class));
		
		ImmutableOpenMap<String, IndexMetaData> map = new ImmutableOpenMap.Builder<String, IndexMetaData>().putAll(hashMap).build();
		SetupMocks(client, indicesAdminClient, map);
		
		LogCleaner cut = new LogCleaner(client, 10);
		
		@SuppressWarnings("unchecked")
		ActionFuture<DeleteIndexResponse> future = ((ActionFuture<DeleteIndexResponse>) mock(ActionFuture.class));
		when(future.actionGet()).thenReturn(mock(DeleteIndexResponse.class));
		
		ArgumentCaptor<DeleteIndexRequest> argumentCaptor = ArgumentCaptor.forClass(DeleteIndexRequest.class);
		when(indicesAdminClient.delete(argumentCaptor.capture())).thenReturn(future);
		
		// act
		cut.deleteOldLogs();
		
		// assert
		verify(indicesAdminClient, never()).delete(new DeleteIndexRequest("kibana-int"));
		verify(indicesAdminClient, never()).delete(new DeleteIndexRequest(".kibana"));
		
		// verify deleted indices
		List<DeleteIndexRequest> callList = argumentCaptor.getAllValues();
		DeleteIndexRequest firstDeleteRequest = callList.get(0);
		assertThat(firstDeleteRequest.indices()[0], equalTo("perfcounters-2014.09.12"));

		DeleteIndexRequest secondDeleteRequest = callList.get(1);
		assertThat(secondDeleteRequest.indices()[0], equalTo("logs-2014.09.12"));
	}
	
	private void SetupMocks(Client client, IndicesAdminClient indicesAdminClient, ImmutableOpenMap<String, IndexMetaData> indicesMap) {
		AdminClient adminClient = mock(AdminClient.class);
		ClusterAdminClient clusterAdminClient = mock(ClusterAdminClient.class);
		
		@SuppressWarnings("unchecked")
		ActionFuture<ClusterStateResponse> actionFuture = ((ActionFuture<ClusterStateResponse>) mock(ActionFuture.class));
		ClusterStateResponse response = mock(ClusterStateResponse.class);
		ClusterState state = mock(ClusterState.class);
		MetaData metaData = mock(MetaData.class);
		
		when(client.admin()).thenReturn(adminClient);
		when(adminClient.indices()).thenReturn(indicesAdminClient);
		when(adminClient.cluster()).thenReturn(clusterAdminClient);
		when(clusterAdminClient.state(Mockito.any())).thenReturn(actionFuture);
		when(actionFuture.actionGet()).thenReturn(response);
		when(response.getState()).thenReturn(state);
		when(state.getMetaData()).thenReturn(metaData);
		when(metaData.getIndices()).thenReturn(indicesMap);
	}
}
