public class mainClass {

	public static void main(String args[]) {

		String mobileConfigFilePath0 = "/Users/deeppatel/Desktop/FinalProject/configFiles/configFile0.txt";
		String mobileConfigFilePath1 = "/Users/deeppatel/Desktop/FinalProject/configFiles/configFile1.txt";
		String mobileConfigFilePath2 = "/Users/deeppatel/Desktop/FinalProject/configFiles/configFile2.txt";
		String mobileConfigFilePath3 = "/Users/deeppatel/Desktop/FinalProject/configFiles/configFile3.txt";
		String mobileConfigFilePath4 = "/Users/deeppatel/Desktop/FinalProject/configFiles/configFile4.txt";

		String govConfigFilePath = "/Users/deeppatel/Desktop/FinalProject/configFiles/DemoDB.txt";
		
		Government objGovernment = new Government(govConfigFilePath);

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

//		String individial1Network = "1f2b::a3ds.a12d.1f13";
//		String individual1Device = "abc's Android Phone";
//		
//		String individial2Network = "1f2b::a3ds.a12d.1f14";
//		String individual2Device = "abc's Android Phone";
//		
//		String individial3Network = "1f2b::a3ds.a12d.1f15";
//		String individual3Device = "abc's Android Phone";
//		
//		String individial4Network = "1f2b::a3ds.a12d.1f16";
//		String individual4Device = "abc's Android Phone";
		
//		String individual1 = Integer.toString((individial1Network + individual1Device).hashCode());
//		String individual2 = Integer.toString((individial2Network + individual2Device).hashCode());
//		String individual3 = Integer.toString((individial3Network + individual3Device).hashCode());
//		String individual4 = Integer.toString((individial4Network + individual4Device).hashCode());

		objMobileDevice0.recordContact(Integer.toString("a1".hashCode()), 332, 10);
		objMobileDevice0.recordContact(Integer.toString("b1".hashCode()), 330, 10);
		objMobileDevice0.recordContact(Integer.toString("c1".hashCode()), 336, 5);
		objMobileDevice0.recordContact(Integer.toString("a1".hashCode()), 338, 40);
		objMobileDevice0.recordContact(Integer.toString("d1".hashCode()), 340, 10);
		objMobileDevice0.recordContact(Integer.toString("d1".hashCode()), 340, 5);

		
		objGovernment.recordTestResult("1A", 330, true);
		objGovernment.recordTestResult("2A", 332, true);
		objGovernment.recordTestResult("3A", 344, false);


		String testHashCode = "867asd";
		//objMobileDevice0.positiveTest(testHashCode);

		boolean reportToQuarantine = objMobileDevice0.synchronizeData();

		if(reportToQuarantine) {
			System.out.println("\nYou were in contect with COVID-19 positive tested individual(s) in 14 days period!!\n"
					+ "Please Quarantine yourself. ");
		}
		
		int gatheringDate = 225;
		int minSize = 5;
		int minTime = 10;
		float density = 5;
		int numOfGathering = objGovernment.findGatherings(gatheringDate, minSize, minTime, density);
		System.out.println(numOfGathering);
	}
}