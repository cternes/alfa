package de.slackspace.alfa;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.slackspace.alfa.elasticsearch.LogCleaner;
import de.slackspace.alfa.properties.PropertyHandler;

public class ObjectFactoryTest {

	@Test
	public void testConstructLogFetcher() {
		String filename = "target/test-classes/testConstructLogFetcher.properties";
		PropertyHandler propertyHandler = new PropertyHandler(filename);
		
		propertyHandler.setProperty("accountName.1", "test");
		propertyHandler.setProperty("maxLogDays.1", "10");
		propertyHandler.setProperty("accountName.2", "test2");
		propertyHandler.setProperty("maxLogDays.2", "20");
		propertyHandler.writeProperties();
		
		LogCleaner logCleaner = ObjectFactory.constructLogCleaner(propertyHandler, null);
		assertEquals(20, logCleaner.getMaxKeepDays());
	}
	
	@Test
	public void testConstructLogFetcherWithDefault() {
		String filename = "target/test-classes/testConstructLogFetcherWithDefault.properties";
		PropertyHandler propertyHandler = new PropertyHandler(filename);
		
		propertyHandler.setProperty("accountName.1", "test");
		propertyHandler.setProperty("maxLogDays.1", "1");
		propertyHandler.setProperty("accountName.2", "test2");
		propertyHandler.setProperty("maxLogDays.2", "2");
		propertyHandler.writeProperties();
		
		LogCleaner logCleaner = ObjectFactory.constructLogCleaner(propertyHandler, null);
		assertEquals(10, logCleaner.getMaxKeepDays());
	}
}
