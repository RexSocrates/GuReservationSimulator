import java.util.Hashtable;
import java.util.Set;

public class OnlineChargingFunctionInventoryBasedReservationScheme extends OnlineChargingFunctionReservationScheme {
	// Variables of Q model for regular IoT devices
	
	// the fixed number of signals per periodical report needs
	double R;
	// the fixed number of signals of each order
	double S;
	// charging periods
	double chargingPeriods;
	
	
	// UE status report variables
	// total demand per month D 
	Hashtable estimatedTotalDemandHashtable;
	// declare a hash table to store the average data usage for each user equipment W
	Hashtable dataUsageHashtable;
	// the granted unit that SBCF always reserves Q 
	Hashtable optimalGUsHashtable;
	
	// remaining GU in each device
	Hashtable remainingGUsHashtable;
	
	// EGU variables
	// latest reporting time
	Hashtable reportingTime;
	// valid time or cycle time
	
	// a hash table to store EGUs
	Hashtable EGUsHashtable;
	
	
	
	
	public OnlineChargingFunctionInventoryBasedReservationScheme(double defaultGu, double chargingPeriods, double signalsPerReport, double signalsPerOrder) {
		super(defaultGu, "IRS");
		this.R = signalsPerReport;
		this.S = signalsPerOrder;
		this.chargingPeriods = chargingPeriods;
		this.defaultGU = defaultGu;
		
		// initialize the hash table, the key is UE ID
		// estimated total demand = data usage * charging period
		this.estimatedTotalDemandHashtable = new Hashtable<Integer, Double>();
		// data usage
		this.dataUsageHashtable = new Hashtable<Integer, Double>();
		// optimal size of GU
		this.optimalGUsHashtable = new Hashtable<Integer, Double>();
		// remaining GU
		this.remainingGUsHashtable = new Hashtable<Integer, Double>();
		// latest reporting time
		this.reportingTime = new Hashtable<Integer, Double>();
		// EGU
		this.EGUsHashtable = new Hashtable<Integer, Double>();
	}
	
	// compute the optimal size of granted unit for each user equipment
	public double getOptimalGU(double estiTotalDemand, double periodicalDataUsage) {
		return Math.sqrt(estiTotalDemand * this.S * periodicalDataUsage / this.R);
	}
	
	// update optimal GU(Q) for each user equipment
	public void receiveStatusReport(int ueID, double avgDataUsage, double remainingGU, double currentTimePeriod) {
		// update the list of average data usage and estimated total demand
		
		double totalDemand = 0;
		double optimalGU = this.defaultGU;
		
		if(avgDataUsage > 0) {
			// calculate estimated total demand
			totalDemand = avgDataUsage * this.chargingPeriods;
			// get optimal size of GU
			optimalGU = this.getOptimalGU(totalDemand, avgDataUsage);
		}
		
//		System.out.printf("UE ID : %d\n", ueID);
//		System.out.printf("Total demand : %f\n", totalDemand);
//		System.out.printf("Periodical data usage : %f\n", avgDataUsage);
//		System.out.printf("Remaining GU : %f\n", remainingGU);
//		System.out.printf("Optimal GU : %f\n", optimalGU);
		
		
		// store estimated total demand
		this.estimatedTotalDemandHashtable.put(ueID, totalDemand);
		// store average data usage
		this.dataUsageHashtable.put(ueID, avgDataUsage);
		System.out.println("Put Periodical data usage : " + avgDataUsage);
		// store remaining GU
		this.remainingGUsHashtable.put(ueID, remainingGU);
		System.out.println("Put Remaining GU : " + remainingGU);
		// store optimal GU
		this.optimalGUsHashtable.put(ueID, optimalGU);
		// update latest reporting time
		this.reportingTime.put(ueID, currentTimePeriod);
		
	}
	
	// compute valid time
	public double computeValidTime(int ueID) {
		// formula : D(estimated total demand) / Q (optimal size of GU)
		double totalDemand = 0;
		double optimalGU = this.defaultGU;
		
		if(this.estimatedTotalDemandHashtable.containsKey(ueID) && this.optimalGUsHashtable.containsKey(ueID)) {
			totalDemand = (double)this.estimatedTotalDemandHashtable.get(ueID);
			optimalGU = (double)this.optimalGUsHashtable.get(ueID);
		}
		
		double validTime = totalDemand / optimalGU;
		
		return validTime;
	}
	
