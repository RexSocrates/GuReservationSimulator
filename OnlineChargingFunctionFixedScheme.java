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
public class OnlineChargingFunctionFixedScheme extends OnlineChargingFunctionReservationScheme {
    
    public OnlineChargingFunctionFixedScheme(double defaultGu) {
    	super(defaultGu, "FS");
    }

    @Override
    public double determineGU(Hashtable hashtable) {
        return this.defaultGU;
    }
}
