import java.util.Hashtable;
import java.util.Set;

public class OnlineChargingFunctionInventoryBasedReservationScheme extends OnlineChargingFunctionReservationScheme {
	// Variables of Q model for regular IoT devices
	
	// the fixed number of signals per periodical report needs
	double R;
	// the fixed number of signals of each order
	double S;
	// charging periods (hours)
//	double chargingPeriods;
	
	
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
		super(defaultGu, chargingPeriods, "IRS");
		this.R = signalsPerReport;
		this.S = signalsPerOrder;
		// change the number of days to the number of hours
//		this.chargingPeriods = chargingPeriods * 24;
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
	public void receiveStatusReport(int ueID, double avgDataUsage, double remainingGU, double totalDemand, double currentTimePeriod) {
		// update the list of average data usage and estimated total demand
		double optimalGU = this.getOptimalGU(totalDemand, avgDataUsage);
		
		// store estimated total demand
		this.estimatedTotalDemandHashtable.put(ueID, totalDemand);
		// store average data usage
		this.dataUsageHashtable.put(ueID, avgDataUsage);
		// store remaining GU
		this.remainingGUsHashtable.put(ueID, remainingGU);
		// store optimal GU
		this.optimalGUsHashtable.put(ueID, optimalGU);
		// update latest reporting time
		this.reportingTime.put(ueID, currentTimePeriod);
		
		/*
		System.out.println("Put Total demand : " + totalDemand);
		System.out.println("Put Periodical data usage : " + avgDataUsage);
		System.out.println("Put Optimal GU : " + optimalGU);
		System.out.println("Put Remaining GU : " + remainingGU);
		System.out.println("Put Reporting time : " + currentTimePeriod);
		System.out.println("Charging periods : " + this.chargingPeriods);
		*/
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
	
	// compute valid time, the time that the GU in the device is exhausted
	public double computeValidTime(int ueID) {
		// It's wrong formula ? : D(estimated total demand) / Q (optimal size of GU) * the length of each period
		double totalDemand = 0;
		double optimalGU = this.defaultGU;
		
		if(this.estimatedTotalDemandHashtable.containsKey(ueID) && this.optimalGUsHashtable.containsKey(ueID)) {
			totalDemand = (double)this.estimatedTotalDemandHashtable.get(ueID);
			optimalGU = (double)this.optimalGUsHashtable.get(ueID);
		}
		
		double validTime = totalDemand / optimalGU;
		
		// this is the variable to define the length of each period, we define it as an hour
		double theLengthOfPeriod = 1;
		
		
		return validTime;
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
		
		// get the remaining GU of the device
		double remainingGU = 0;
		// check if the remaining GU is in the hash table
		if(this.remainingGUsHashtable.containsKey(ueID)) {
			remainingGU = (double)this.remainingGUsHashtable.get(ueID);
			System.out.printf("Remaining GU : %f in device UE ID : %d\n", remainingGU, ueID);
		}
		
		// compute the EGU
		if(this.getCompleteCycleExpectedGU(ueID) - remainingGU >= 0) {
			egu = this.getCompleteCycleExpectedGU(ueID) - remainingGU;
		}
		
		// put the value in hash table
		this.EGUsHashtable.put(ueID, egu);
		
		return egu;
	}
	
	// get sum of EGU
	public double getSumOfEGUs() {
		double sumOfEGUs = 0;
		
		// get the IDs in hash table
		int[] ueIDs = this.getKeys();
//		System.out.println("Get Sum of EGUs length : " + ueIDs.length);
		
		for(int i = 0; i < ueIDs.length; i++) {
			int ueID = ueIDs[i];
			double ueIdEGU = this.getEgu(ueID);
			sumOfEGUs += ueIdEGU;
//			System.out.printf("UE ID : %d\nEGU : %f\n", ueID, ueIdEGU);
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
		System.out.println("Sum of EGUs : " + sumOfEGUs);
		
		double ueIdEGU = this.defaultGU;
		if(this.EGUsHashtable.contains(ueID)) {
			ueIdEGU = (double)this.EGUsHashtable.get(ueID);
		}
		
		double insufficientGU = ueIdEGU / sumOfEGUs * remainingDataAllowance;
		
		return insufficientGU;
	}


	@Override
	public double determineGU(Hashtable hashtable) {
		int ueID = ((Double)hashtable.get("UEID")).intValue();
		
		double sumOfEGUs = this.getSumOfEGUs();
		double remainingDataAllowance = (double)hashtable.get("remainingDataAllowance");
		
		double reservedGU = this.defaultGU;
		// if the sum of EGUs <= remaining data allowance, then allocate the optimal granted unit for the device
		if(sumOfEGUs <= remainingDataAllowance) {
			if(this.optimalGUsHashtable.containsKey(ueID)) {
				reservedGU = (double)this.optimalGUsHashtable.get(ueID);				
			}
		}else {
			reservedGU = this.getSurplusGu(ueID, remainingDataAllowance);
		}
		
		return reservedGU;
	}

}
