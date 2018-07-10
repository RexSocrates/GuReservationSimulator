import java.util.Hashtable;

public class OnlineChargingFunctionInventoryBasedReservationScheme extends OnlineChargingFunctionReservationScheme {
	// Variables of Q model for regular IoT devices
	
	// total demand per month D list ?
//	double D;
	// the granted unit that SBCF always reserves Q list ?
//	double Q;
	// the data size of periodical data usage W list ?
//	double W;
	
	// the fixed number of signals per periodical report needs
	double R;
	// the fixed number of signals of each order
	double S;
	
	
	// UE status report variables
	// declare a hash table to record the average data usage for each user equipment
	Hashtable<Integer, Double> avgDataUsages;
	// charging periods
	double chargingPeriods;
	
	public OnlineChargingFunctionInventoryBasedReservationScheme(double defaultGu, double chargingPeriods, double signalsPerReport, double signalsPerOrder) {
		super(defaultGu, "IRS");
		this.R = signalsPerReport;
		this.S = signalsPerOrder;
		this.chargingPeriods = chargingPeriods;
		this.defaultGU = defaultGu;
		
		// initialize the optimal GUs hash table, the key is UE ID and the value is the average data usage of each UE
		this.avgDataUsages = new Hashtable<Integer, Double>();
	}
	
	// compute the optimal size of granted unit for each user equipment
	public double getOptimalGU(double totalDemandPerMonth, double periodicalDataUsage) {
		return Math.sqrt(totalDemandPerMonth * this.S * periodicalDataUsage / this.R);
	}
	
	// update optimal GU(Q) for each user equipment
	public void receiveStatusReport(int ueID, double totalDemandPerMonth) {
		// update the list of average data usage
	}
	
	// calculating EGU

	@Override
	public double determineGU(Hashtable hashtable) {
		return this.defaultGU;
	}

}
