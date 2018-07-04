/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Scanner;
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
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        int numOfDevices = 0;
        System.out.print("Enter the number of devices : ");
        numOfDevices = input.nextInt();
        System.out.println("");
        
        System.out.println("1. Fixed scheme");
        System.out.println("2. Multiplicative scheme");
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
        }
        
        // add the user equipments into the array
        for(int i = 0; i <= numOfDevices; i++) {
            UeArr.add(new UserEquipment(i, OCS));
        }
        
        // stimulate that the UEs keep sending online charging request util the data allowance becomes 0.
//        int deviceCount = 0;
//        while(OCS.getABMF().getRemainingDataAllowance() >= OCS.getOCF().determineGU()) {
//            UserEquipment ue = UeArr.get(deviceCount);
//            deviceCount = (deviceCount + 1) % UeArr.size();
//            
//            ue.sendOnlineChargingRequest();
//            System.out.printf("Number of signals : %5.0f\n", ue.getProducedSignals());
//            System.out.printf("Remaining data allowance : %10.2f\n", OCS.getABMF().getRemainingDataAllowance());
//            System.out.println("");
//        }
        
        // stimulate that the devices send online charging request once
        if(UeArr.size() * OCS.getOCF().determineGU(new Hashtable<String, Double>()) < OCS.getRemainingDataAllowance()) {
            for(int i = 0; i < UeArr.size(); i++) {
                UserEquipment ue = UeArr.get(i);
                
                ue.sendOnlineChargingRequestSessionStart();
            }
        }
        
        // consume GU, stimulate that the devices keep consuming GU until the data allowance becomes 0 or less that default GU.
        double defaultGU = OCS.getOCF().determineGU(new Hashtable<String, Double>());
        int deviceCount = 0;
        while(OCS.getRemainingDataAllowance() >= OCS.getOCF().determineGU(new Hashtable<String, Double>())) {
            double randomConsumedGU = Math.random() * 2 * defaultGU;
            System.out.printf("Random GU : %5.2f\n", randomConsumedGU);
            UserEquipment ue = UeArr.get(deviceCount);
            deviceCount = (deviceCount + 1) % UeArr.size();
            
            System.out.printf("Remaining data allowance : %10.2f\n", OCS.getRemainingDataAllowance());
            
            ue.consumeGU(randomConsumedGU);
        }
        
        // the remaining data allowance is not enough, so sessions terminate
        for(int i = 0; i < UeArr.size(); i++) {
            UserEquipment ue = UeArr.get(i);
            
            ue.sendOnlineChargingRequestSessionEnd();
        }
        
        
        
        // get total signals of this operation
        double totalSignals = 0;
        for(int i = 0; i < UeArr.size(); i++) {
            UserEquipment ue = UeArr.get(i);
            
            double signals = ue.getProducedSignals();
            totalSignals += signals;
            System.out.printf("Signals : %5.0f\n", signals);
        }
        System.out.printf("Total signals : %f\n", totalSignals);
        
    }

    // configure the fixed scheme
    private static OnlineChargingSystem fixedScheme(double totalDataAllowance) {
        System.out.print("Enter the default GU(MB) for fixed scheme : ");
        double defaultGU = input.nextDouble();
        System.out.println("");
        
        // configure online charging function for fixed scheme
        OnlineChargingFunctionFixedScheme OCF = new OnlineChargingFunctionFixedScheme(defaultGU);
        // configure account balance management function
        AccountBalanceManagementFunction ABMF = new AccountBalanceManagementFunction(totalDataAllowance);
        
        // create an instance for online charging system
        OnlineChargingSystem OCS = new OnlineChargingSystem(OCF, ABMF);
        
        return OCS;
    }

    private static OnlineChargingSystem multiplicativeScheme(double totalDataAllowance) {
        System.out.print("Enter default GU(MB) : ");
        double defaultGU = input.nextDouble();
        System.out.println("");
        
        System.out.print("Enter C : ");
        double c = input.nextInt();
        System.out.println("");
        
        // configure online charging function for multiplicative scheme
        OnlineChargingFunctionMultiplicativeScheme OCF = new OnlineChargingFunctionMultiplicativeScheme(defaultGU, c);
        // configure account balance management function
        AccountBalanceManagementFunction ABMF = new AccountBalanceManagementFunction(totalDataAllowance);
        
        OnlineChargingSystem OCS = new OnlineChargingSystem(OCF, ABMF);
        
        return OCS;
        
    }
    
}
