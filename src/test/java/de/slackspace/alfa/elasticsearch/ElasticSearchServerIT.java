package de.slackspace.alfa.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.junit.Test;

public class ElasticSearchServerIT {

	@Test
	public void testServerStart() {
		Properties configuration = new Properties();
		ElasticSearchServer server = new ElasticSearchServer(configuration);
		server.start();
		
		assertEquals(ClusterHealthStatus.GREEN, server.getHealthStatus());
	}
	
	@Test
	public void testGetClient() {
		Properties configuration = new Properties();
		ElasticSearchServer server = new ElasticSearchServer(configuration);
		server.start();
		assertNotNull(server.getClient());
	}
}
