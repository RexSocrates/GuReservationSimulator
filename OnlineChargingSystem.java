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
public class OnlineChargingSystem {
    private OnlineChargingFunctionReservationScheme OCF;
    private AccountBalanceManagementFunction ABMF;
    String reservationSchemeName = "";
    
    public OnlineChargingSystem(OnlineChargingFunctionReservationScheme OCF, AccountBalanceManagementFunction ABMF, String reservationSchemeName) {
        this.OCF = OCF;
        this.ABMF = ABMF;
        this.reservationSchemeName = reservationSchemeName;
    }

    public OnlineChargingFunctionReservationScheme getOCF() {
        return OCF;
    }

    public AccountBalanceManagementFunction getABMF() {
        return ABMF;
    }

    public double determineGU(Hashtable hashtable) {
        return this.OCF.determineGU(hashtable);
    }
    
    public double getRemainingDataAllowance() {
        return this.ABMF.getRemainingDataAllowance();
    }
    
    // Functions
    
    // reserve GU
    public double reserveGrantedUnit(Hashtable hashtable) {
        // the hashtable should at least containa number of signals
    	double numOfSignals = (double)hashtable.get("numOfSignals");
        
        // Debit unit request, signals + 1
        numOfSignals += 1;
//        System.out.println("Send Debit unit request");
    	
        // determine GU
        double reservedGU = this.determineGU(hashtable);
        
        // Debit unit response, signals + 1
        numOfSignals += 1;
//        System.out.println("Send Debit unit response");
        
        // update the number of signals
        hashtable.put("numOfSignals", numOfSignals);
        
        double remainingBalance = this.ABMF.getRemainingDataAllowance();
        
        if(remainingBalance >= reservedGU) {
            // remaining data allowance is enough
            this.ABMF.setRemainingDataAllowance(remainingBalance - reservedGU);
            System.out.printf("Reserved GU : %5.0f\n", reservedGU);
//            System.out.printf("Remaining data allowance : %10.2f\n", this.ABMF.getRemainingDataAllowance());
        }else {
            // remaining data allowance is not enough
            
            // set a flag to tell the device that the remaining data allowance is not enough, represented by 1
            hashtable.put("dataAllowanceNotEnough", 1);
            
            // call the function to determine GU when the remaining data allowance is not enough
            if(this.reservationSchemeName.equals("IRS")) {
            	int ueID = ((Double)hashtable.get("UEID")).intValue();
                double remainingDataAllowance = this.getRemainingDataAllowance();
                OnlineChargingFunctionInventoryBasedReservationScheme irsOCF = (OnlineChargingFunctionInventoryBasedReservationScheme)this.getOCF();
                reservedGU = irsOCF.getSurplusGu(ueID, remainingDataAllowance);
            }else {
            	double remainingDataAllowance = this.getRemainingDataAllowance();
            	reservedGU = this.getOCF().getSurplusGu(remainingDataAllowance);
            	
            	// subtract the reserved GU 
            	this.ABMF.setRemainingDataAllowance(remainingBalance - reservedGU);
            }
            
            System.out.printf("Reserve surplus GU : %5.0f\n", reservedGU);
//            System.out.printf("Remaining data allowance : %10.2f\n", this.ABMF.getRemainingDataAllowance());
        }
        
        // send online charging response to tell the UE how much granted unit it can use
        return reservedGU;
        
    }
    
    // Account control operations
    
    // receive online charging request, session start, check balance
    public Hashtable receiveOnlineChargingRequestSessionStart(int ueID, double numOfSignals) {
        // Debit unit request, signals + 1
        numOfSignals += 1;
//        System.out.println("Send Debit unit request");
        
        // Debit unit response, signals + 1
        numOfSignals += 1;
//        System.out.println("Send Debit unit response");
        
        
        // prepare the hashtable to return the value
        Hashtable<String, Double> hashtable = new Hashtable<String, Double>();
        hashtable.put("UEID", (double)ueID);
        hashtable.put("numOfSignals", numOfSignals);
        hashtable.put("balance", this.ABMF.getRemainingDataAllowance());
        
        // reserve GU
        double reservedGU = this.reserveGrantedUnit(hashtable);
        hashtable.put("reservedGU", reservedGU);
        
        // online charging response, signals + 1
        numOfSignals = (double)hashtable.get("numOfSignals") + 1;
        hashtable.put("numOfSignals", numOfSignals);
        System.out.printf("Num of signals : %5.0f\n", numOfSignals);
        
        return hashtable;
    }
    
    // receive online charging request, session continue
    public Hashtable receiveOnlineChargingRequestSessionContinue(int ueID, double numOfSignals, double reservationCount) {
        Hashtable<String, Double> hashtable = new Hashtable<String, Double>();
        hashtable.put("UEID", (double)ueID);
        hashtable.put("numOfSignals", numOfSignals);
        hashtable.put("reservationCount", reservationCount);
        
        // reserve GU
        double reservedGU = reserveGrantedUnit(hashtable);
        hashtable.put("reservedGU", reservedGU);
        
        // online charging response, signals + 1
        numOfSignals = (double)hashtable.get("numOfSignals");
        numOfSignals = numOfSignals + 1;
        hashtable.put("numOfSignals", numOfSignals);
        
        return hashtable;
    }
    
    // receive online charging request, session end
    public Hashtable receiveOnlineChargingRequestSessionEnd(int ueID, double numOfSignals) {
        Hashtable<String, Double> hashtable = new Hashtable<String, Double>();
        hashtable.put("UEID", (double)ueID);
        
        // Debit unit request, signals + 1
        numOfSignals += 1;
//        System.out.println("Send Debit unit request");
        
        // Debit unit response, signals + 1
        numOfSignals += 1;
//        System.out.println("Send Debit unit response");
        
        // online charging response, signals + 1
        numOfSignals += 1;
//        System.out.println("Send Online charging response");
        
        hashtable.put("numOfSignals", numOfSignals);
        
        return hashtable;
    }
    
    // receiving the current status of user equipments, the report includes UE ID, remaining GU and average data rate
    public void receiveCurrentStatusReport(Hashtable hashtable) {
    	// update the optimal GU when receiving the current status report of each user equipment
    	
    	// get information from the current status report
    	int ueID = 0;
    	double avgDataRate = 0;
    	double remainingGU = 0;
    	double currentTimePeriod = 1;
    	
    	if(hashtable.containsKey("ueID") && hashtable.containsKey("avgDataRate") && hashtable.containsKey("remainingGU") && hashtable.containsKey("timePeriod")) {
    		ueID = ((Double)hashtable.get("ueID")).intValue();
    		avgDataRate = (double)hashtable.get("avgDataRate");
    		remainingGU = (double)hashtable.get("remainingGU");
    		currentTimePeriod = (double)hashtable.get("timePeriod");
    	}
    	
    	// record these data in online charging function
    	if(this.reservationSchemeName.equals("IRS")) {
    		OnlineChargingFunctionInventoryBasedReservationScheme IRSOCF = (OnlineChargingFunctionInventoryBasedReservationScheme)this.getOCF();
    		
    		IRSOCF.receiveStatusReport(ueID, avgDataRate, remainingGU, currentTimePeriod);
    	}
    	
    	
    	
    }
}
