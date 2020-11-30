import java.io.File;
import java.util.Scanner;

public class Government {

	private String configurationFile;

	private String DATABASE;
	private String USERNAME;
	private String PASSWORD;

	/*
	 * Government()- Constructor to store database related information like URL to
	 * Database, username and password
	 * 
	 * parameters- configurationFile- File containing database related info
	 */
	public Government(String configurationFile) {
		this.configurationFile = configurationFile;
		try {
			if (configurationFile != null) {

				// Creatiing File object for configuration file.
				File objFile = new File(configurationFile);
				if (objFile.exists()) {

					// Using scanner to read config file
					Scanner reader = new Scanner(objFile);
					while (reader.hasNext()) {

						// storing Database URL
						String[] deviceData = reader.nextLine().split("=", 2);

						if (deviceData[0].equals("database")) {
							this.DATABASE = deviceData[1];
						}

						// storing username
						if (deviceData[0].equals("user")) {
							this.USERNAME = deviceData[1];
						}

						// storing password
						if (deviceData[0].equals("password")) {
							this.PASSWORD = deviceData[1];
						}
					}

					// Close reader
					reader.close();

				} else {
					System.out.println("The file does not exist.");
				}
			}
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}
	}

	/*
	 * mobileContact()- Method to get contacts & store them into database and
	 * retrive if any contact tested positive in last 14 days
	 * 
	 * parameters- initiator- mobileDevice info, contactInfo- contacted devices data
	 * witl mobile device's test hash
	 * 
	 * return- true- if any contact tested positive in last 14 days, false-
	 * otherwise
	 */
	public boolean mobileContact(String initiator, String contactInfo) {

		boolean contactedPositivePerson = false;

		if (initiator != null && contactInfo != null) {

			String deviceHashCode = initiator;
			String xmlFileToRead = contactInfo;

			// code to read xml file & store data into data structure

			// Code to connect to database
		}

		return contactedPositivePerson;
	}

	/*
	 * recordTestResult()- method to store test record's result on the database
	 * 
	 * parameters- testHash-hash value of invividual's test report, date- date of
	 * the report, result- result of that testHash
	 */
	public void recordTestResult(String testHash, int date, boolean result) {

	}

	/*
	 * findGatherings()- method to fing small gathering on perticulare date
	 * 
	 * parameters- date- date of the gathering, minSize- min number of people can
	 * gathered, minTime- gather for minTime in minutes, density- density of
	 * gathering
	 * 
	 * return- number of large gathering happend on perticular date
	 */
	public int findGatherings(int date, int minSize, int minTime, float density) {

		return 0;
	}
}