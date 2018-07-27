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
    
    public OnlineChargingFunctionFixedScheme(double defaultGu, double chargingPeriods) {
    	super(defaultGu, chargingPeriods, "FS");
    }

    @Override
    public double determineGU(Hashtable hashtable) {
    	double remainingDataAllowance = (double)hashtable.get("remainingDataAllowance");
    	
    	double reservedGU = this.defaultGU;
    	if(reservedGU > remainingDataAllowance) {
    		reservedGU = remainingDataAllowance;
    		hashtable.put("dataAllowanceNotEnough", 1);
    	}
    	
        return reservedGU;
    }
}
