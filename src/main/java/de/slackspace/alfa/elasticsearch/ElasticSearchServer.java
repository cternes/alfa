package de.slackspace.alfa.elasticsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchServer.class);
	
	protected final Properties configuration;
    private Node server;
    
    public ElasticSearchServer(Properties configuration) {
    	this.configuration = configuration;
    }
    
    public void start() {
        if (LOGGER.isInfoEnabled()) {
        	LOGGER.info("Starting the Elastic Search server node");
        }
 
        final ImmutableSettings.Builder builder = ImmutableSettings.settingsBuilder().put(configuration);
        server = NodeBuilder.nodeBuilder().settings(builder).build();
 
        if (LOGGER.isInfoEnabled()) {
        	LOGGER.info("Starting the Elastic Search server node with these settings:");
            final Map<String, String> map = server.settings().getAsMap();
            final List<String> keys = new ArrayList<String>(map.keySet());
            Collections.sort(keys);
            for (String key : keys) {
            	LOGGER.info("    " + key + " : " + map.get(key));
            }
        }
 
        server.start();
 
        checkServerStatus();
 
        if (LOGGER.isInfoEnabled()) {
        	LOGGER.info("Elastic Search server is started.");
        }
    }
    
    protected ClusterHealthStatus getHealthStatus() {
        return getClient().admin().cluster().prepareHealth().execute().actionGet().getStatus();
    }
    
    protected void checkServerStatus() {
        ClusterHealthStatus status = getHealthStatus();
 
        // Check the current status of ES.
        if (ClusterHealthStatus.RED.equals(status)) {
            LOGGER.info("ES status is " + status + ". Waiting for ES recovery.");
 
            // Waits at most 90 seconds to make sure the cluster health is at least yellow.
            getClient().admin().cluster().prepareHealth()
                    .setWaitForYellowStatus()
                    .setTimeout("90s")
                    .execute().actionGet();
        }
 
        // Check the cluster health for a final time.
        status = getHealthStatus();
        LOGGER.info("ES cluster status is " + status);
 
        // If we are still in red status, then we cannot proceed.
        if (ClusterHealthStatus.RED.equals(status)) {
            throw new RuntimeException("ES health status is RED. Server is not able to start.");
        }
 
    }
    
    public Client getClient() {
        return server.client();
    }
    
    public void stop() {
        server.close();
    }
    
}
