/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.Hashtable;

/**
 *
 * @author Socrates
 */
public class OnlineChargingFunctionMultiplicativeScheme extends OnlineChargingFunctionReservationScheme {
    private double allocatedGuIncreasingFactor = 1;
    
    public OnlineChargingFunctionMultiplicativeScheme(double defaultGU, double allocatedGuIncreasingFactor, double chargingPeriods) {
        super(defaultGU, chargingPeriods, "MS");
        this.allocatedGuIncreasingFactor = allocatedGuIncreasingFactor;
    }

    public double getDefaultGU() {
        return defaultGU;
    }

    public double getAllocatedGuIncreasingFactor() {
        return allocatedGuIncreasingFactor;
    }

    @Override
    public double determineGU(Hashtable hashtable) {
        // j is the number of reservations run by UE
        double consecutiveReservationTimes = 1;
        if(hashtable.containsKey("reservationCount")) {
            consecutiveReservationTimes = (double)hashtable.get("reservationCount");
        }
        System.out.printf("J : %5.0f\n", j);
        double reservedGU = Math.ceil(consecutiveReservationTimes / this.allocatedGuIncreasingFactor) * this.getDefaultGU();
        
        double remainingDataAllowance = (double)hashtable.get("remainingDataAllowance");
        
        if(reservedGU >= remainingDataAllowance) {
        	reservedGU = remainingDataAllowance;
        	hashtable.put("dataAllowanceNotEnough", 1);
        }
        
        return reservedGU;
    }
}
