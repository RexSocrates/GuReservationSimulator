/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;

/**
 *
 * @author Socrates
 */
public class GuReservationSimulator {
    static Scanner input = new Scanner(System.in);
    // 儲存 UE 的陣列
    static ArrayList<UserEquipment> UeArr = new ArrayList<UserEquipment>();
    // 即時計費系統物件
    static OnlineChargingSystem OCS;
    // default GU 設定值
    static double defaultGU = 0;
    // count by hours
    static double chargingPeriods = 168;
    // QRS 回報間隔時間 (單位：小時)
    static double reportingCycle = 1;
    // QRS 一個 period 的時間長度 (單位：小時)
    static double periodLength = 1;
    // 用於記錄 UE 變好的陣列
    static int[] ueIDs;

    /**
     * @param args the command line arguments
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException {
    	// 參數說明：變更 FSP 內的裝置數量
        System.out.print("Enter the number of devices : ");
        int numOfDevices = input.nextInt();
//        int numOfDevices = 7;
        System.out.println("");
        ueIDs = new int[numOfDevices];
        
        // print reservation scheme options
        String[] reservationSchemes = {
        		"Fixed scheme",
        		"Multiplicative scheme",
        		"Inventory-based Reservation Scheme"
        };
        
        for(int i = 0; i < reservationSchemes.length; i++) {
        	System.out.printf("%2d . %s\n", i+1, reservationSchemes[i]);
        }
        // 參數說明：變更 FSP 使用的預留機制 1:FS, 2:MS, 3:QRS
        System.out.print("Choose the reservation scheme : ");
        int indexOfSelectedReservationScheme = input.nextInt();
//        int indexOfSelectedReservationScheme = 3;
        System.out.println("");
        
        // configure the experiment
        double totalDataAllowance = dataAllowanceSetting(numOfDevices);
        
        switch(indexOfSelectedReservationScheme) {
            case 1 : OCS = fixedScheme(totalDataAllowance);
                break;
            case 2 : OCS = multiplicativeScheme(totalDataAllowance);
                break;
            case 3 : OCS = inventoryBasedReservationScheme(totalDataAllowance);
            	break;
        }
        
        // add the user equipments into the array
        initializeUserEquipments(numOfDevices, indexOfSelectedReservationScheme);
        readTotalUsageFile();
        
        
        // stimulate that time is moving
//        int deviceCount = 0;
        double timePeriod = 0;
        reportCurrentStatus(timePeriod++);
//        int loopCount = 0;
        
        setTotalDemandAndDataRate();
        
        while(chargingProcessContinue(OCS.getRemainingDataAllowance(), timePeriod)) {
        	if(timePeriod % reportingCycle == 0) {
        		reportCurrentStatus(timePeriod);
        	}
        	
        	System.out.println("Time period : " + timePeriod);
        	for(int i = 0; i < UeArr.size(); i++) {
        		UserEquipment ue = UeArr.get(i);
        		DailyUsage ueDailyUsage = ue.getDailyUsage();
        		int intTime = (new Double(timePeriod)).intValue();
        		double consumedGU = ueDailyUsage.getHourlyUsage(intTime);
        		
        		System.out.println("UE ID : " + ue.getUeID());
        		System.out.println("Consumed GU : " + consumedGU);
        		System.out.println("");
        		
        		ue.completeSession(consumedGU, timePeriod);
        	}
            
            System.out.printf("Remaining data allowance : %10.2f\n", OCS.getRemainingDataAllowance());
            
            timePeriod += 1;
        }
        
        
        
        // get total signals of this operation
        countTotalSignals(UeArr);
        
        // print experiment configuration
        System.out.printf("Number of devices : %d\n", numOfDevices);
        System.out.printf("Reservation scheme : %s\n", reservationSchemes[indexOfSelectedReservationScheme - 1]);
        System.out.printf("Monthly data allowance : %3.0f\n", totalDataAllowance);
        System.out.printf("Remaining data allowance : %3.0f\n", OCS.getABMF().getRemainingDataAllowance());
        System.out.println("Data collection period : " + periodLength);
        System.out.printf("Default GU : %5.0f\n", defaultGU);
        
        writeExperimentResult(numOfDevices, reservationSchemes[indexOfSelectedReservationScheme - 1], totalDataAllowance, defaultGU);
    }

	private static void initializeUserEquipments(int numOfDevices, int indexOfSelectedReservationScheme) throws FileNotFoundException {
		System.out.println("++++++++++++++++++++");
    	// randomly select the user equipment
    	int[] ueIDs = new int[numOfDevices];
//    	for(int i = 0; i < numOfDevices; i++) {
//    		int cellID = (int)(Math.random() * 10001);
//    		
//    		// check if the cell ID is in the array
//    		boolean cellIdInTheList = false;
//    		for(int j = 0; j < i; j++) {
//    			if(ueIDs[i] == ueIDs[j]) {
//    				cellIdInTheList = true;
//    			}
//    		}
//    		
//    		if(cellIdInTheList) {
//    			i--;
//    		}else {
//    			ueIDs[i] = cellID;
//    		}
//    	}
    	
    	// read IDs file, which contains the randomly selected ID
//    	File file = new File("cells/" + Integer.toString(numOfDevices) + ".txt");
//    	Scanner inputFile = new Scanner(file);
//    	
//    	int cellCount = 0;
//    	while(inputFile.hasNext()) {
//    		ueIDs[cellCount++] = inputFile.nextInt();
//    	}
//    	
//    	inputFile.close();
    	
    	// read selected UE IDs
    	String selectedIDsFile = "IDs.csv";
    	File file = new File(selectedIDsFile);
    	
    	Scanner inputFile = new Scanner(file);
    	
    	// remove title
    	inputFile.nextLine();
    	
    	for(int i = 0; i < ueIDs.length; i++) {
    		ueIDs[i] = inputFile.nextInt();
    	}
    	
    	inputFile.close();
    	
    	
    	for(int i = 0; i < ueIDs.length; i++) {
    		System.out.println("Cell ID : " + ueIDs[i]);
    	}
    	
    	Arrays.sort(ueIDs);
    	
    	periodLength = 0;
    	
    	
    	double[] totalDemands = new double[numOfDevices];
    	double[] dataUsages = new double[numOfDevices];
    	
    	if(indexOfSelectedReservationScheme == 3) {
    		// enter some variable that IRS needs
    		System.out.print("Enter data collection periods(hour 1 ~ 168) : ");
        	periodLength = input.nextDouble();
        	
        	// read period length file
    		/*
        	String periodFileName = "periods.txt";
        	File periodFile = new File(periodFileName);
        	Scanner periodFileInput = new Scanner(periodFile);
        	
        	periodLength = periodFileInput.nextDouble();
        	
        	// write period file
        	PrintWriter pw = new PrintWriter(periodFileName);
        	double newPeriodLength = periodLength + 1;
        	pw.print(newPeriodLength);
        	pw.close();
        	System.out.println("");
        	*/
        	
        	
//        	System.out.print("Enter report interval(hour) : ");
//        	reportingCycle = input.nextDouble();
        	reportingCycle = periodLength;
        	System.out.println("");
        	
