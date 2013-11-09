package de.slackspace.alfa.date;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {

	private static SimpleDateFormat DATEFORMATTER_DAY = new SimpleDateFormat("yyyy.MM.dd");
	private static SimpleDateFormat DATEFORMATTER_SECOND = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static String toYYYYMMDD(Date date) {
		return DATEFORMATTER_DAY.format(date);
	}
	
	public static String toYYYYMMDDHHMMSS(Date date) {
		return DATEFORMATTER_SECOND.format(date);
	}
}
