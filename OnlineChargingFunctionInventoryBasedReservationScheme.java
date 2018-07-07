import java.util.Hashtable;

public class OnlineChargingFunctionInventoryBasedReservationScheme implements ReservationScheme {
	
	
	// Variables of Q model for regular IoT devices
	
	// total demand per month D
	double D;
	// the granted unit that SBCF always reserves Q
	double defaultGU;
	// the data size of periodical data usage W
	double W;
	
	// the fixed number of signals per periodical report needs
	double R;
	// the fixed number of signals of each order
	double S;
	
	// declare a hash table to record the optimal GUs for each user equipment
	Hashtable<Integer, Double> optimalGUs;
	
	public OnlineChargingFunctionInventoryBasedReservationScheme(double defaultGu, double signalsPerReport, double signalsPerOrder) {
		this.R = signalsPerReport;
		this.S = signalsPerOrder;
		this.defaultGU = defaultGu;
		
		// initialize the optimal GUs hash table, the key is UE ID and the value is the average data usage of each UE
		this.optimalGUs = new Hashtable<Integer, Double>();
	}
	
	// compute the optimal size of granted unit for each user equipment
	public double getOptimalGU(double totalDemandPerMonth, double periodicalDataUsage) {
		return Math.sqrt(totalDemandPerMonth * this.S * periodicalDataUsage / this.R);
	}
	
	// update optimal GU for each user equipment
	public void updateOptimalGUs(int ueID, double totalDemandPerMonth) {
		
	}

	@Override
	public double determineGU(Hashtable hashtable) {
		return this.defaultGU;
	}

}