	// get latest reporting time
	public double getLatestReportingTime(int ueID) {
		// return 0 if there is no record of latest reporting time
		double latestReportingTime = 0;
		if(this.reportingTime.containsKey(ueID)) {
			latestReportingTime = (double)this.reportingTime.get(ueID);
		}
		
		return latestReportingTime;
	}
	
	// compute the expected GU for UE to complete its cycle
	public double getCompleteCycleExpectedGU(int ueID) {
		// formula : (valid time or cycle time - latest reporting time) * average data rate
		double validTime = this.computeValidTime(ueID);
		double latestReportingTime = this.getLatestReportingTime(ueID);
		double avgDataRate = 0;
		
		// check if the average data rate is in the hash table
		if(this.dataUsageHashtable.containsKey(ueID)) {
			avgDataRate = (double)this.dataUsageHashtable.get(ueID);
		}
		
		return (validTime - latestReportingTime) * avgDataRate;
	}
	
	// calculating EGU
	public double getEgu(int ueID) {
		// formula : getCompleteCycleExpectedGU() - remaining GU -> if the result is positive
		double egu = 0;
		double remainingGU = 0;
		
		// check if the data is in the hash table
		if(this.remainingGUsHashtable.containsKey(ueID)) {
//			System.out.print("Enter if ");
			remainingGU = (double)this.remainingGUsHashtable.get(ueID);
//			System.out.printf("UE ID : %d\n", ueID);
//			System.out.println("Remaining GU : " + this.remainingGUsHashtable.get(ueID));
		}
		
		
		if(this.getCompleteCycleExpectedGU(ueID) - remainingGU >= 0) {
			egu = this.getCompleteCycleExpectedGU(ueID) - remainingGU;
		}else {
			egu = 0;
		}
		
//		System.out.println("UE ID : " + ueID);
//		System.out.println("Remaining GU hash table length : " + this.remainingGUsHashtable.keySet().toArray().length);
//		System.out.println("Remaining GU : " + remainingGU);
//		System.out.println("EGU : " + egu);
//		System.out.println("==============================================");
		
		// put the value in hash table
		this.EGUsHashtable.put(ueID, egu);
		
		return egu;
	}
	
	// get sum of EGU
	public double getSumOfEGUs() {
		double sumOfEGUs = 0;
		
		// get the IDs in hash table
		int[] ueIDs = this.getKeys();
		
		for(int i = 0; i < ueIDs.length; i++) {
			int ueID = ueIDs[i];
			sumOfEGUs += this.getEgu(ueID);
		}
		
		return sumOfEGUs;
	}
	
	// get all the keys in the hash table, to calculate the sum of EGU
	public int[] getKeys() {
		// get the set of the keys in hash table
		Object[] keys = this.reportingTime.keySet().toArray();
		
		// declare an array to store those keys
		int[] IDs = new int[keys.length];
		
		// change type to integer
		for(int i = 0; i < keys.length; i++) {
			IDs[i] = (int)keys[i];
		}
		
		return IDs;
	}
	
	// get GU for the device when the remaining data allowance is not enough
	public double getSurplusGu(int ueID, double remainingDataAllowance) {
		double sumOfEGUs = this.getSumOfEGUs();
		
		// the EGU of the device whose UE ID is ueID
		double ueIdEGU = (double)this.EGUsHashtable.get(ueID);
		
		double insufficientGU = ueIdEGU / sumOfEGUs * remainingDataAllowance;
		
		System.out.println("Remaining data allowance : " + remainingDataAllowance);
		System.out.println("UE ID GU " + ueIdEGU);
		System.out.println("Sum of EGU " + sumOfEGUs);
		System.out.println("insufficient EGU : " + insufficientGU);
		
		return insufficientGU;
	}


	@Override
	public double determineGU(Hashtable hashtable) {
		int ueID = ((Double)hashtable.get("UEID")).intValue();
		
		// if the UE ID was stored in the hash table, then return the optimal size of GU, otherwise return  default GU
		double reservedGU = this.defaultGU;
		if(this.optimalGUsHashtable.containsKey(ueID)) {
			System.out.println("UE ID is in the hash table");
			reservedGU = (double)this.optimalGUsHashtable.get(ueID);
		}
		
		return reservedGU;
	}

}
