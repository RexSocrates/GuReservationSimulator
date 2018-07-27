/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
    static double chargingPeriods = 1;
    static double reportInterval = 1;

    /**
     * @param args the command line arguments
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException {
        // TODO code application logic here
//    	readFile();
    	
        int numOfDevices = 3;
//        System.out.print("Enter the number of devices : ");
//        numOfDevices = input.nextInt();
//        System.out.println("");
        
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
        System.out.print("Enter the monthly data allowance(GB) : ");
        double totalDataAllowance = input.nextDouble();
        System.out.println("");
        
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
        
        // stimulate that there are lots of sessions should be completed
        System.out.print("Enter the random GU range ( > 0) : ");
        double randomRange = input.nextDouble();
        
        
        // stimulate that time is moving
        int deviceCount = 0;
        double timePeriod = 0;
        reportCurrentStatus(timePeriod++);
//        int loopCount = 0;
        while(chargingProcessContinue(OCS.getRemainingDataAllowance(), timePeriod)) {
            double randomConsumedGU = Math.random() * randomRange * defaultGU;
            System.out.printf("Random GU : %5.2f\n", randomConsumedGU);
            
            UserEquipment ue = UeArr.get(deviceCount);
            deviceCount = (deviceCount + 1) % UeArr.size();
            
            ue.completeSession(randomConsumedGU, timePeriod);
            
            // randomly determine that the time move
            if(Math.random() * 10 >= 5) {
            	System.out.println("Time counter : " + timePeriod++);
            }
            
            // report current status once every report interval
        	if(timePeriod % reportInterval == 0) {
        		reportCurrentStatus(timePeriod);
        	}
            
            System.out.printf("Remaining data allowance : %10.2f\n", OCS.getRemainingDataAllowance());
            
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
        System.out.printf("Consumed GU random range : %3.0f\n", randomRange);
        System.out.printf("Default GU : %5.0f\n", defaultGU);
    }
    
    private static void initializeUserEquipments(int numOfDevices, int option) {
    	double dataCollectionPeriods = 0;
    	reportInterval = 0;
    	
    	double[] totalDemands = new double[numOfDevices];
    	double[] dataUsages = new double[numOfDevices];
    	
    	// configure total demand
    	totalDemands[0] = 8604.985;
    	totalDemands[1] = 8650.3365;
    	totalDemands[2] = 8698.5925;
    	
    	// configure periodical data usage
    	dataUsages[0] = 358.54;
    	dataUsages[1] = 360.43;
    	dataUsages[2] = 362.44;
    	
    	if(option == 3) {
    		// enter some variable that IRS needs
    		System.out.print("Enter data collection periods(hour) : ");
        	dataCollectionPeriods = input.nextDouble();
        	System.out.println("");
        	
        	System.out.print("Enter report interval(hour) : ");
        	reportInterval = input.nextDouble();
        	System.out.println("");
    	}
    	
		for(int i = 0; i < numOfDevices; i++) {
			if(option == 1 || option == 2) {
				// fixed scheme
				UeArr.add(new UserEquipment(i, OCS, "FS"));
			}else if(option == 2) {
				// multiplicative scheme
				UeArr.add(new UserEquipment(i, OCS, "MS"));
			}else if(option == 3) {
				// Inventory-based reservation scheme
				UeArr.add(new UserEquipment(i, OCS, chargingPeriods, dataCollectionPeriods, reportInterval, totalDemands[i], dataUsages[i], "IRS"));
			}
		}
	}

	// File IO Functions
	private static void readFile() throws FileNotFoundException {
//		String[] fileNames = {
//				"2013_11_01.csv",
//				"2013_11_02.csv",
//				"2013_11_03.csv",
//				"2013_11_04.csv",
//				"2013_11_05.csv",
//				"2013_11_06.csv",
//				"2013_11_07.csv",
//		};
		
		File file = new File("2013_11_01.csv");
		Scanner inputFile = new Scanner(file);
		
		// remove title
		inputFile.nextLine();
		
		long counter = 0;
		while(inputFile.hasNextLine()) {
			String dateStr = inputFile.next();
			String record = inputFile.nextLine();
			System.out.println("Counter : " + ++counter);
//			System.out.println(record);
			
			
			// split the single tuple
			String[] data = record.split(",");
			for(int i = 0; i < data.length; i++) {
				if(i == 0) {
					System.out.printf("Time : %s\n", data[i]);
				}else if(i == 1) {
					System.out.printf("Cell ID : %s\n", data[i]);
				}else {
					System.out.printf("Data : %s\n", data[i]);
				}
			}
			
			System.out.println("");
			if(counter >= 80000) {
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
    	boolean chargingProcessContinue = false;
    	
    	if(remainingDataAllowance > 0 || timePeriod <= chargingPeriods * 24) {
    		chargingProcessContinue = true;
    	}
    	
    	return chargingProcessContinue;
    }
    
}