        	Hashtable<String, double[]> dataRateAndTotalUsage = getPeriodicalDataUsageAndTotalUsage(numOfDevices, ueIDs);
        	
        	totalDemands = (double[]) dataRateAndTotalUsage.get("totalUsage");
        	dataUsages = (double[]) dataRateAndTotalUsage.get("dataRate");
    	}
    	
    	
    	
		for(int i = 0; i < ueIDs.length; i++) {
			int ueID = ueIDs[i];
			
			if(indexOfSelectedReservationScheme == 1 || indexOfSelectedReservationScheme == 2) {
				// fixed scheme
				UeArr.add(new UserEquipment(ueID, OCS, "FS"));
			}else if(indexOfSelectedReservationScheme == 2) {
				// multiplicative scheme
				UeArr.add(new UserEquipment(ueID, OCS, "MS"));
			}else if(indexOfSelectedReservationScheme == 3) {
				// Inventory-based reservation scheme
				UeArr.add(new UserEquipment(ueID, "Regular", OCS, chargingPeriods, periodLength, reportingCycle, totalDemands[i], dataUsages[i], "IRS"));
			}
		}
	}

	// File IO Functions
	// read quota usage in each time period from the file
	private static void readTotalUsageFile() throws FileNotFoundException {
		/*
		String fileName = "usage_01.csv";
		File file = new File(fileName);
		Scanner inputFile = new Scanner(file);
		
		// remove title
		inputFile.nextLine();
		
//		int countDevice = 0;
		
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
					
					System.out.println("==================================");
					System.out.println("Cell ID : " + cellID);
					System.out.println("Time : " + time);
					System.out.println("Total usage : " + totalUsage);
//					countDevice++;
					break;
				}
			}
			
//			if(countDevice >= UeArr.size()) {
//				break;
//			}
		}
		
		inputFile.close();
		*/
		
		for(int day = 1; day <= 7; day++) {
			String dateString = "2013_11_0" + day + "_";
			for(int hour = 0; hour <= 23; hour++) {
				String fileName = "";
				if(hour < 10) {
					fileName = dateString + "0" + hour + ".csv";
				}else {
					fileName = dateString + hour + ".csv";
				}
				
				File file = new File(fileName);
				Scanner inputFile = new Scanner(file);
				
				System.out.println("Read log file : " + fileName);
				
				// remove title
				inputFile.nextLine();
				
				while(inputFile.hasNext()) {
					String tuple = inputFile.nextLine();
					String[] tupleArr = tuple.split(",");
					
					int ueID = Integer.parseInt(tupleArr[0]);
					double internetUsage = Math.ceil(Double.parseDouble(tupleArr[1]));
					
					int time = (day - 1) * 24 + hour;
					
					// insert the Internet usage into UE
					for(int i = 0; i < UeArr.size(); i++) {
						UserEquipment ue = UeArr.get(i);
						
						if(ueID == ue.getUeID()) {
							DailyUsage ueDailyUsage = ue.getDailyUsage();
							
							ueDailyUsage.addHourlyUsage(time, internetUsage);
							
							break;
						}
					}
				}
				
				inputFile.close();
			}
		}
	}
	
	private static Hashtable<String, double[]> getPeriodicalDataUsageAndTotalUsage(int numberOfDevices, int[] ueIDs) throws FileNotFoundException {
		Hashtable<String, double[]> dataRateAndTotalUsage = new Hashtable<String, double[]>();
		double[] dataRate = new double[numberOfDevices];
		double[] totalUsage = new double[numberOfDevices];
		
		readDataRateFile(ueIDs, dataRate, totalUsage);
		
		dataRateAndTotalUsage.put("dataRate", dataRate);
		dataRateAndTotalUsage.put("totalUsage", totalUsage);
		
		return dataRateAndTotalUsage;
	}
	
	// read periodical data rate and total usage
	private static void readDataRateFile(int[] ueIDs, double[] dataRates, double[] totalUsageArr) throws FileNotFoundException {
		// periodLength
		String dataCollectionPeriodFileName = "";
		int periodLengthInt = (int)periodLength;
		if(periodLengthInt < 10) {
			dataCollectionPeriodFileName = "cycleTimeOptimalGU_0" + periodLengthInt + ".csv";
		}else {
			dataCollectionPeriodFileName = "cycleTimeOptimalGU_" + periodLengthInt + ".csv";
		}
		
		File file = new File(dataCollectionPeriodFileName);
		
		Scanner inputFile = new Scanner(file);
		
		// remove title
		inputFile.nextLine();
		
		while(inputFile.hasNext()) {
			String tuple = inputFile.nextLine();
			String[] tupleArr = tuple.split(",");
			
			int ueID = Integer.parseInt(tupleArr[0]);
			double totalInternetUsage = Double.parseDouble(tupleArr[1]);
			double dataRate = Double.parseDouble(tupleArr[2]);
			
			for(int i = 0; i < ueIDs.length; i++) {
				int currentUeID = ueIDs[i];
				
				if(currentUeID == ueID) {
					dataRates[i] = dataRate;
					totalUsageArr[i] = totalInternetUsage;
					
					break;
				}
			}
		}
		
		inputFile.close();
		
	}
	
	// Devices report current status, only dynamic devices report current status
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
	
	// set the total data allowance with normal distribution
	private static double dataAllowanceSetting(int numOfDevices) {
		/*
		// store the data allowance of data plans in an array
		double[] dataAllowanceArr = {500, 1024, 3072, 5120};
		
		int numberOfDataPlans = dataAllowanceArr.length;
		double normalDistributionLowerBound = -4;
		double normalDistributionUpperBound = 4;
		
		double intervalRange = (normalDistributionUpperBound - normalDistributionLowerBound) / numberOfDataPlans;
		
		int[] dataPlanUserCountArr = new int[numberOfDataPlans];
		Random rand = new Random();
		for(int i = 0; i < numOfDevices; i++) {
			double randNum = rand.nextGaussian();
			
			int interval = (int)(Math.floor((randNum - normalDistributionLowerBound) / intervalRange));
			dataPlanUserCountArr[interval] += 1;
		}
		
		// aggregate the total allowance
		double totalDataAllowance = 0;
		for(int i = 0; i < dataAllowanceArr.length; i++) {
			totalDataAllowance += dataAllowanceArr[i] * dataPlanUserCountArr[i];
		}
		
		// change monthly allowance to weekly allowance
		return totalDataAllowance / 4;
		*/
		
//		return Math.ceil(numOfDevices * 500 / 4);
		// 參數說明：變更 ABMF 的流量額度 (單位：MB)
		double totalDataAllowance = 500;
		return totalDataAllowance;
	}
	

	// configure the reservation schemes
    private static OnlineChargingSystem fixedScheme(double totalDataAllowance) {
    	// 參數說明：變更 FS default GU (單位：MB)
        System.out.print("Enter the default GU(MB) for fixed scheme : ");
        defaultGU = input.nextDouble();
        System.out.println("");
        
        
        
        // configure online charging function for fixed scheme
        OnlineChargingFunctionFixedScheme OCF = new OnlineChargingFunctionFixedScheme(defaultGU, chargingPeriods);
        // configure account balance management function
        AccountBalanceManagementFunction ABMF = new AccountBalanceManagementFunction(totalDataAllowance);
        
        // create an instance for online charging system
        OnlineChargingSystem OCS = new OnlineChargingSystem(UeArr, OCF, ABMF, "FS");
        
        return OCS;
    }

    private static OnlineChargingSystem multiplicativeScheme(double totalDataAllowance) {
    	// 參數說明：變更 MS default GU (單位：MB)
        System.out.print("Enter default GU(MB) for multiplicative scheme : ");
        defaultGU = input.nextDouble();
        System.out.println("");
        
        // 參數說明：用於史 MS 增加分配 GU 的參數
        System.out.print("Enter C : ");
        double allocatedGuIncreasingFactor = input.nextDouble();
        System.out.println("");
        
        // configure online charging function for multiplicative scheme
        OnlineChargingFunctionMultiplicativeScheme OCF = new OnlineChargingFunctionMultiplicativeScheme(defaultGU, allocatedGuIncreasingFactor, chargingPeriods);
        // configure account balance management function
        AccountBalanceManagementFunction ABMF = new AccountBalanceManagementFunction(totalDataAllowance);
        
        OnlineChargingSystem OCS = new OnlineChargingSystem(UeArr, OCF, ABMF, "MS");
        
        return OCS;
        
    }
    
    private static OnlineChargingSystem inventoryBasedReservationScheme(double totalDataAllowance) {
    	// view the test data and record the total usage and periodical data usage
    	
    	
    	// hyper-parameters
//    	System.out.print("Enter the charging period(days) : ");
//    	chargingPeriods = input.nextDouble();
//    	System.out.println("");
    	
//		System.out.print("Enter default GU(MB) for inventory-based reservation scheme : ");
//		defaultGU = input.nextDouble();
    	// in IRS the default GU of each device is obtained from total demand and default GU calculation
		defaultGU = 10;
//		defaultGU = defaultGU * 130;
//		System.out.println("");
		
//		System.out.print("Enter the signals of each report");
//		double signalsPerReport = input.nextDouble();
//		System.out.println("");
		
		double signalsPerReport = 1;
		
//		System.out.print("Enter the signals of each order");
//		double signalsPerOrder = input.nextDouble();
//		System.out.println("");
		
		double signalsPerOrder = 1;
		
		// configure online charging function for IRS
		OnlineChargingFunctionInventoryBasedReservationScheme OCF = new OnlineChargingFunctionInventoryBasedReservationScheme(defaultGU, chargingPeriods, signalsPerReport, signalsPerOrder);
		// configure account balance management function
		AccountBalanceManagementFunction ABMF = new AccountBalanceManagementFunction(totalDataAllowance);
    	
    	
		return new OnlineChargingSystem(UeArr, OCF, ABMF, "IRS");
	}
    
    private static boolean chargingProcessContinue(double remainingDataAllowance, double timePeriod) {
    	boolean chargingProcessContinue = true;
    	
    	/*
    	if(remainingDataAllowance <= 0 && timePeriod > chargingPeriods * 24 && getSumOfRemainingGuInUEs() <= 0) {
    		chargingProcessContinue = false;
    	}
    	*/
    	
    	if(timePeriod > chargingPeriods) {
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
    
    private static void writeExperimentResult(int numOfDevices, String reservationScheme, double totalDataAllowance, double defaultGU) throws FileNotFoundException {
    	Date date = new Date();
    	String dateStr = date.toString();
    	
    	String[] dateStrArr = dateStr.split(" ");
    	String timeStr = dateStrArr[3].replaceAll(":", "_");
    	String filename = dateStrArr[5] + dateStrArr[1] + dateStrArr[2] + "_" + timeStr;
    	
    	String logFilename = filename + ".txt";
    	
    	PrintWriter pw = new PrintWriter(logFilename);
    	
    	double totalSignals = 0;
    	
    	// print experiment configuration
    	pw.printf("Number of devices : %d\n", numOfDevices);
    	pw.printf("Reservation scheme : %s\n", reservationScheme);
    	pw.printf("Monthly data allowance : %3.0f\n", totalDataAllowance);
    	pw.printf("Default GU : %5.0f\n", defaultGU);
    	pw.println("Data collection period : " + periodLength);
    	pw.println();
		
    	double totalSucdcessfulRate = 0;
    	double totalInteractionTimes = 0;
		for(int i = 0; i < UeArr.size(); i++) {
			UserEquipment device = UeArr.get(i);
			
			double signals = device.getProducedSignals();
			double interactionTimes = device.interaction;
			totalSignals += signals;
			totalInteractionTimes += interactionTimes;
			pw.printf("UE ID : %5d ", device.getUeID());
			pw.printf("Signals : %3.0f\n", signals);
			pw.printf("Interaction times : %3.0f\n", interactionTimes);
			pw.printf("Session successful rate : %5.0f", device.getSuccessfulRate() * 100);
			pw.println("%");
			
			totalSucdcessfulRate += device.getSuccessfulRate() * 100;
		}
		pw.printf("Average successful rate : %f", (totalSucdcessfulRate / numOfDevices));
		pw.println("%");
		pw.printf("Total signals : %5.0f\n", totalSignals);
		pw.printf("Total interaction times : %5.0f\n", totalInteractionTimes);
		
		pw.close();
		
		// print short experiment result
		String shortResultName = filename + "_short.txt";
		
		PrintWriter shortPW = new PrintWriter(shortResultName);
		shortPW.printf("%f , %f , %f", totalInteractionTimes, totalSignals, (totalSucdcessfulRate / numOfDevices));
		shortPW.close();
		
	}
    
    // 傳送所有 UE 的總需求以及data rate 到 OCS
    public static void setTotalDemandAndDataRate() {
    	for(int i = 0; i < UeArr.size(); i++) {
    		UserEquipment ue = UeArr.get(i);
    		
    		int ueID = ue.getUeID();
    		double totalDemand = ue.getTotalDemand();
    		double dataRate = ue.getPeriodicalDataUsage();
    		
    		OCS.setTotalDemandAndDataRate(ueID, totalDemand, dataRate);
    	}
    }
}
