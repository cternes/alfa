package de.slackspace.alfa.properties;

public class PropertyHandlerFactory {

	private static PropertyHandler INSTANCE;
	
	public static PropertyHandler createPropertyHandler(String propertiesFile) {
		if(INSTANCE == null) {
			INSTANCE = new PropertyHandler(propertiesFile); 
		}
		return INSTANCE; 
	}
}
