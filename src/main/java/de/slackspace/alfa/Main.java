package de.slackspace.alfa;

public class Main {

	public static void main(String[] args) {
		try {
			new Alfa();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
