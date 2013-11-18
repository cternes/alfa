package de.slackspace.alfa.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

import org.junit.Test;
import org.mockito.Mockito;

import de.slackspace.alfa.exception.ConfigurationException;

public class PropertyHandlerTest {

	private PropertyHandler cut;
	 
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
	
	@Test(expected=IllegalArgumentException.class)
	public void testConstructorHandleNull() {
		new PropertyHandler(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testConstructorHandleEmpty() {
		new PropertyHandler("");
	}
	
	@Test(expected=ConfigurationException.class)
	public void testWritePropertiesWithException() throws IOException {
		PropertyHandler cut = new PropertyHandler("test");
		
		Properties p = mock(Properties.class);
		Mockito.doThrow(new IOException()).when(p).store(Mockito.any(Writer.class), Mockito.anyString());
		
		cut.writeProperties(p);
	}
	
	private Properties createProperties() {
		Properties p = new Properties();
		p.setProperty("key1", "value1");
		p.setProperty("key2", "value2");
		return p;
	}
}
