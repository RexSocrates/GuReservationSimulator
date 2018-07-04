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
public class OnlineChargingFunctionFixedScheme implements ReservationScheme {
    private double defaultGU;
    
    public OnlineChargingFunctionFixedScheme(double defaultGu) {
        this.defaultGU = defaultGu;
    }

    @Override
    public double determineGU(Hashtable hashtable) {
        return this.defaultGU;
    }
}
