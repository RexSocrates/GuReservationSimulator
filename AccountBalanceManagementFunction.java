/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Socrates
 */
public class AccountBalanceManagementFunction {
    // 單位 : GB
    private double totalDataAllowance;
    private double remainingDataAllowance;
    
    public AccountBalanceManagementFunction(double totalDataAllowance) {
        // transfer GB to MB
        this.totalDataAllowance = totalDataAllowance * 1024;
        this.remainingDataAllowance = totalDataAllowance * 1024;
    }

    public double getTotalDataAllowance() {
        return totalDataAllowance;
    }

    public void setTotalDataAllowance(double totalDataAllowance) {
        this.totalDataAllowance = totalDataAllowance;
    }

    public double getRemainingDataAllowance() {
        return remainingDataAllowance;
    }

    public void setRemainingDataAllowance(double remainingDataAllowance) {
        this.remainingDataAllowance = remainingDataAllowance;
    }
}
