package de.slackspace.alfa.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import de.slackspace.alfa.properties.PropertyHandler;

public class PropertyHandlerTest {

	private PropertyHandler cut = new PropertyHandler();
	
	@Test
	public void testWriteProperties() throws FileNotFoundException, IOException {
		String filename = "target/test-classes/testWrite.properties";
		cut = new PropertyHandler(filename);
		
		Properties properties = createProperties();
		cut.writeProperties(properties);
		
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
		cut = new PropertyHandler(filename);
		
		Properties propertiesToWrite = createProperties();
		propertiesToWrite.store(new FileOutputStream(filename), filename);
		
		Properties properties = cut.readProperties();
		
		assertEquals("value1", properties.getProperty("key1"));
		assertEquals("value2", properties.getProperty("key2"));
	}
	
	@Test
	public void testReloadProperties() {
		String filename = "target/test-classes/testReload.properties";
		cut = new PropertyHandler(filename);
		
		Properties properties = createProperties();
		cut.writeProperties(properties);
		
		Properties readProperties = cut.readProperties();
		assertEquals("value1", readProperties.getProperty("key1"));
		assertEquals("value2", readProperties.getProperty("key2"));
		
		properties.setProperty("key1", "newValue");
		cut.writeProperties(properties);
		
		assertEquals("newValue", cut.readProperties().getProperty("key1"));
	}
	
	private Properties createProperties() {
		Properties p = new Properties();
		p.setProperty("key1", "value1");
		p.setProperty("key2", "value2");
		return p;
	}
}
