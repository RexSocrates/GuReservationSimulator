/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.ArrayList;
import java.util.Hashtable;



/**
 *
 * @author Socrates
 */
public class UserEquipment {
    private int ueID;
    private OnlineChargingSystem OCS;
    private double currentGU;
    private double producedSignals = 0;
    ArrayList<Double> allocatedGUs;
    
    
    public UserEquipment(int ID, OnlineChargingSystem OCS) {
        this.ueID = ID;
        this.OCS = OCS;
        this.currentGU = 0;
        this.allocatedGUs = new ArrayList<Double>();
    }

    public double getCurrentGU() {
        return currentGU;
    }

    public void setCurrentGU(double currentGU) {
        this.currentGU = currentGU;
    }

    public double getProducedSignals() {
        return producedSignals;
    }

    public void setProducedSignals(double producedSignals) {
        this.producedSignals = producedSignals;
    }
    
    // a completed session, giving a grantes unit that a session needs
    public void completeSession(double sessionTotalGU) {
        this.sendOnlineChargingRequestSessionStart();
        this.consumeGU(sessionTotalGU);
        this.sendOnlineChargingRequestSessionEnd();
    }
    
    
    // session start, requesting GU
    public void sendOnlineChargingRequestSessionStart() {
        System.out.println("sendOnlineChargingRequest");
        
        // call next function, the parameter is a signals counter, it will return the number of signals
        Hashtable hashtable = this.OCS.receiveOnlineChargingRequestSessionStart(1);
        
        // keys : numOfSignals, balance, reservedGU
        double numOfSignals = (double)hashtable.get("numOfSignals");
        
        // add the number of signals
        this.setProducedSignals(this.getProducedSignals() + numOfSignals);
//        System.out.printf("Num of signals : %5.0f\n", numOfSignals);
        
        // update granted unit
        double allocatedGU = (double) hashtable.get("reservedGU");
        this.setCurrentGU(this.getCurrentGU() + allocatedGU);
        // add the allocated GU to the list
        this.allocatedGUs.add(currentGU);
    }
    
    // consuming granted unit
    public void consumeGU(double consumedGU) {
//        Hashtable<String, Double> hashtable = new Hashtable<String, Double>();
        
        if(this.getCurrentGU() < consumedGU && this.getCurrentGU() > 0) {
            // current GU is positive and it is enough for this activity
            this.setCurrentGU(this.getCurrentGU() - consumedGU);
            System.out.printf("Enough Current device remaining GU : %5.1f\n", this.getCurrentGU());
        }else {
            // trigger online charging request to ask for new GU, since current GU is not enough
            int reservationCount = 0;
            do {
                // to continue session
                sendOnlineChargingRequestSessionContinue(reservationCount++);
            }while(this.getCurrentGU() < consumedGU);
            
            // consume GU
            this.setCurrentGU(this.getCurrentGU() - consumedGU);
            System.out.printf("Not Enough Current device remaining GU : %5.1f\n", this.getCurrentGU());
        }
    }
    
    // session continue, requesting GU
    public void sendOnlineChargingRequestSessionContinue(double reservationCount) {
        
        // send the online charging request, so the initial number of signals is 1
        Hashtable<String, Double> hashtable = this.OCS.receiveOnlineChargingRequestSessionContinue(1, reservationCount);
        
        double reservedGU = (double) hashtable.get("reservedGU");
        
        this.setCurrentGU(this.getCurrentGU() + reservedGU);
        
        double numOfSignals = hashtable.get("numOfSignals");
        System.out.printf("Number of signals : %3.0f\n", numOfSignals);
        
        // add the number of signals to the variable produced signals
        this.setProducedSignals(this.getProducedSignals() + numOfSignals);
        
//        return hashtable;
    }
    
    // session end
    public void sendOnlineChargingRequestSessionEnd() {
        // send the online charging request, so the initial number of signals is 1
        Hashtable<String, Double> hashtable = this.OCS.receiveOnlineChargingRequestSessionEnd(1);
        
        // add number of signals
        double numOfSignals = hashtable.get("numOfSignals");
        this.setProducedSignals(this.getProducedSignals() + numOfSignals);
    }
    
    
    
    
}
