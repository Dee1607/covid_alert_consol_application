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

	// Max limit after which synchronize will be called
	public static int LIMIT_TO_CALL_SYNC = 5;

	// Map of Contacts where: key- connected individual's hash code, value-
	// List<int>- date,duration
	public Map<String, List<Integer>> DEVICE_CONTACTS = new HashMap<String, List<Integer>>();

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

				// Creatiing File object for configuration file.
				File myObj = new File(configurationFile);
				if (myObj.exists()) {

					// Using scanner to read config file
					Scanner reader = new Scanner(myObj);
					while (reader.hasNext()) {

						// storing netork address
						String[] deviceData = reader.nextLine().split("=");

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

				this.contactTracer = contactTracer;

			}
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
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

			// Creating List of date and duration of the contact
			List<Integer> listDateAndTime = new ArrayList<Integer>();
			listDateAndTime.add(date);
			listDateAndTime.add(duration);

			// Storing individual's hashCode as a map key and List of date and duration as a
			// value inside HashMap
			DEVICE_CONTACTS.put(individual, listDateAndTime);

			// Call syncronizeData if limit of contacts exceeds.
			if (DEVICE_CONTACTS.size() >= LIMIT_TO_CALL_SYNC) {
				synchronizeData();
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
		// Storing testHash- a unique string into an arrayList
		ALL_TEST_HASH.add(testHash);
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

		boolean contactEstablished = false;

		if (NETWORK_ADDRESS != null && DEVICE_NAME != null) {

			String initiator = Integer.toString((NETWORK_ADDRESS + DEVICE_NAME).hashCode());
			String contactInfo = "";

			// testHash of mobileDevide user into xml
			String testHashesFormattedString = "";
			testHashesFormattedString += ("<device_tests>");
			for (String str : ALL_TEST_HASH) {
				testHashesFormattedString += ("\n\t<test_hash>" + str + "</test_hash>");
			}
			testHashesFormattedString += ("\n</device_tests>");

			// Contact XML
			String contactXMLString = "";
			contactXMLString += ("\n<contact_info>");

			for (String contact : DEVICE_CONTACTS.keySet()) {
				contactXMLString += ("\n\t<contact>" +
										"\n\t\t<contact_hash>" + contact + "</contact_hash>" +
										"\n\t\t<date>" + DEVICE_CONTACTS.get(contact).get(0) + "</date>" +
										"\n\t\t<duration>" + DEVICE_CONTACTS.get(contact).get(1) + "</duration>" +
									"\n\t</contact>");
			}
			contactXMLString += ("\n</contact_info>");

			// creating Xml of testHash of mobile device and contact info of contacted
			// devices
			//contactInfo = testHashesFormattedString + contactXMLString;

			contactInfo = contactXMLString;
			
			// Calling mobileContact of government class
			contactEstablished = contactTracer.mobileContact(initiator, contactInfo);

		}

		return contactEstablished;
	}
}