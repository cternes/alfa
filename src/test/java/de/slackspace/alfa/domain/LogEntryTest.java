package de.slackspace.alfa.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LogEntryTest {

	@Test
	public void testSeverityMapping() {
		LogEntry logEntry = createLogEntry("1");
		assertEquals("Critical", logEntry.getSeverity());
		
		logEntry = createLogEntry("2");
		assertEquals("Error", logEntry.getSeverity());
		
		logEntry = createLogEntry("3");
		assertEquals("Warning", logEntry.getSeverity());
		
		logEntry = createLogEntry("4");
		assertEquals("Information", logEntry.getSeverity());
		
		logEntry = createLogEntry("5");
		assertEquals("Verbose", logEntry.getSeverity());
	}

	private LogEntry createLogEntry(String level) {
		LogEntry logEntry = new LogEntry();
		logEntry.setLevel(level);
		return logEntry;
	}
}
