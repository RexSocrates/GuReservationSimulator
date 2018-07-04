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
public class OnlineChargingFunctionMultiplicativeScheme implements ReservationScheme {
    private double defaultGU;
    private double c = 1;
    
    public OnlineChargingFunctionMultiplicativeScheme(double defaultGU, double c) {
        this.defaultGU = defaultGU;
        this.c = c;
    }

    public double getDefaultGU() {
        return defaultGU;
    }

    public double getC() {
        return c;
    }

    @Override
    public double determineGU(Hashtable hashtable) {
        // j is the number of reservations run by UE
        double j = 1;
        if(hashtable.containsKey("reservationCount")) {
            j = (double)hashtable.get("reservationCount");
        }
        
        return Math.ceil(j / this.c) * this.getDefaultGU();
    }
}
