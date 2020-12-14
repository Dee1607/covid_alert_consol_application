public class mainClass {

	public static void main(String args[]) {

		String mobileConfigFilePath0 = "/Users/deeppatel/Desktop/FinalProject/configFiles/configFile0.txt";
		String mobileConfigFilePath1 = "/Users/deeppatel/Desktop/FinalProject/configFiles/configFile1.txt";
		String mobileConfigFilePath2 = "/Users/deeppatel/Desktop/FinalProject/configFiles/configFile2.txt";
		String mobileConfigFilePath3 = "/Users/deeppatel/Desktop/FinalProject/configFiles/configFile3.txt";
		String mobileConfigFilePath4 = "/Users/deeppatel/Desktop/FinalProject/configFiles/configFile4.txt";
		String mobileConfigFilePath6 = "/Users/deeppatel/Desktop/FinalProject/configFiles/configFile6.txt";

		String govConfigFilePath = "/Users/deeppatel/Desktop/FinalProject/configFiles/DemoDB.txt";

		Government objGovernment = new Government(govConfigFilePath);

		objGovernment.recordTestResult("0A", 400, false);
		objGovernment.recordTestResult("1A", 330, false);
		objGovernment.recordTestResult("2A", 335, true);
		objGovernment.recordTestResult("3A", 344, false);

		MobileDevice objMobileDevice0 = new MobileDevice(mobileConfigFilePath0, objGovernment);

		MobileDevice objMobileDevice1 = new MobileDevice(mobileConfigFilePath1, objGovernment);
		objMobileDevice1.synchronizeData();

		MobileDevice objMobileDevice2 = new MobileDevice(mobileConfigFilePath2, objGovernment);
		objMobileDevice2.positiveTest("3A");
		objMobileDevice2.synchronizeData();

		MobileDevice objMobileDevice3 = new MobileDevice(mobileConfigFilePath3, objGovernment);
		objMobileDevice3.positiveTest("2A");
		objMobileDevice3.synchronizeData();

		MobileDevice objMobileDevice4 = new MobileDevice(mobileConfigFilePath4, objGovernment);
		objMobileDevice4.positiveTest("1A");
		objMobileDevice4.synchronizeData();

		MobileDevice objMobileDevice6 = new MobileDevice(mobileConfigFilePath6, objGovernment);
		objMobileDevice6.synchronizeData();


		// Alert Check test data

		objMobileDevice0.recordContact(Integer.toString("a1".hashCode()), 200, 10);
		objMobileDevice0.recordContact(Integer.toString("b1".hashCode()), 330, 90);
		objMobileDevice0.recordContact(Integer.toString("c1".hashCode()), 336, 50);
		objMobileDevice0.recordContact(Integer.toString("a1".hashCode()), 338, 40);
		objMobileDevice0.recordContact(Integer.toString("d1".hashCode()), 340, 10);
		objMobileDevice0.recordContact(Integer.toString("d1".hashCode()), 340, 5);

//		objMobileDevice0.recordContact(Integer.toString("c1".hashCode()), 340, 10);
//		objMobileDevice0.recordContact(Integer.toString("c1".hashCode()), 340, 5);


		
		//Gathering Test data

		objMobileDevice1.recordContact(Integer.toString("b1".hashCode()), 344, 5);
		objMobileDevice1.recordContact(Integer.toString("c1".hashCode()), 344, 10);
		objMobileDevice1.recordContact(Integer.toString("d1".hashCode()), 344, 10);
		objMobileDevice2.recordContact(Integer.toString("a1".hashCode()), 344, 10);
		objMobileDevice2.recordContact(Integer.toString("c1".hashCode()), 344, 10);
		objMobileDevice2.recordContact(Integer.toString("f1".hashCode()), 344, 10);
		objMobileDevice3.recordContact(Integer.toString("a1".hashCode()), 344, 10);
		objMobileDevice3.recordContact(Integer.toString("b1".hashCode()), 344, 10);
		objMobileDevice4.recordContact(Integer.toString("a1".hashCode()), 344, 10);
		objMobileDevice6.recordContact(Integer.toString("b1".hashCode()), 344, 10);

		objMobileDevice1.synchronizeData();
		objMobileDevice2.synchronizeData();
		objMobileDevice3.synchronizeData();
		objMobileDevice4.synchronizeData();
		objMobileDevice6.synchronizeData();

//		objMobileDevice0.recordContact(Integer.toString("a1".hashCode()), 344, 5);
//		objMobileDevice0.recordContact(Integer.toString("a1".hashCode()), 344, 15);

		String testHashCode = "0A";
		objMobileDevice0.positiveTest(testHashCode);

		boolean reportToQuarantine = objMobileDevice0.synchronizeData();

		if (reportToQuarantine) {
			System.out.println("\nYou were in contect with COVID-19 positive tested individual(s) in 14 days period!!\n"
					+ "Please Quarantine yourself. ");
		}

		int gatheringDate = 344;
		int minSize = 3;
		int minTime = 10;
		float density = (float) 0.7;
		int numOfGathering = objGovernment.findGatherings(gatheringDate, minSize, minTime, density);

		System.out.println("Number of Gathering:" + numOfGathering);
	}
}