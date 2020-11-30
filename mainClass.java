public class mainClass {

	public static void main(String args[]) {

		String mobileConfigFilePath = "/Users/deeppatel/Desktop/FinalProject/configFiles/configFile1.txt";
		String govConfigFilePath = "/Users/deeppatel/Desktop/FinalProject/configFiles/DatabaseConfig.txt";
		Government objGovernment = new Government(govConfigFilePath);

		MobileDevice objMobileDevice = new MobileDevice(mobileConfigFilePath, objGovernment);

		String individial1Network = "1f2b::a3ds.a12d.1f13";
		String individual1Device = "abc's Android Phone";
		
		String individial2Network = "1f2b::a3ds.a12d.1f14";
		String individual2Device = "abc's Android Phone";
		
		String individial3Network = "1f2b::a3ds.a12d.1f15";
		String individual3Device = "abc's Android Phone";
		
		String individial4Network = "1f2b::a3ds.a12d.1f16";
		String individual4Device = "abc's Android Phone";
		
		String individual1 = individial1Network + individual1Device;
		String individual2 = individial2Network + individual2Device;
		String individual3 = individial3Network + individual3Device;
		String individual4 = individial4Network + individual4Device;

		int date = 225;
		int duration = 500;

		objMobileDevice.recordContact(individual1, date, duration);
		objMobileDevice.recordContact(individual2, date, duration);
		objMobileDevice.recordContact(individual3, date, duration);
		objMobileDevice.recordContact(individual4, date, duration);

		String testHashCode = "867sdf7gj378asf";
		objMobileDevice.positiveTest(testHashCode);

		objMobileDevice.synchronizeData();

		int gatheringDate = 225;
		int minSize = 5;
		int minTime = 10;
		float density = 5;
		int numOfGathering = objGovernment.findGatherings(gatheringDate, minSize, minTime, density);
		System.out.println(numOfGathering);
	}
}