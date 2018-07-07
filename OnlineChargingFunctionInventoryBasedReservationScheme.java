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
	
	public OnlineChargingFunctionInventoryBasedReservationScheme(double defaultGu, double signalsPerReport, double signalsPerOrder) {
		this.R = signalsPerReport;
		this.S = signalsPerOrder;
		this.defaultGU = defaultGu;
	}
	
	// compute the optimal size of granted unit for each user equipment
	public double getOptimalGU(double totalDemand, double periodicalDataUsage) {
		return Math.sqrt(totalDemand * this.S * periodicalDataUsage / this.R);
	}

	@Override
	public double determineGU(Hashtable hashtable) {
		// TODO Auto-generated method stub
		return this.defaultGU;
	}

}
