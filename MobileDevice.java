import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/*
 * Class:- MobileDevice - Class for mobile device user to record contact, 
 * 							add test results via testHash and 
 * 							synchronize data with government database.
 */
public class MobileDevice {

	// Network Address of mobile device
	private String NETWORK_ADDRESS = null;

	// Device name
	private String DEVICE_NAME = null;

	// Goverment object for database
	private Government contactTracer;

	// Map of Contacts where: key- connected individual's hash code, value-
	// Map<Integer,DeviceContacts>- where key is date of contact and value is
	// DeviceContacts class's object
	Map<String, Map<Integer, DeviceContacts>> MAP_DEVICE_CONTACTS = new HashMap<String, Map<Integer, DeviceContacts>>();

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
			if (configurationFile != null && contactTracer != null && !configurationFile.isEmpty()) {

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
				System.out.println("ERROR: " + e.getMessage());
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

		if (individual != null && !individual.isEmpty() && !(date < 0) && !(date > getCurrentDate()) && !(duration <= 0)
				&& !(duration > 1440)) {

			DeviceContacts objContacts = null;
			Map<Integer, DeviceContacts> listTempContact = MAP_DEVICE_CONTACTS.get(individual);

			if (MAP_DEVICE_CONTACTS.containsKey(individual)) {
				// Map to get if any individual with same naem already exist

				if (listTempContact.containsKey(date)) {
					int finalDuration = listTempContact.get(date).getContactDuration();

					// Add two duration if contacts on ssame day twice
					finalDuration = finalDuration + duration;
					listTempContact.get(date).setContactDuration(finalDuration);

				} else {

					// add
					objContacts = new DeviceContacts(individual, date, duration);
					listTempContact.put(date, objContacts);

				}
			} else {
				objContacts = new DeviceContacts(individual, date, duration);
				listTempContact = new HashMap<Integer, DeviceContacts>();
				listTempContact.put(date, objContacts);
			}
			MAP_DEVICE_CONTACTS.put(individual, listTempContact);
		}
	}

	/*
	 * getCurrentDate() - Method to get current date in terms of number
	 * 
	 * return - number of days from 1st Jan 2020 till today
	 */
	private long getCurrentDate() {

		// Preparing date in "yyyy-MM-dd" format
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		// Get current date
		Date date = Calendar.getInstance().getTime();

		// Convert Current date and date of 1st jan 2020 into String
		String currentDate = dateFormat.format(date);
		String startDate = "2020-01-01";

		// Getting dates in Local Date type
		LocalDate date1 = LocalDate.parse(startDate);
		LocalDate date2 = LocalDate.parse(currentDate);

		return ChronoUnit.DAYS.between(date1, date2);
	}

	/*
	 * positiveTest()- method to store data into arrayList of unique test result
	 * Code
	 * 
	 * parameters- testHash- a unique hashCode of the test result
	 */
	public void positiveTest(String testHash) {

		if (testHash != null && !testHash.isEmpty()) {
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
			String contactInfo = getXMLFormattedString();

			// Calling mobileContact of government class
			contactEstablished = contactTracer.mobileContact(initiator, contactInfo);

			// releasing memory by clearing synced data
			MAP_DEVICE_CONTACTS.clear();
			ALL_TEST_HASH.clear();
		}

		return contactEstablished;
	}

	/*
	 * getXMLFormattedString() - Method to generate XML from the all test hashes and
	 * device contacts
	 * 
	 * returns - XML formatted String
	 */
	private String getXMLFormattedString() {

		// Final xml format string
		String contactInfo = "";

		// String of test hashes od mobile device in XML format string
		String testHashesFormattedString = "";

		// Generating xml for testHashes
		testHashesFormattedString += ("\t<device_tests>");
		for (String str : ALL_TEST_HASH) {

			// Formatting String into xml
			testHashesFormattedString += ("\n\t\t<test>" + "\n\t\t\t<test_hash>" + str + "</test_hash>"
					+ "\n\t\t</test>");
		}
		testHashesFormattedString += ("\n\t</device_tests>");

		// Generating xml for contacts
		String contactXMLString = "";
		contactXMLString += ("\n\t<contact_info>");

		// Loop through each map data
		for (String contact : MAP_DEVICE_CONTACTS.keySet()) {
			Map<Integer, DeviceContacts> tempMap = MAP_DEVICE_CONTACTS.get(contact);

			// Loop through each inner map data
			for (int dateInfo : tempMap.keySet()) {
				DeviceContacts objContactData = tempMap.get(dateInfo);

				// Formatting String into xml
				contactXMLString += ("\n\t\t<contact>" + "\n\t\t\t<contact_hash>" + objContactData.contactHash
						+ "</contact_hash>" + "\n\t\t\t<date>" + objContactData.contactDate + "</date>"
						+ "\n\t\t\t<duration>" + objContactData.contactDuration + "</duration>" + "\n\t\t</contact>");
			}
		}
		contactXMLString += ("\n\t</contact_info>");

		// creating Xml of testHash of mobile device and contact info of with main root
		// tag of xml containing all details
		contactInfo = "<government_data>\n" + testHashesFormattedString + contactXMLString + "\n</government_data>";

		return contactInfo;
	}
}