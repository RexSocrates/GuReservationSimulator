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
	// Basic variables
    private int ueID;
    private OnlineChargingSystem OCS;
    private double currentGU;
    private double producedSignals = 0;
    
    // store a single period of allocated GUs
    ArrayList<Double> allocatedGUs;
    
    // store multiple periods' allocated GUs
    ArrayList<SinglePeriodAllocatedGUs> periodAllocatedRecords;
    
    // IRS variables
    // total demand per month, Unit : MB 
    double totalDemand;
    // periodical data usage
    double periodicalDataUsage;
    // charging periods, unit : day
    double chargingPeriods;
    // data collection time, unit : hour
    double dataCollectionPeriod;
    // cycle time, report interval
    double cycleTime;
    
    // constructor for FS or MS
    public UserEquipment(int ID, OnlineChargingSystem OCS) {
        this.ueID = ID;
        this.OCS = OCS;
        this.currentGU = 0;
        this.allocatedGUs = new ArrayList<Double>();
        this.periodAllocatedRecords = new ArrayList<SinglePeriodAllocatedGUs>();
    }
    
    // constructor for IRS
    public UserEquipment(int ID, OnlineChargingSystem OCS, double chargingPeriods, double dataCollectionPeriod, double cycleTime) {
    	this.ueID = ID;
    	this.OCS = OCS;
    	this.currentGU = 0;
    	this.allocatedGUs = new ArrayList<Double>();
        this.periodAllocatedRecords = new ArrayList<SinglePeriodAllocatedGUs>();
    	
    	// change days to hours
    	this.chargingPeriods = chargingPeriods * 24;
    	this.dataCollectionPeriod = dataCollectionPeriod;
    	this.cycleTime = cycleTime;
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
    
    // compute IRS variables
    // compute periodical data usage
    public double computePeriodicalDataUsage(double totalDemand, double periods) {
    	// formula : current total data usage / current time periods
    	
    	return totalDemand / periods;
    }
    
    // compute total demand in a charging period
    public double computeTotalDemand() {
    	// formula : current total data usage / current time periods * charging periods
    	return 0.0;
    }
    
    // Functions
    
    // return current status, including remaining GU of UE and the average data rate
    public Hashtable reportCurrentStatus() {
    	Hashtable<String, Double> hashtable = new Hashtable<String, Double>();
    	
    	// how to define average data rate ?
    	double avgDataRate = Math.random() * 10;
    	hashtable.put("avgDataRate", avgDataRate);
    	hashtable.put("remaining", this.currentGU);
    	
    	return hashtable;
    }
    
    // a completed session, giving a granted unit that a session needs
    public void completeSession(double sessionTotalGU, double timePeriod) {
        this.sendOnlineChargingRequestSessionStart();
        this.consumeGU(sessionTotalGU);
        this.sendOnlineChargingRequestSessionEnd();
        
        // add those allocated GUs into a single record
        this.periodAllocatedRecords.add(new SinglePeriodAllocatedGUs(timePeriod, this.allocatedGUs));
        
        // report current status to OCS after report interval
        if(timePeriod % this.cycleTime == 0) {
        	reportCurrentStatus();
        }
    }
    
    
    // session start, requesting GU
    public void sendOnlineChargingRequestSessionStart() {
        System.out.println("sendOnlineChargingRequestSessionStart");
        
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
        this.allocatedGUs.add(allocatedGU);
    }
    
    // consuming granted unit
    public void consumeGU(double consumedGU) {
//        Hashtable<String, Double> hashtable = new Hashtable<String, Double>();
        
        if(this.getCurrentGU() > consumedGU) {
            // current GU is positive and enough for this activity
            this.setCurrentGU(this.getCurrentGU() - consumedGU);
            System.out.printf("Enough Current device remaining GU : %5.1f\n", this.getCurrentGU());
        }else {
            // trigger online charging request to ask for new GU, since current GU is not enough
        	System.out.printf("Not Enough Current device remaining GU : %5.1f\n", this.getCurrentGU());
        	
            int reservationCount = 0;
            do {
                // to continue session
                sendOnlineChargingRequestSessionContinue(reservationCount++);
            }while(this.getCurrentGU() < consumedGU);
            
            // consume GU
            this.setCurrentGU(this.getCurrentGU() - consumedGU);
        }
    }
    
    // session continue, requesting GU
    public void sendOnlineChargingRequestSessionContinue(double reservationCount) {
    	System.out.println("sendOnlineChargingRequestSessionContinue");
        
        // send the online charging request, so the initial number of signals is 1
        Hashtable<String, Double> hashtable = this.OCS.receiveOnlineChargingRequestSessionContinue(1, reservationCount);
        
        double reservedGU = (double) hashtable.get("reservedGU");
        
        this.setCurrentGU(this.getCurrentGU() + reservedGU);
        this.allocatedGUs.add(reservedGU);
        
        double numOfSignals = hashtable.get("numOfSignals");
        System.out.printf("Number of signals : %3.0f\n", numOfSignals);
        
        // add the number of signals to the variable produced signals
        this.setProducedSignals(this.getProducedSignals() + numOfSignals);
        
//        return hashtable;
    }
    
    // session end
    public void sendOnlineChargingRequestSessionEnd() {
    	System.out.println("sendOnlineChargingRequestSessionEnd");
    	
        // send the online charging request, so the initial number of signals is 1
        Hashtable<String, Double> hashtable = this.OCS.receiveOnlineChargingRequestSessionEnd(1);
        
        // add number of signals
        double numOfSignals = hashtable.get("numOfSignals");
        this.setProducedSignals(this.getProducedSignals() + numOfSignals);
    }
    
    
    
    
}
