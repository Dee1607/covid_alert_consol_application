import java.io.File;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Government {

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

		try {
			if (configurationFile != null) {

				// Calling openConfigurationFile to open configuration file and fatch database
				// related data
				openConfigurationFile(configurationFile);
			}
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}
	}

	/*
	 * openConfigurationFile() - Method to open configuration file to read and store
	 * database related data from it
	 * 
	 * params - configurationFile - a path of configuration file
	 */
	private void openConfigurationFile(String configurationFile) {

		if (configurationFile != null) {
			try {
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
			} catch (Exception e) {
				e.printStackTrace();
			}
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

			// Getting deviceHashCode as initiator
			String deviceHashCode = initiator;

			// Getting xml file as a formatted string
			String xmlFileToRead = contactInfo;

			// Creating connection to the data base
			Connection conn = startConnection();

			try {

				// Creating Document of the xml containing contact devices information
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = builder.parse(new InputSource(new StringReader(xmlFileToRead)));

				// Call syncWithDatabase()- To add new contacts from doc to the database.
				syncWithDatabase(conn, doc, deviceHashCode);

				// Call getCurrentDate() - To Get Current Date in number from 1st Jan 2020
				long numOfCurrentDate = getCurrentDate();

				// SQL quary to fatch contacted device's Hash code and date of contact
				String quarryToGetContactInfo = "select contactHash,contactDate from contacts where deviceHash = "
						+ deviceHashCode + ";";

				// Lists to store contacted device's hash and contact dates
				List<String> recentContactHash = new ArrayList<String>();
				List<Integer> recentContactDate = new ArrayList<Integer>();

				// Creating Statment and ResultSet to get contact devices and their contact
				// dates
				Statement stmt1 = conn.createStatement();
				ResultSet rs1 = stmt1.executeQuery(quarryToGetContactInfo);

				// Storing ResultSet(rs1) data into lists(recentContactHash & recentContactDate)
				while (rs1.next()) {
					// Retrieve by column name
					recentContactHash.add(rs1.getString("contactHash"));
					recentContactDate.add(rs1.getInt("contactDate"));
				}

				// Closing result set and statement
				rs1.close();
				stmt1.close();

				// Remove contacts and their dates from lists where contact dates were more than
				// 14 days old
				// or in the case of invalid date if the date are more than 14 days in
				for (int i = 0; i < recentContactHash.size(); i++) {
					if (Math.abs(numOfCurrentDate - recentContactDate.get(i)) >= 14) {

						// Remove data which are not relavent(contacts of 14 days or prior period) from
						// lists
						recentContactHash.remove(i);
						recentContactDate.remove(i);
						i--;
					}
				}

				// Check those devices who got in contact withing 14 day period
				for (String contactDevice : recentContactHash) {

					// Sql quary to fetch dates of contacted individual in 14 days period
					String quaryToGetTestDateOfPositive = "select testDate from testinfo where testHash in (select testHash from mobiledevices where deviceHash = '"
							+ contactDevice + "')and testResult = 'true';";

					// Creating statment & getting result set for contacted device's test result
					Statement stmt2 = conn.createStatement();
					ResultSet rs2 = stmt2.executeQuery(quaryToGetTestDateOfPositive);

					// Checking if result dates are prior to 14 days period or not.
					while (rs2.next()) {
						if (Math.abs(numOfCurrentDate - rs2.getInt("testDate")) <= 14) {

							recentContactHash.clear();
							recentContactDate.clear();
							// One contact with individual whos test result is positive in last 14 days and
							// get in contact with user(device) in last 14 days
							return true;
						}
					}
				}

				// Close Connection
				conn.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return contactedPositivePerson;
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
	 * syncWithDatabase() - Method to sync data of device with its teastHash and its
	 * contact information with the government database
	 * 
	 * params - conn- Connection to the government database, doc - Document
	 * containing test info and contact info of device in xml formate,
	 * deviceHashCode - device's hashCode
	 */
	private void syncWithDatabase(Connection conn, Document doc, String deviceHashCode) {

		if (conn != null && doc != null && deviceHashCode != null) {
			try {

				// Creating NodeList of node-"test" from given xml
				NodeList testHashList = doc.getElementsByTagName("test");

				// Going through each Node in nodeList for getting testHash
				for (int i = 0; i < testHashList.getLength(); i++) {

					// Getting first test Hash as node
					Node testHashNode = testHashList.item(i);

					// check weather node type matchess or not with xml
					if (testHashNode.getNodeType() == Node.ELEMENT_NODE) {

						// Converting node into element to get value
						Element eElement = (Element) testHashNode;

						// Prepared Statement to insert values of deviceHash and testHash into table of
						// government Database.
						PreparedStatement preparedStatement1 = conn
								.prepareStatement("INSERT into mobiledevices (deviceHash, testHash) VALUES (?,?)");
						preparedStatement1.setString(1, deviceHashCode);
						preparedStatement1.setString(2,
								eElement.getElementsByTagName("test_hash").item(0).getTextContent());

						// Executing prepared statment
						preparedStatement1.execute();
					}
				}

				// Creating NodeList of node-"contact" from given xml
				NodeList nodeList = doc.getElementsByTagName("contact");

				// Going through each Node in nodeList for getting contact details
				for (int i = 0; i < nodeList.getLength(); i++) {

					// Getting first contacts details as node
					Node node = nodeList.item(i);

					// check weather node type matchess or not with xml
					if (node.getNodeType() == Node.ELEMENT_NODE) {

						// Converting node into element to get value
						Element eElement = (Element) node;

						// Prepared Statement to insert values of deviceHash and contactHash,
						// contactDate and contactDuration into table of government Database.

						PreparedStatement preparedStatement2 = conn.prepareStatement(
								"INSERT into contacts(deviceHash, contactHash, contactDate, contactDuration) VALUES (?,?,?,?)");
						preparedStatement2.setString(1, deviceHashCode);
						preparedStatement2.setString(2,
								eElement.getElementsByTagName("contact_hash").item(0).getTextContent());
						preparedStatement2.setString(3, eElement.getElementsByTagName("date").item(0).getTextContent());
						preparedStatement2.setString(4,
								eElement.getElementsByTagName("duration").item(0).getTextContent());

						// Execute prepared statment
						preparedStatement2.execute();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * recordTestResult()- method to store test record's result on the database
	 * 
	 * parameters- testHash-hash value of invividual's test report, date- date of
	 * the report, result- result of that testHash
	 */
	public void recordTestResult(String testHash, int date, boolean result) {

		if (testHash != null) {

			// Establishing connection using start connection method.
			Connection conn = startConnection();

			try {

				// Prepared statment to set data into database
				PreparedStatement preparedStatement = conn
						.prepareStatement("INSERT into testinfo (testHash,testDate,testResult) VALUES (?,?,?)");
				preparedStatement.setString(1, testHash);
				preparedStatement.setInt(2, date);
				preparedStatement.setString(3, String.valueOf(result));

				// Executing prepared statment
				preparedStatement.execute();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * startConnection() - Method to establish connection with the database
	 * 
	 * return - Connection created from the credentials got from configuration file
	 */
	private Connection startConnection() {
		Connection conn = null;

		// Code to connect to database
		try {
			// Driver info String
			String DRIVER_INFO = "com.mysql.cj.jdbc.Driver";

			// Creating connection
			Class.forName(DRIVER_INFO);
			conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);

		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
		}
		return conn;
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

		String SQL1 = "select * from contacts where contactDate = " + date + ";";

		return 0;
	}
}