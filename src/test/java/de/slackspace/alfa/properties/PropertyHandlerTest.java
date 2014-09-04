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

public class PropertyHandlerTest {

	private PropertyHandler cut;
	
	@Test
	public void testWriteProperties() throws FileNotFoundException, IOException {
		String filename = "target/test-classes/testWrite.properties";
		cut = new PropertyHandler(filename);
		
		cut.setProperty("key1", "value1");
		cut.setProperty("key2", "value2");
		cut.writeProperties();
		
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
		
		assertEquals("value1", cut.getProperty("key1"));
		assertEquals("value2", cut.getProperty("key2"));
	}
	
	@Test
	public void testReloadProperties() throws FileNotFoundException, IOException {
		String filename = "target/test-classes/testReload.properties";
		cut = new PropertyHandler(filename);
		
		cut.setProperty("key1", "value1");
		cut.setProperty("key2", "value2");
		cut.writeProperties();
		
		assertEquals("value1", cut.getProperty("key1"));
		assertEquals("value2", cut.getProperty("key2"));
		
		cut.setProperty("key1", "newValue");
		cut.writeProperties();
		
		assertEquals("newValue", cut.getProperty("key1"));
	}
	
	@Test
	public void testGetProperty() {
		String filename = "target/test-classes/testGetProperty.properties";
		cut = new PropertyHandler(filename);
		
		cut.setProperty("key_1", "A");
		cut.setProperty("key_2", "B");
		cut.setProperty("key_3", "C");
		cut.writeProperties();
		
		assertEquals("A", cut.getProperty("key", 1));
		assertEquals("B", cut.getProperty("key", 2));
		assertEquals("C", cut.getProperty("key", 3));
	}
	
	@Test
	public void testSetProperty() {
		String filename = "target/test-classes/testSetProperty.properties";
		cut = new PropertyHandler(filename);
		
		cut.setProperty("key", "A", 1);
		cut.setProperty("key", "B", 2);
		cut.setProperty("key", "C", 3);
		
		cut.writeProperties();
		
		assertEquals("A", cut.getProperty("key", 1));
		assertEquals("B", cut.getProperty("key", 2));
		assertEquals("C", cut.getProperty("key", 3));
	}
	
	@Test
	public void testGetNumberOfAccountPropertiesExpectedThree() {
		String filename = "target/test-classes/testGetNumberOfAccountsThree.properties";
		cut = new PropertyHandler(filename);
		
		cut.setProperty("accountUrl_1", "value1");
		cut.setProperty("accountUrl_2", "value2");
		cut.setProperty("accountUrl_3", "value3");
		cut.writeProperties();
		
		assertEquals(3, cut.getNumberOfAccounts());
	}
	
	@Test
	public void testGetNumberOfAccountPropertiesExpectedOne() {
		String filename = "target/test-classes/testGetNumberOfAccountsOne.properties";
		cut = new PropertyHandler(filename);
		
		cut.setProperty("accountUrl_1", "value1");
		cut.writeProperties();
		
		assertEquals(1, cut.getNumberOfAccounts());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testConstructorHandleNull() {
		new PropertyHandler(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testConstructorHandleEmpty() {
		new PropertyHandler("");
	}
	
//	@Test(expected=ConfigurationException.class)
//	public void testWritePropertiesWithException() throws IOException {
//		PropertyHandler cut = new PropertyHandler("test");
//		
//		Properties p = mock(Properties.class);
//		Mockito.doThrow(new IOException()).when(p).store(Mockito.any(Writer.class), Mockito.anyString());
//		
//		cut.writeProperties(p);
//	}
	
	private Properties createProperties() {
		Properties p = new Properties();
		p.setProperty("key1", "value1");
		p.setProperty("key2", "value2");
		return p;
	}
}
