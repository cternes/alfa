package de.slackspace.alfa.azure;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

public class PartitionCalculatorTest {

	@Test
	public void testNull() {
		String key = PartitionCalculator.calculatePartitionKeyFor(null);
		assertEquals("0", key);
	}
	
	@Test
	public void testPartitionKeyGeneration() {
		Calendar cal = Calendar.getInstance();
		cal.set(2013, 10, 11, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		String key = PartitionCalculator.calculatePartitionKeyFor(cal);
		assertEquals("0635197212000000000", key);
	}
}
