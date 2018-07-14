import java.util.Hashtable;

public class OnlineChargingFunctionReservationScheme implements ReservationScheme {
	double defaultGU;
	String reservationScheme;
	
	public OnlineChargingFunctionReservationScheme(double defaultGU, String reservationScheme) {
		this.defaultGU = defaultGU;
		this.reservationScheme = reservationScheme;
	}

	@Override
	public double determineGU(Hashtable hashtable) {
		// TODO Auto-generated method stub
		return this.defaultGU;
	}
	
	public double getSurplusGu(double remainingDataAllowance) {
		return remainingDataAllowance;
	}
	
}
