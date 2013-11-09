package de.slackspace.alfa.properties;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class PropertyHandler {

	private static final String PROPERTIES_FILE = "alfa.properties";
	private static PropertyHandler INSTANCE = new PropertyHandler();
	private Properties properties = new Properties();
	private boolean havePropertiesChanged = true;
	
	private PropertyHandler() {
	}
	
	public static PropertyHandler getInstance() {
		return INSTANCE;
	}
	
	public void writeProperties(Properties p) {
		try {
			p.store(new FileWriter(PROPERTIES_FILE), null);
			havePropertiesChanged = true;
		} catch (IOException e) {
			throw new RuntimeException("Could not write properties file ("+PROPERTIES_FILE+")", e);
		}
	}
	
	public Properties readProperties() {
		if(havePropertiesChanged) {
			readPropertiesFromDisk();
			havePropertiesChanged = false;
		}
		return properties;
	}

	private void readPropertiesFromDisk() {
		try {
			properties.load(new FileReader(PROPERTIES_FILE));
		} catch (FileNotFoundException e) {
			//silent catch is intended
		} catch (IOException e) {
			//silent catch is intended
		}
	}
}
