package de.slackspace.alfa.azure;

import java.util.Calendar;

public class PartitionCalculator {

	private PartitionCalculator() {}
	
	public static String calculatePartitionKeyFor(Calendar calendar) {
		if(calendar != null) {
			//simulate c# ticks
			long ticks = 621355968000000000L + calendar.getTimeInMillis() * 10000;
			
			//append a leading zero for azure
			return "0" + String.valueOf(ticks);
		}

		return "0";
	}
}
