package de.slackspace.alfa.properties;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import de.slackspace.alfa.exception.ConfigurationException;


public class PropertyHandler {

	public static final String ACCOUNT_NAME = "accountName";
	public static final String ACCOUNT_KEY = "accountKey";
	public static final String ACCOUNT_URL = "accountUrl";
	
	private String propertiesFile;
	private Properties properties = new Properties();
	private boolean havePropertiesChanged = true;
	
	PropertyHandler(String propertiesFile) {
		if(propertiesFile == null || propertiesFile.isEmpty()) {
			throw new IllegalArgumentException("Parameter propertiesFile must not be null or empty");
		}
		
		this.propertiesFile = propertiesFile;
	}
	
	public void writeProperties() {
		try {
			properties.store(new FileWriter(propertiesFile), null);
			havePropertiesChanged = true;
		} catch (IOException e) {
			throw new ConfigurationException("Could not write properties file ("+propertiesFile+")", e);
		}
	}
	
	private Properties readProperties() {
		if(havePropertiesChanged) {
			readPropertiesFromDisk();
			havePropertiesChanged = false;
		}
		return properties;
	}

	private void readPropertiesFromDisk() {
		try {
			properties.load(new FileReader(propertiesFile));
		} catch (IOException e) {
			throw new ConfigurationException("The properties file ("+propertiesFile+") was not found. Please provide one.", e);
		}
	}

	public int getNumberOfAccounts() {
		Properties properties = readProperties();
		
		int i = 1;
		while(true) {
			String value = properties.getProperty(String.format("%s_%s", ACCOUNT_URL, i));
			if(value == null) {
				return i - 1;
			}
			i++;
		}
	}

	public String getProperty(String key, int instance) {
		return getProperty(prepareKey(key, instance));
	}

	public String getProperty(String key) {
		Properties properties = readProperties();
		return properties.getProperty(key);
	}
	
	public void setProperty(String key, String value, int instance) {
		setProperty(prepareKey(key, instance), value);
	}

	public void setProperty(String key, String value) {
		if(key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Parameter key must not be null or empty");
		}
		
		if(value == null || value.isEmpty()) {
			throw new IllegalArgumentException("Parameter value must not be null or empty");
		}
			 
		properties.setProperty(key, value);
		havePropertiesChanged = true;
	}

	private String prepareKey(String key, int instance) {
		return String.format("%s_%s", key, instance);
	}
}
