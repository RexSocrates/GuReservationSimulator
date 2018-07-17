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
	// consider the UE ID is its IMEI
    private int ueID;
    private OnlineChargingSystem OCS;
    // store the remaining GU in the device
    private double currentGU;
    // record the number of signals that the device produce
    private double producedSignals = 0;
    // record the current time
    private double currentTimePeriod = 1;
    // count the sessions include completed and failed sessions
    private double numberOfSessions = 0;
    // record the times that session fails
    private int sessionFailedTimes = 0;
    
    // store a single period of allocated GUs
    ArrayList<Double> allocatedGUs;
    
    // store multiple periods' allocated GUs
    ArrayList<SinglePeriodAllocatedGUs> periodAllocatedRecords;
    
    // represent the reservation with acronym
    String reservationScheme = "";
    // if the reservation need each UE to report their current status, then this variable is true, otherwise it's false
    boolean reportUeStatus;
    
    // IRS variables
    // total demand per month, Unit : MB 
    double totalDemand;
    // periodical data usage
    double periodicalDataUsage;
    // charging periods, unit : hour
    double chargingPeriods;
    // data collection time, unit : hour
    double dataCollectionPeriod;
    // report interval
    double reportInterval;
    
    // constructor for FS or MS
    public UserEquipment(int ID, OnlineChargingSystem OCS, String reservationScheme) {
        this.ueID = ID;
        this.OCS = OCS;
        this.currentGU = 0;
        this.allocatedGUs = new ArrayList<Double>();
        this.periodAllocatedRecords = new ArrayList<SinglePeriodAllocatedGUs>();
        this.reservationScheme = reservationScheme;
        this.reportUeStatus = false;
        this.currentTimePeriod = 1;
    }
    
    // constructor for IRS
    public UserEquipment(int ID, OnlineChargingSystem OCS, double chargingPeriods, double dataCollectionPeriod, double reportInterval, String reservationScheme) {
    	this.ueID = ID;
        this.OCS = OCS;
        this.currentGU = 0;
        this.allocatedGUs = new ArrayList<Double>();
        this.periodAllocatedRecords = new ArrayList<SinglePeriodAllocatedGUs>();
        this.reservationScheme = reservationScheme;
        this.reportUeStatus = true;
        this.currentTimePeriod = 1;
    	
    	// change days to hours
    	this.chargingPeriods = chargingPeriods * 24;
    	this.dataCollectionPeriod = dataCollectionPeriod;
    	this.reportInterval = reportInterval;
    }

    // getter and setter
    public double getCurrentGU() {
        return this.currentGU;
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
    
    public int getSessionFailedTimes() {
    	return this.sessionFailedTimes;
    }
    
    public void setSessionFailedTimes(int sessionFailedTimes) {
    	this.sessionFailedTimes = sessionFailedTimes;
    }
    
    public double getSuccessfulRate() {
    	double successfulTimes = this.numberOfSessions - this.sessionFailedTimes;
    	return successfulTimes / this.numberOfSessions;
    }
    
    // compute IRS variables
    // compute periodical data usage rate
    public double computePeriodicalDataUsage() {
    	// formula : current total data usage / current time periods
    	
    	double totalGuConsumption = this.computeTotalGuConsumption();
    	
//    	System.out.printf("Total GU consumption : %f\n", totalGuConsumption);
//    	System.out.printf("Current time period : %f\n", this.currentTimePeriod);
    	
    	// subtract the GU that haven't been used
    	if(totalGuConsumption - this.currentGU >= 0) {
    		totalGuConsumption = totalGuConsumption - this.currentGU;
    	}
    	
    	return totalGuConsumption / this.currentTimePeriod;
    }
    
    // compute total GU consumption
    public double computeTotalGuConsumption() {
    	// get current total GU consumption
    	double totalGuConsumption = 0;
    	// compute previous periods' data usage
    	for(int i = 0; i < this.periodAllocatedRecords.size(); i++) {
    		SinglePeriodAllocatedGUs record = this.periodAllocatedRecords.get(i);
    		
    		totalGuConsumption += record.getSumOfGUs();
    	}
    	
    	// compute the GU consumption of current period
    	for(int i = 0; i < this.allocatedGUs.size(); i++) {
    		totalGuConsumption += this.allocatedGUs.get(i);
    	}
    	
    	return totalGuConsumption;
    }
    
    // compute estimated total demand in a charging period
    public double computeTotalDemand() {
    	// formula : data usage (current total data usage / current time periods) * charging periods
    	return this.computePeriodicalDataUsage() * this.chargingPeriods;
    }
    
    // Functions
    
    // return current status, including remaining GU of UE and the average data rate
    public Hashtable reportCurrentStatus() {
    	Hashtable<String, Double> hashtable = new Hashtable<String, Double>();
    	
    	double periodicalDataUsage = this.computePeriodicalDataUsage();
    	
//    	System.out.printf("UE ID : %d\n", this.ueID);
//    	System.out.printf("Periodical data usage : %f\n", periodicalDataUsage);
//    	System.out.printf("Remaining GU : %f\n", this.currentGU);
    	
    	// add the content of current status report
    	hashtable.put("ueID", (double)this.ueID);
    	hashtable.put("avgDataRate", periodicalDataUsage);
    	hashtable.put("remainingGU", this.currentGU);
    	
//    	System.out.println("Periodical data usage : " + periodicalDataUsage);
//    	System.out.println("Remaining GU : " + this.getCurrentGU());
//    	System.out.println("==============================");
    	
    	// add the time period to tell the OCS that the current time
    	hashtable.put("timePeriod", this.currentTimePeriod);
    	
    	// add the number of signals used by one report
    	System.out.printf("Report current status, UE ID : %d\n", this.ueID);
    	this.producedSignals += 1;
    	
    	this.OCS.receiveCurrentStatusReport(hashtable);
    	
    	return hashtable;
    }
    
    // to complete a session, giving a granted unit that a session needs and the time that the session created
    public void completeSession(double sessionTotalGU, double timePeriod) {
    	System.out.println("UE ID : " + this.ueID);
    	System.out.println("Current GU : " + this.getCurrentGU());
    	System.out.println("=====================================");
    	
    	this.currentTimePeriod = timePeriod;
        
        boolean dataAllowanceNotEnough = this.sendOnlineChargingRequestSessionStart();
        
        if(dataAllowanceNotEnough) {
        	// the remaining data allowance is not enough, session ends
        	this.sendOnlineChargingRequestSessionEnd();
        }
        else {
        	// the remaining data allowance is enough, session continues
        	this.consumeGU(sessionTotalGU);
            this.sendOnlineChargingRequestSessionEnd();
        }
        
        
        
        // add those allocated GUs into a single record
        this.periodAllocatedRecords.add(new SinglePeriodAllocatedGUs(timePeriod, this.allocatedGUs));
        
        // initialize the allocated GUs after the record of previous period is stored
        this.allocatedGUs = new ArrayList<Double>();
    }
    
    
    // session start, requesting GU
    public boolean sendOnlineChargingRequestSessionStart() {
    	// session counter += 1
        this.numberOfSessions += 1;
    	
//        System.out.println("sendOnlineChargingRequestSessionStart");
        
        // call next function, the parameter is a signals counter, it will return the number of signals
        Hashtable hashtable = this.OCS.receiveOnlineChargingRequestSessionStart(this.ueID, 1);
        
        boolean dataAllowanceNotEnough = false;
        if(hashtable.containsKey("dataAllowanceNotEnough")) {
        	// if the key is contained in the hash table, then the remaining data allowance is not enough
        	dataAllowanceNotEnough = true;
        	this.sessionFailedTimes += 1;
        }
        
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
        
        return dataAllowanceNotEnough;
    }
    
    // consuming granted unit
    public void consumeGU(double consumedGU) {
        
        if(this.getCurrentGU() > consumedGU) {
            // current GU is positive and enough for this activity
            this.setCurrentGU(this.getCurrentGU() - consumedGU);
            System.out.printf("Enough Current device remaining GU : %5.1f\n", this.getCurrentGU());
        }else {
            // trigger online charging request to ask for new GU, since current GU is not enough
        	System.out.printf("Not Enough Current device remaining GU : %5.1f\n", this.getCurrentGU());
        	
            int reservationCount = 0;
            boolean dataAllowanceNotEnough = false;
            do {
                // to continue session
                dataAllowanceNotEnough = sendOnlineChargingRequestSessionContinue(reservationCount++);
                
                if(dataAllowanceNotEnough) {
                	// if the remaining data allowance is not enough, then break the loop
                	break;
                }
            }while(this.getCurrentGU() < consumedGU);
            
            if(dataAllowanceNotEnough) {
            	// if the remaining data allowance is not enough, then the session is failed
            	this.sessionFailedTimes += 1;
            }else {
            	// if the remaining data  allowance is enough, then we can consume GU
            	this.setCurrentGU(this.getCurrentGU() - consumedGU);
            }
        }
    }
    
    // session continue, requesting GU
    public boolean sendOnlineChargingRequestSessionContinue(double reservationCount) {
//    	System.out.println("sendOnlineChargingRequestSessionContinue");
        
        // send the online charging request, so the initial number of signals is 1
        Hashtable<String, Double> hashtable = this.OCS.receiveOnlineChargingRequestSessionContinue(this.ueID, 1, reservationCount);
        
        boolean dataAllowanceNotEnough = false;
        if(hashtable.containsKey("dataAllowanceNotEnough")) {
        	// the remaining data allowance is not enough
        	dataAllowanceNotEnough = true;
        }else {
        	
        }
        
        double reservedGU = (double) hashtable.get("reservedGU");
        
        this.setCurrentGU(this.getCurrentGU() + reservedGU);
        this.allocatedGUs.add(reservedGU);
        
        double numOfSignals = hashtable.get("numOfSignals");
        System.out.printf("Number of signals : %3.0f\n", numOfSignals);
        
        // add the number of signals to the variable produced signals
        this.setProducedSignals(this.getProducedSignals() + numOfSignals);
        
        return dataAllowanceNotEnough;
    }
    
    // session end
    public void sendOnlineChargingRequestSessionEnd() {
//    	System.out.println("sendOnlineChargingRequestSessionEnd");
    	
        // send the online charging request, so the initial number of signals is 1
        Hashtable<String, Double> hashtable = this.OCS.receiveOnlineChargingRequestSessionEnd(this.ueID, 1);
        
        // add number of signals
        double numOfSignals = hashtable.get("numOfSignals");
        this.setProducedSignals(this.getProducedSignals() + numOfSignals);
    }
}
