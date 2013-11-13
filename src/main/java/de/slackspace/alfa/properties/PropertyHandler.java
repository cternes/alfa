package de.slackspace.alfa.properties;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import de.slackspace.alfa.exception.ConfigurationException;


public class PropertyHandler {

	private String propertiesFile;
	private Properties properties = new Properties();
	private boolean havePropertiesChanged = true;
	
	public PropertyHandler(String propertiesFile) {
		if(propertiesFile == null || propertiesFile.isEmpty()) {
			throw new IllegalArgumentException("Parameter propertiesFile must not be null or empty");
		}
		
		this.propertiesFile = propertiesFile;
	}
	
	public void writeProperties(Properties p) {
		try {
			p.store(new FileWriter(propertiesFile), null);
			havePropertiesChanged = true;
		} catch (IOException e) {
			throw new ConfigurationException("Could not write properties file ("+propertiesFile+")", e);
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
			properties.load(new FileReader(propertiesFile));
		} catch (FileNotFoundException e) {
			throw new ConfigurationException("The properties file ("+propertiesFile+") was not found. Please provide one.", e);
		} catch (IOException e) {
			throw new ConfigurationException("The properties file ("+propertiesFile+") was not found. Please provide one.", e);
		}
	}
	
}
