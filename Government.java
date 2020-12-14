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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Government {

	// Database Credentials
	private String DATABASE = null;
	private String USERNAME = null;
	private String PASSWORD = null;

	/*
	 * Government()- Constructor to store database related information like URL to
	 * Database, username and password
	 * 
	 * parameters- configurationFile- File containing database related info
	 */
	public Government(String configurationFile) {

		try {

			// Calling openConfigurationFile to open configuration file and fatch database
			// related data
			openConfigurationFile(configurationFile);

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

		if (configurationFile != null && !configurationFile.isEmpty()) {
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
				System.out.println("ERROR: " + e.getMessage());
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

			if (conn != null) {

				try {

					// Creating Document of the xml containing contact devices information
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document doc = builder.parse(new InputSource(new StringReader(xmlFileToRead)));

					// Call syncWithDatabase()- To add new contacts from doc to the database.
					syncWithDatabase(conn, doc, deviceHashCode);

					// Call getCurrentDate() - To Get Current Date in number from 1st Jan 2020
					long numOfCurrentDate = getCurrentDate();

					// Lists to store contacted device's hash and contact dates
					List<String> recentContactHash = new ArrayList<String>();
					List<Integer> recentContactDate = new ArrayList<Integer>();

					// SQL quary to fatch contacted device's Hash code and date of contact
					String quarryToGetContactInfo = "select contactHash,contactDate from contacts where deviceHash = "
							+ deviceHashCode + " and contactDate between " + (numOfCurrentDate - 20) + " and "
							+ numOfCurrentDate;

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

					// Instanitiating result set
					ResultSet rs2 = null;

					// Check those devices who got in contact withing 14 day period
					for (String contactDevice : recentContactHash) {

						// Sql quary to fetch dates of contacted individual in 14 days period
						String quaryToGetTestDateOfPositive = "select testHash from testinfo where testHash in "
								+ "(select testHash from mobiledevices where deviceHash = '" + contactDevice
								+ "' )and testResult = 'true' and testDate between " + (numOfCurrentDate - 14) + " and "
								+ numOfCurrentDate;

						// Creating statment & getting result set for contacted device's test result
						Statement stmt2 = conn.createStatement();
						rs2 = stmt2.executeQuery(quaryToGetTestDateOfPositive);

						// Storing positive tested devices's testHash into list
						List<String> positiveTestedHash = new ArrayList<String>();
						while (rs2.next()) {
							positiveTestedHash.add(rs2.getString("testHash"));
						}

						// rs2 and stmt2 closed
						rs2.close();
						stmt2.close();

						// Check if device is already alerted for this contact or not
						contactedPositivePerson = checkForAlert(conn, contactedPositivePerson, positiveTestedHash);

					}

					// Close Connection
					conn.close();
				} catch (Exception e) {
					System.out.println("ERROR: " + e.getMessage());
				}
			}
		}

		return contactedPositivePerson;
	}

	/*
	 * checkForAlert() - Method to check weather an alert should be sent or not
	 * 
	 * params - conn- connection to government database, contactedPositivePerson-
	 * Boolean parameter to be returned, positiveTestedHash- List of positive tested
	 * individual's test hash
	 * 
	 * return - contactedPositivePerson- true: if alert must be sent, false:
	 * otherwise
	 */
	private boolean checkForAlert(Connection conn, boolean contactedPositivePerson, List<String> positiveTestedHash) {

		if (conn != null) {
			try {
				// Checking each positive tested Hash for existance
				for (String temp : positiveTestedHash) {

					// SQL quary to find contact details from device's testHash
					String sqlToGetDataFromTestHash = "select deviceHash,contactHash,contactDate from contacts where contactHash in (select deviceHash from mobiledevices where testHash in (select testHash from testinfo where testHash = '"
							+ temp + "'))";

					// Creating statment and resultSet for quary
					Statement stmt3 = conn.createStatement();
					ResultSet rs3 = stmt3.executeQuery(sqlToGetDataFromTestHash);

					// check result set data if exists
					while (rs3.next()) {

						// sql quary to find data from alert info
						String sqlTogetAlertedData = "select id from alertinfo where deviceHash = '" + rs3.getString(1)
								+ "' and" + " contactHash = '" + rs3.getString(2) + "' and contactDate = "
								+ rs3.getInt(3);

						// creating statment and result set for the quary
						Statement stmt4 = conn.createStatement();
						ResultSet rs4 = stmt4.executeQuery(sqlTogetAlertedData);

						// Storing result of data if exist in bCheck
						boolean bCheck = rs4.next();

						// Check if data already alerted
						while (!bCheck) {

							// Alert device user for contact with positive tested individual
							contactedPositivePerson = true;

							// Prepare statment to insert alerted contacts into seperate table
							PreparedStatement preparedStatement = conn.prepareStatement(
									"INSERT into alertinfo (deviceHash,contactHash,contactDate) VALUES (?,?,?)");
							preparedStatement.setString(1, rs3.getString(1));
							preparedStatement.setString(2, rs3.getString(2));
							preparedStatement.setInt(3, rs3.getInt(3));
							preparedStatement.execute();
							bCheck = true;
						}

						// Closing statments and result sets.
						stmt4.close();
						rs4.close();
					}

					// Closing statments and result sets.
					stmt3.close();
					rs3.close();
				}
			} catch (Exception e) {
				System.out.println("ERROR: " + e.getMessage());
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

						// SQL quary to get contact information alrady existed
						String sqlToCheckTestHashExist = "select * from mobileDevices where deviceHash = '"
								+ deviceHashCode + "' and " + "testHash = '"
								+ eElement.getElementsByTagName("test_hash").item(0).getTextContent() + "'";

						boolean bCheckForData = false;
						bCheckForData = checkTableForDataBySQL(conn, bCheckForData, sqlToCheckTestHashExist);

						if (!bCheckForData) {

							// Sql quary to validate testHash from agencies entred table.
							String sqlToCheckTestHashInDatabase = "select id from testinfo where " + "testHash = '"
									+ eElement.getElementsByTagName("test_hash").item(0).getTextContent() + "'";

							boolean bCheck = false;
							bCheck = checkTableForDataBySQL(conn, bCheck, sqlToCheckTestHashInDatabase);

							// If testinfo table managed by agencies contains testHash only than add into
							// mobiledevice's testHash
							if (bCheck) {

								// Prepared Statement to insert values of deviceHash and testHash into table of
								// government Database.
								PreparedStatement preparedStatement1 = conn.prepareStatement(
										"INSERT into mobiledevices (deviceHash, testHash) VALUES (?,?)");
								preparedStatement1.setString(1, deviceHashCode);
								preparedStatement1.setString(2,
										eElement.getElementsByTagName("test_hash").item(0).getTextContent());

								// Executing prepared statment
								preparedStatement1.execute();
							}
						}
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

						// SQL quary to get contact information alrady existed
						String quaryToGetContactInfo = "select id,contactDuration from contacts where deviceHash = '"
								+ deviceHashCode + "' and " + "contactHash ='"
								+ eElement.getElementsByTagName("contact_hash").item(0).getTextContent()
								+ "' and contactDate ="
								+ eElement.getElementsByTagName("date").item(0).getTextContent();

						// Creating statment and result set for that quary
						Statement stmt = conn.createStatement();
						ResultSet rs = stmt.executeQuery(quaryToGetContactInfo);

						// Store in variable if any similer data exists
						boolean check = rs.next();

						// If any similer contact found on same date
						if (check) {

							// Combine duration of contact of both meet on same day
							int sum = rs.getInt(2) + Integer
									.parseInt(eElement.getElementsByTagName("duration").item(0).getTextContent());

							// Update sql quary to update combined duration of both contact
							String updateQuary = "Update contacts set contactDuration = " + sum + " where id = "
									+ rs.getInt(1);

							// Creating statment to update quary
							Statement stmt1 = conn.createStatement();
							stmt1.executeUpdate(updateQuary);

						} else {

							// Prepared Statement to insert values of deviceHash and contactHash,
							// contactDate and contactDuration into table of government Database.
							PreparedStatement preparedStatement2 = conn.prepareStatement(
									"INSERT into contacts(deviceHash, contactHash, contactDate, contactDuration) VALUES (?,?,?,?)");
							preparedStatement2.setString(1, deviceHashCode);
							preparedStatement2.setString(2,
									eElement.getElementsByTagName("contact_hash").item(0).getTextContent());
							preparedStatement2.setString(3,
									eElement.getElementsByTagName("date").item(0).getTextContent());
							preparedStatement2.setString(4,
									eElement.getElementsByTagName("duration").item(0).getTextContent());

							// Execute prepared statment
							preparedStatement2.execute();
						}
					}
				}

			} catch (Exception e) {
				System.out.println("ERROR: " + e.getMessage());
			}
		}
	}

	/*
	 * checkTableForDataBySQL()- Method to check into the database for given sql
	 * quary to check weather contains any data on given quary or not.
	 * 
	 * params- conn- Connection to database, bCheck- boolean value to return,
	 * sqlQuary- quary to run on db
	 * 
	 * returns- true: if any record for given sql quary exist using rs.next(),
	 * false: otherwise
	 */
	public boolean checkTableForDataBySQL(Connection conn, boolean bCheck, String sqlToCheckTestHashExist) {

		if (conn != null) {

			try {
				// Creating statment and result set for that quary
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sqlToCheckTestHashExist);

				// Store result in bCheck to return
				bCheck = rs.next();

				// Statment and result set closed.
				stmt.close();
				rs.close();
			} catch (Exception e) {
				System.out.println("ERROR: " + e.getMessage());
			}
		}
		return bCheck;
	}

	/*
	 * recordTestResult()- method to store test record's result on the database
	 * 
	 * parameters- testHash-hash value of invividual's test report, date- date of
	 * the report, result- result of that testHash
	 */
	public void recordTestResult(String testHash, int date, boolean result) {

		if (testHash != null && !testHash.isEmpty() && !(date < 0)) {

			// Establishing connection using start connection method.
			Connection conn = startConnection();

			try {
				// Sql quary to validate testHash from agencies entred table.
				String sqlToCheckTestHashInDatabase = "select id from testinfo where " + "testHash = '" + testHash
						+ "'";

				boolean bCheck = false;
				bCheck = checkTableForDataBySQL(conn, bCheck, sqlToCheckTestHashInDatabase);

				while (!bCheck) {
					// Prepared statment to set data into database
					PreparedStatement preparedStatement = conn
							.prepareStatement("INSERT into testinfo (testHash,testDate,testResult) VALUES (?,?,?)");
					preparedStatement.setString(1, testHash);
					preparedStatement.setInt(2, date);
					preparedStatement.setString(3, String.valueOf(result));

					// Executing prepared statment
					preparedStatement.execute();
					bCheck = true;
				}

			} catch (Exception e) {
				System.out.println("ERROR: " + e.getMessage());
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

		if (DATABASE != null && USERNAME != null) {

		}
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

		int numOfGatherings = 0;

		if (!(date < 0) && !(minSize < 2) && !(minTime < 0) && !(density < 0) && !(density > 1)) {

			try {

				// Creating connection statment and result set for quary.
				Connection conn = startConnection();

				// Quary to find contacts on perticular date and contacts whos contact duration
				// is greater than min Duration
				String sqlForAllContactOnDate = "select deviceHash,contactHash from contacts where contactDate = "
						+ date + " and contactDuration >=" + minTime;

				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sqlForAllContactOnDate);

				// List of two seperate devices and contacts
				List<String> deviceList = new ArrayList<String>();
				List<String> contactList = new ArrayList<String>();

				// Check entries in result set
				while (rs.next()) {

					// flag to know when to add data
					boolean flag = false;

					// Check weather deviceList or contact list both contains same pair into lists
					// to remove duplicate entries
					if (deviceList.contains(rs.getString(1)) && contactList.contains(rs.getString(2))
							|| deviceList.contains(rs.getString(2)) && contactList.contains(rs.getString(1))) {

						// Check each List as both list will bve of same size.
						for (int i = 0; i < deviceList.size(); i++) {

							// Check device list's index of the data if duplicates may be possible
							if (deviceList.get(i).equals(rs.getString(1))) {

								// Match same indecx value of contact list and confirm duplicates
								if (contactList.get(i).equals(rs.getString(2))) {

									// turn flag value Bcz match is found and no need to add that data
									flag = true;
									break;
								} else {
									flag = false;
								}
							}

							// Check contact list's index of the data if duplicates may be possible
							if (contactList.get(i).equals(rs.getString(1))) {

								// Match same indecx value of device list and confirm duplicates
								if (deviceList.get(i).equals(rs.getString(2))) {

									// turn flag value Bcz match is found and no need to add that data
									flag = true;
									break;
								} else {
									flag = false;
								}
							}
						}
					}

					// Flag value true do not add data and false means new occurance so add data
					if (!flag) {

						// Add Data without any duplicate entries
						deviceList.add(rs.getString(1));
						contactList.add(rs.getString(2));
					}
				}

				// Adjecency list as Map where key is Integer and value is List<Integer>
				Map<Integer, List<Integer>> adjMap = new HashMap<Integer, List<Integer>>();

				// Calling method to create adjecency list.
				adjMap = createAdjecencyList(deviceList, contactList);

				// Calculate number of gathering from adjecency list
				numOfGatherings = calculateGathering(adjMap, minSize, density);

			} catch (Exception e) {
				System.out.println("ERROR: " + e.getMessage());
			}
		}

		return numOfGatherings;
	}

	/*
	 * createAdjecencyList()- Method to create adjecency list using map and list
	 * 
	 * params- deviceList,contactList:- unique pairs of contact working as pairs
	 * 
	 * return- Return adjecency list in formate of Map<Integer,List<Integer>>
	 */
	public Map<Integer, List<Integer>> createAdjecencyList(List<String> deviceList, List<String> contactList) {

		Map<Integer, List<Integer>> adjMap = new HashMap<Integer, List<Integer>>();

		// go through each value of both lists: devicelist and contactlist
		for (int i = 0; i < deviceList.size(); i++) {

			// tempList to add into map(adjMap) as adjecency list
			List<Integer> tempList = new ArrayList<Integer>();

			// if adjecency list(as a map) already contains key to be added from devicelist
			if (adjMap.containsKey(Integer.parseInt(deviceList.get(i)))) {

				// if same key exist than get assosiated list value and store it in tempList and
				// add value of the device list on same index to that list
				tempList = adjMap.get(Integer.parseInt(deviceList.get(i)));
				tempList.add(Integer.parseInt(contactList.get(i)));

				// replace key value with tempList
				adjMap.replace(Integer.parseInt(deviceList.get(i)), tempList);
			} else {

				// get data of contactlist on same index and add it in tempList and then simply
				// put data into adjMap
				tempList.add(Integer.parseInt(contactList.get(i)));
				adjMap.put(Integer.parseInt(deviceList.get(i)), tempList);
			}

			// re-instanciated tempList to check same for contactList for same index
			tempList = new ArrayList<Integer>();

			// if adjecency list(as a map) already contains key to be added from contactlist
			if (adjMap.containsKey(Integer.parseInt(contactList.get(i)))) {

				// if same key exist than get assosiated list value and store it in tempList and
				// add value of the contact list on same index to that list
				tempList = adjMap.get(Integer.parseInt(contactList.get(i)));
				tempList.add(Integer.parseInt(deviceList.get(i)));

				// replace key value with tempList
				adjMap.replace(Integer.parseInt(contactList.get(i)), tempList);
			} else {

				// get data of devicelist on same index and add it in tempList and then simply
				// put data into adjMap
				tempList.add(Integer.parseInt(deviceList.get(i)));
				adjMap.put(Integer.parseInt(contactList.get(i)), tempList);
			}
		}
		return adjMap;
	}

	/*
	 * calculateGathering()- Method to calculate number of gatherings from adjecency
	 * map
	 * 
	 * patams- adjMap:- Adjecency List as a map, minSize:- minimum gathering size,
	 * density:- minimum density if exceeds limit
	 * 
	 * returns- Number of gatherings on a perticular date
	 */
	private int calculateGathering(Map<Integer, List<Integer>> adjMap, int minSize, float density) {

		int numOfGatherings = 0;

		// Lists to store sets,connections,max possible connections and density of each
		// pair.
		List<Integer> set = new ArrayList<Integer>();
		List<Integer> connection = new ArrayList<Integer>();
		List<Integer> maxConnection = new ArrayList<Integer>();
		List<Float> pairDensity = new ArrayList<Float>();

		// Get keyset of the adjMap and convert it to list to access data by index
		Set<Integer> tempSet = adjMap.keySet();
		List<Integer> tempList = new ArrayList<Integer>(tempSet);

		// Check each key value pair of map
		for (int i = 0; i < adjMap.size() - 1; i++) {

			// Check index i value in a pair with index j value
			for (int j = i + 1; j < adjMap.size(); j++) {

				// Get neighbour list values from map to check sets
				List<Integer> checkleft = adjMap.get(tempList.get(i));
				List<Integer> checkright = adjMap.get(tempList.get(j));

				// counter for calculating set
				int setCount = 0;

				// Counter for calculating connection
				int connCount = 0;

				// look each element from check right
				for (int k = 0; k < checkleft.size(); k++) {

					// check for matching element from both left and right list
					if (checkright.contains(checkleft.get(k))) {

						// increment set count by one as one connective node found
						setCount++;

						// both node from checklists connect to same node which creates 2 connective
						// edges
						connCount = connCount + 2;
					}
				}

				// Check one list contains node of another list itself which creates one
				// connection between them.
				if (checkleft.contains(tempList.get(j))) {
					connCount = connCount + 1;
				}

				// Final counted sets is counted set and additional two for both pair nodes
				int finalSetCount = setCount + 2;

				// Add data into set and connection lists
				set.add(finalSetCount);
				connection.add(connCount);

				// Calculating max connection from the equation c = ( n * (n-1) / 2 )
				int maxConn = (finalSetCount * (finalSetCount - 1)) / 2;
				maxConnection.add(maxConn);

				// Parse connection count and max connection count to convert them into floats
				// for further calculation of density in decimal points
				float connect = Float.parseFloat(String.valueOf(connCount));
				float maxConnectCount = Float.parseFloat(String.valueOf(maxConn));

				// Calculating density by equation (conneection / max connection)
				float finalDesnsity = connect / maxConnectCount;
				pairDensity.add(finalDesnsity);

				// Check weather found density is greater than given minimum density
				// Check weather gethering of people in set are higher than min gathering size
				if (finalDesnsity > density && finalSetCount >= minSize) {
					numOfGatherings++;
				}
			}
		}
		return numOfGatherings;
	}
}