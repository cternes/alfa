package de.slackspace.alfa.date;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class DateFormatterTest {

	@Test
	public void testYearMonthDay() {
		Date date = createDate(2013, 10, 11);
		
		assertEquals("2013.11.11", DateFormatter.toYYYYMMDD(date));
	}
	
	@Test
	public void testYearMonthDayNull() {
		assertEquals("", DateFormatter.toYYYYMMDD(null));
	}
	
	@Test
	public void testYearMonthDayMinuteSecond() {
		Date date = createDate(2013, 10, 11, 15, 0, 0);
		
		assertEquals("2013-11-11T15:00:00", DateFormatter.toYYYYMMDDHHMMSS(date));
	}
	
	@Test
	public void testtestYearMonthDayMinuteSecondNull() {
		assertEquals("", DateFormatter.toYYYYMMDDHHMMSS(null));
	}

	private Date createDate(int year, int month, int day) {
		return createDate(year, month, day, 0, 0, 0);
	}
	
	private Date createDate(int year, int month, int day, int hour, int minute, int second) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day, hour, minute, second);
		
		return cal.getTime();
	}
}
