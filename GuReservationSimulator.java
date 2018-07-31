/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

/**
 *
 * @author Socrates
 */
public class GuReservationSimulator {
    static Scanner input = new Scanner(System.in);
    static ArrayList<UserEquipment> UeArr = new ArrayList<UserEquipment>();
    static OnlineChargingSystem OCS;
    static double defaultGU = 0;
    static double chargingPeriods = 7;
    static double reportInterval = 1;
    static double dataCollectionPeriods = 1;
    static int[] cellIDs;

    /**
     * @param args the command line arguments
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException {
        // TODO code application logic here
    	
        int numOfDevices = 3;
//        System.out.print("Enter the number of devices : ");
//        numOfDevices = input.nextInt();
//        System.out.println("");
        cellIDs = new int[numOfDevices];
        
        // print reservation scheme options
        String[] reservationSchemes = {
        		"Fixed scheme",
        		"Multiplicative scheme",
        		"Inventory-based Reservation Scheme"
        };
        
        for(int i = 0; i < reservationSchemes.length; i++) {
        	System.out.printf("%2d . %s\n", i+1, reservationSchemes[i]);
        }
        System.out.print("Choose the reservation scheme : ");
        int option = input.nextInt();
        System.out.println("");
        
        // configure the experiment
//        System.out.print("Enter the monthly data allowance(GB) : ");
//        double totalDataAllowance = input.nextDouble();
//        System.out.println("");
        // total data allowance = 130(population of a cell) * 9(average data allowance of each person)
        double totalDataAllowance = 130 * 9 * numOfDevices;
        
        switch(option) {
            case 1 : OCS = fixedScheme(totalDataAllowance);
                break;
            case 2 : OCS = multiplicativeScheme(totalDataAllowance);
                break;
            case 3 : OCS = inventoryBasedReservationScheme(totalDataAllowance);
            	break;
        }
        
        // add the user equipments into the array
        initializeUserEquipments(numOfDevices, option);
        readTotalUsageFile();
        
        // stimulate that there are lots of sessions should be completed
//        System.out.print("Enter the random GU range ( > 0) : ");
//        double randomRange = input.nextDouble();
        
        
        // stimulate that time is moving
//        int deviceCount = 0;
        double timePeriod = 0;
        reportCurrentStatus(timePeriod++);
//        int loopCount = 0;
        while(chargingProcessContinue(OCS.getRemainingDataAllowance(), timePeriod)) {
        	
        	for(int i = 0; i < UeArr.size(); i++) {
        		UserEquipment ue = UeArr.get(i);
        		DailyUsage ueDailyUsage = ue.getDailyUsage();
        		int intTime = (new Double(timePeriod)).intValue();
        		double consumedGU = ueDailyUsage.getHourlyUsage(intTime);
        		
        		System.out.println("UE ID : " + ue.getUeID());
        		System.out.println("Consumed GU : " + consumedGU);
        		
        		ue.completeSession(consumedGU, timePeriod);
        	}
        	
//            double randomConsumedGU = Math.random() * randomRange * defaultGU;
//            System.out.printf("Random GU : %5.2f\n", randomConsumedGU);
            
//            UserEquipment ue = UeArr.get(deviceCount);
//            deviceCount = (deviceCount + 1) % UeArr.size();
            
//            ue.completeSession(randomConsumedGU, timePeriod);
            
            // randomly determine that the time move
//            if(Math.random() * 10 >= 5) {
//            	System.out.println("Time counter : " + timePeriod++);
//            }
            
            // report current status once every report interval
        	if(timePeriod % reportInterval == 0) {
        		reportCurrentStatus(timePeriod);
        	}
            
            System.out.printf("Remaining data allowance : %10.2f\n", OCS.getRemainingDataAllowance());
            
            timePeriod += 1;
            
//            if(timePeriod >= 50) {
//            	System.out.println("Time period exceed 50");
//            	break;
//            }
        }
        
        
        
        // get total signals of this operation
        countTotalSignals(UeArr);
        
        // print experiment configuration
        System.out.printf("Number of devices : %d\n", numOfDevices);
        System.out.printf("Reservation scheme : %s\n", reservationSchemes[option - 1]);
        System.out.printf("Monthly data allowance : %3.0f\n", totalDataAllowance);
//        System.out.printf("Consumed GU random range : %3.0f\n", randomRange);
        System.out.printf("Default GU : %5.0f\n", defaultGU);
    }

	private static void initializeUserEquipments(int numOfDevices, int option) throws FileNotFoundException {
    	// randomly select the user equipment
    	int[] cellIDs = new int[numOfDevices];
    	for(int i = 0; i < numOfDevices; i++) {
    		int cellID = (int)Math.random() * 10000;
    		
    		// check if the cell ID is in the array
    		boolean cellIdInTheList = false;
    		for(int j = 0; j < i; j++) {
    			if(cellIDs[i] == cellIDs[j]) {
    				cellIdInTheList = true;
    			}
    		}
    		
    		if(cellIdInTheList) {
    			i--;
    		}else {
    			cellIDs[i] = cellID;
    		}
    	}
    	
    	Arrays.sort(cellIDs);
    	
    	dataCollectionPeriods = 0;
    	reportInterval = 0;
    	
    	double[] totalDemands = new double[numOfDevices];
    	double[] dataUsages = new double[numOfDevices];
    	
    	if(option == 3) {
    		// enter some variable that IRS needs
    		System.out.print("Enter data collection periods(hour) : ");
        	dataCollectionPeriods = input.nextDouble();
        	System.out.println("");
        	
        	System.out.print("Enter report interval(hour) : ");
        	reportInterval = input.nextDouble();
        	System.out.println("");
        	
        	Hashtable<String, double[]> dataRateAndTotalUsage = getPeriodicalDataUsageAndTotalUsage(numOfDevices, cellIDs);
        	
        	totalDemands = (double[]) dataRateAndTotalUsage.get("totalUsage");
        	dataUsages = (double[]) dataRateAndTotalUsage.get("dataRate");
    	}
    	
    	
    	
		for(int i = 0; i < cellIDs.length; i++) {
			int cellID = cellIDs[i];
			
			if(option == 1 || option == 2) {
				// fixed scheme
				UeArr.add(new UserEquipment(cellID, OCS, "FS"));
			}else if(option == 2) {
				// multiplicative scheme
				UeArr.add(new UserEquipment(cellID, OCS, "MS"));
			}else if(option == 3) {
				// Inventory-based reservation scheme
				UeArr.add(new UserEquipment(cellID, OCS, chargingPeriods, dataCollectionPeriods, reportInterval, totalDemands[i], dataUsages[i], "IRS"));
			}
		}
	}

	// File IO Functions
	// read total usage from the file
	private static void readTotalUsageFile() throws FileNotFoundException {
		String fileName = "sevenDaysRecords.csv";
		File file = new File(fileName);
		Scanner inputFile = new Scanner(file);
		
		// remove title
		inputFile.nextLine();
		
		while(inputFile.hasNext()) {
			String tuple = inputFile.nextLine();
			String[] tupleData = tuple.split(",");
			int cellID = Integer.parseInt(tupleData[0]);
			double time = Double.parseDouble(tupleData[1]);
			double totalUsage = Double.parseDouble(tupleData[2]);
			
			for(int i = 0; i < UeArr.size(); i++) {
				UserEquipment ue = UeArr.get(i);
				
				if(cellID == ue.getUeID()) {
					DailyUsage ueDailyUsage = ue.getDailyUsage();
					int intTime = (new Double(time)).intValue();
					ueDailyUsage.addHourlyUsage(intTime, totalUsage);
					break;
				}
			}
		}
		
		inputFile.close();
	}
	
	private static Hashtable<String, double[]> getPeriodicalDataUsageAndTotalUsage(int numberOfDevices, int[] cellIDs) throws FileNotFoundException {
		Hashtable<String, double[]> dataRateAndTotalUsage = new Hashtable<String, double[]>();
		double[] dataRate = new double[numberOfDevices];
		double[] totalUsage = new double[numberOfDevices];
		
		readDataRateFile(cellIDs, dataRate, totalUsage);
		
		dataRateAndTotalUsage.put("dataRate", dataRate);
		dataRateAndTotalUsage.put("totalUsage", totalUsage);
		
		return dataRateAndTotalUsage;
	}
	
	// read periodical data rate and total usage
	private static void readDataRateFile(int[] cellIDs, double[] dataRate, double[] totalUsageArr) throws FileNotFoundException {
		String fileName = "accumulatedTotalUsage.csv";
		
		File file = new File(fileName);
		Scanner inputFile = new Scanner(file);
		
		// remove title
		inputFile.nextLine();
		
		int cellIdIndex = 0;
		
		while(inputFile.hasNext()) {
			String singleTuple = inputFile.nextLine();
			
			// split a tuple
			String[] tupleData = singleTuple.split(",");
			int cellID = Integer.parseInt(tupleData[0]);
			double time = Double.parseDouble(tupleData[1]);
			double periodicalDataUsage = Double.parseDouble(tupleData[2]);
			double totalUsage = Double.parseDouble(tupleData[3]);
			
			if(cellID == cellIDs[cellIdIndex] && time == dataCollectionPeriods) {
				dataRate[cellIdIndex] = periodicalDataUsage;
				totalUsageArr[cellIdIndex] = totalUsage;
				cellIdIndex += 1;
			}
			
			if(cellIdIndex >= cellIDs.length) {
				break;
			}
		}
		
		inputFile.close();
		
	}
	
	// Devices report current status
	private static void reportCurrentStatus(double currentTime){
		for(int i = 0; i < UeArr.size(); i++) {
			UserEquipment ue = UeArr.get(i);
			ue.reportCurrentStatus(currentTime);
		}
	}
	
	// Count functions
	private static void countTotalSignals(ArrayList<UserEquipment> devicesArr) {
		double totalSignals = 0;
		
		for(int i = 0; i < devicesArr.size(); i++) {
			UserEquipment device = devicesArr.get(i);
			
			double signals = device.getProducedSignals();
			totalSignals += signals;
			System.out.printf("Signals : %3.0f\n", signals);
			System.out.printf("Session successful rate : %5.0f", device.getSuccessfulRate() * 100);
			System.out.println("%");
		}
		System.out.printf("Total signals : %5.0f\n", totalSignals);
		
	}
	

	// configure the reservation schemes
    private static OnlineChargingSystem fixedScheme(double totalDataAllowance) {
    	// hyper-parameters
        System.out.print("Enter the default GU(MB) for fixed scheme : ");
        defaultGU = input.nextDouble();
        defaultGU = defaultGU * 130;
        System.out.println("");
        
        
        
        // configure online charging function for fixed scheme
        OnlineChargingFunctionFixedScheme OCF = new OnlineChargingFunctionFixedScheme(defaultGU, chargingPeriods);
        // configure account balance management function
        AccountBalanceManagementFunction ABMF = new AccountBalanceManagementFunction(totalDataAllowance);
        
        // create an instance for online charging system
        OnlineChargingSystem OCS = new OnlineChargingSystem(OCF, ABMF, "FS");
        
        return OCS;
    }

    private static OnlineChargingSystem multiplicativeScheme(double totalDataAllowance) {
    	// hyper-parameters
        System.out.print("Enter default GU(MB) for multiplicative scheme : ");
        defaultGU = input.nextDouble();
        defaultGU = defaultGU * 130;
        System.out.println("");
        
        System.out.print("Enter C : ");
        double c = input.nextInt();
        System.out.println("");
        
        // configure online charging function for multiplicative scheme
        OnlineChargingFunctionMultiplicativeScheme OCF = new OnlineChargingFunctionMultiplicativeScheme(defaultGU, c, chargingPeriods);
        // configure account balance management function
        AccountBalanceManagementFunction ABMF = new AccountBalanceManagementFunction(totalDataAllowance);
        
        OnlineChargingSystem OCS = new OnlineChargingSystem(OCF, ABMF, "MS");
        
        return OCS;
        
    }
    
    private static OnlineChargingSystem inventoryBasedReservationScheme(double totalDataAllowance) {
    	// view the test data and record the total usage and periodical data usage
    	
    	
    	// hyper-parameters
//    	System.out.print("Enter the charging period(days) : ");
//    	chargingPeriods = input.nextDouble();
//    	System.out.println("");
    	
		System.out.print("Enter default GU(MB) for inventory-based reservation scheme : ");
		defaultGU = input.nextDouble();
		defaultGU = defaultGU * 130;
		System.out.println("");
		
//		System.out.print("Enter the signals of each report");
//		double signalsPerReport = input.nextDouble();
//		System.out.println("");
		
		double signalsPerReport = 1;
		
//		System.out.print("Enter the signals of each order");
//		double signalsPerOrder = input.nextDouble();
//		System.out.println("");
		
		double signalsPerOrder = 6;
		
		// configure online charging function for IRS
		OnlineChargingFunctionInventoryBasedReservationScheme OCF = new OnlineChargingFunctionInventoryBasedReservationScheme(defaultGU, chargingPeriods, signalsPerReport, signalsPerOrder);
		// configure account balance management function
		AccountBalanceManagementFunction ABMF = new AccountBalanceManagementFunction(totalDataAllowance);
    	
    	
		return new OnlineChargingSystem(OCF, ABMF, "IRS");
	}
    
    private static boolean chargingProcessContinue(double remainingDataAllowance, double timePeriod) {
    	boolean chargingProcessContinue = true;
    	
    	/*
    	if(remainingDataAllowance <= 0 && timePeriod > chargingPeriods * 24) {
    		chargingProcessContinue = false;
    	}
    	*/
    	
    	
    	if(remainingDataAllowance <= 0 && timePeriod > chargingPeriods * 24 && getSumOfRemainingGuInUEs() <= 0) {
    		chargingProcessContinue = false;
    	}
    	
    	
    	return chargingProcessContinue;
    }
    
    private static double getSumOfRemainingGuInUEs() {
    	double sumOfRemainingGU = 0;
    	
    	for(int i = 0; i < UeArr.size(); i++) {
    		UserEquipment currentUE = UeArr.get(i);
    		double remainingGU = currentUE.getCurrentGU();
    		sumOfRemainingGU += remainingGU;
    	}
    	
    	return sumOfRemainingGU;
    }
    
}
