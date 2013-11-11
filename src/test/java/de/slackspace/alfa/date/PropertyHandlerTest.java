package de.slackspace.alfa.date;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import de.slackspace.alfa.properties.PropertyHandler;
import static org.junit.Assert.*;

public class PropertyHandlerTest {

	private PropertyHandler instance = PropertyHandler.getInstance();
	
	@Test
	public void testSingleton() {
		PropertyHandler instance1 = PropertyHandler.getInstance();
		PropertyHandler instance2 = PropertyHandler.getInstance();
		
		assertEquals(instance1, instance2);
	}
	
	@Test
	public void testWriteProperties() throws FileNotFoundException, IOException {
		Properties properties = createProperties();
		
		String filename = "target/test-classes/testWrite.properties";
		PropertyHandler.getInstance().setPropertiesFile(filename);
		PropertyHandler.getInstance().writeProperties(properties);
		
		File file = new File(filename);
		assertTrue(file.exists());
		
		Properties loadedProperties = new Properties();
		loadedProperties.load(new FileReader(filename));
		
		assertEquals("value1", loadedProperties.get("key1"));
		assertEquals("value2", loadedProperties.get("key2"));
	}

	@Test
	public void testReadProperties() throws FileNotFoundException, IOException {
		String filename = "target/test-classes/testRead.properties";
		Properties propertiesToWrite = createProperties();
		propertiesToWrite.store(new FileOutputStream(filename), filename);
		
		instance.setPropertiesFile(filename);
		Properties properties = instance.readProperties();
		
		assertEquals("value1", properties.getProperty("key1"));
		assertEquals("value2", properties.getProperty("key2"));
	}
	
	@Test
	public void testReloadProperties() {
		String filename = "target/test-classes/testReload.properties";
		Properties properties = createProperties();
		PropertyHandler.getInstance().setPropertiesFile(filename);
		PropertyHandler.getInstance().writeProperties(properties);
		
		Properties readProperties = PropertyHandler.getInstance().readProperties();
		assertEquals("value1", readProperties.getProperty("key1"));
		assertEquals("value2", readProperties.getProperty("key2"));
		
		properties.setProperty("key1", "newValue");
		PropertyHandler.getInstance().writeProperties(properties);
		
		assertEquals("newValue", PropertyHandler.getInstance().readProperties().getProperty("key1"));
	}
	
	private Properties createProperties() {
		Properties p = new Properties();
		p.setProperty("key1", "value1");
		p.setProperty("key2", "value2");
		return p;
	}
}
