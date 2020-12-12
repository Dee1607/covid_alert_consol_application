import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MobileDevice {

	// Network Address of mobile device
	private String NETWORK_ADDRESS = null;

	// Device name
	private String DEVICE_NAME = null;

	// Goverment object for database
	private Government contactTracer;

	// Map of Contacts where: key- connected individual's hash code, value-
	// List<int>- date,duration
	public Map<String, Map<Integer, Integer>> MAP_DEVICE_CONTACTS = new HashMap<String, Map<Integer, Integer>>();

	// ArrayList of mobileDevice users test results as testHash
	public List<String> ALL_TEST_HASH = new ArrayList<String>();

	/*
	 * MobileDevice() - Constructor to store data
	 * 
	 * parameters- configurationFile- File containing network address, device name,
	 * contactTracer- Government class object
	 */
	public MobileDevice(String configurationFile, Government contactTracer) {

		try {
			if (configurationFile != null && contactTracer != null) {

				// Calling openConfigurationFile to Open configuration file and read network
				// address of device and device name
				openConfigurationFile(configurationFile);

				this.contactTracer = contactTracer;

			}
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}
	}

	/*
	 * openConfigurationFile() - Method to open configuration file to read and store
	 * data from it
	 * 
	 * params - configurationFile - a path of configuration file
	 */
	private void openConfigurationFile(String configurationFile) {

		if (configurationFile != null) {
			try {
				// Creatiing File object for configuration file.
				File myObj = new File(configurationFile);

				// Check weather file exist or not
				if (myObj.exists()) {

					// Using scanner to read config file
					Scanner reader = new Scanner(myObj);

					// While has next line to read
					while (reader.hasNext()) {

						// storing device details into array line by line
						String[] deviceData = reader.nextLine().split("=");

						// Storing network address
						if (deviceData[0].equals("address")) {
							this.NETWORK_ADDRESS = deviceData[1];
						}

						// storing device name
						if (deviceData[0].equals("deviceName")) {
							this.DEVICE_NAME = deviceData[1];
						}
					}
					// Close reader
					reader.close();

				} else {
					System.out.println("The file does not exist.");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * recordContact()- method to store individual who came in contact of this
	 * mobile device
	 *
	 * parameters- individual- contacted device's hashCode for anonimity, date-count
	 * of days from 1st anuary 2020. duration- number of minutes devices stay in
	 * contact with each other
	 */
	public void recordContact(String individual, int date, int duration) {

		if (individual != null) {

			// Map to get if any individual with same naem already exist
			Map<Integer, Integer> mapContactDetails = MAP_DEVICE_CONTACTS.get(individual);

			// If some same individual already stored
			if (mapContactDetails != null) {

				// getting value on date to check if individual has contacted on the same day
				// twice
				Integer tempDurationOnDate = mapContactDetails.get(date);

				// if Contacted on same day
				if (tempDurationOnDate != null) {

					// Add duration to it
					mapContactDetails.replace(date, (tempDurationOnDate + duration));
				} else {
					// Add same individual with different date in inner map
					mapContactDetails.put(date, duration);
				}

				// Replace data for repeted individual as a new data
				MAP_DEVICE_CONTACTS.replace(individual, mapContactDetails);
			} else {

				// Map of new date and time for new individual contact
				Map<Integer, Integer> mapDateTime = new HashMap<Integer, Integer>();
				mapDateTime.put(date, duration);

				// Put new data of date and time into map
				MAP_DEVICE_CONTACTS.put(individual, mapDateTime);
			}
		}
	}

	/*
	 * positiveTest()- method to store data into arrayList of unique test result
	 * Code
	 * 
	 * parameters- testHash- a unique hashCode of the test result
	 */
	public void positiveTest(String testHash) {

		if (testHash != null) {
			if (testHash.matches("^[a-zA-Z0-9]*$")) {

				// Storing testHash- a unique string into an arrayList
				ALL_TEST_HASH.add(testHash);
			}
		}
	}

	/*
	 * synchronizeData()- method to store contacted devices into the database &
	 * check weather user has been in a contact with someone whos test result is
	 * positive
	 * 
	 * return- boolean true- if Get in contact with someone who has positive results
	 * false- otherwise
	 */
	public boolean synchronizeData() {

		// To store result after Checking from database
		boolean contactEstablished = false;

		if (NETWORK_ADDRESS != null && DEVICE_NAME != null) {

			// Creating initiator from network address and device name
			String initiator = Integer.toString((NETWORK_ADDRESS + DEVICE_NAME).hashCode());

			// ContactInfo in XML format
			String contactInfo = "";

			// String of test hashes od mobile device in XML format string
			String testHashesFormattedString = "";

			// Generating xml for testHashes
			testHashesFormattedString += ("<device_tests>");
			for (String str : ALL_TEST_HASH) {

				// Formatting String into xml
				testHashesFormattedString += ("\n\t<test>" + "\n\t\t<test_hash>" + str + "</test_hash>"
						+ "\n\t</test>");
			}
			testHashesFormattedString += ("\n</device_tests>");

			// String of contacts details of mobile device in XML format string
			String contactXMLString = "";

			// Generating xml for contact details
			contactXMLString += ("\n<contact_info>");
			for (String contact : MAP_DEVICE_CONTACTS.keySet()) {
				Map<Integer, Integer> tempMap = MAP_DEVICE_CONTACTS.get(contact);

				for (Integer contactDate : tempMap.keySet()) {

					// Formatting String into xml
					contactXMLString += ("\n\t<contact>" + "\n\t\t<contact_hash>" + contact + "</contact_hash>"
							+ "\n\t\t<date>" + contactDate + "</date>" + "\n\t\t<duration>" + tempMap.get(contactDate)
							+ "</duration>" + "\n\t</contact>");
				}
			}
			contactXMLString += ("\n</contact_info>");

			// creating Xml of testHash of mobile device and contact info of with main root
			// tag of xml containing all details
			contactInfo = "<government_data>\n" + testHashesFormattedString + contactXMLString + "\n</government_data>";

			// Calling mobileContact of government class
			contactEstablished = contactTracer.mobileContact(initiator, contactInfo);

			// releasing memory by clearing synced data
			MAP_DEVICE_CONTACTS.clear();
			ALL_TEST_HASH.clear();
		}

		return contactEstablished;
	}
}