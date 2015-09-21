/*
 * Test purpose only, run this class with the correct parameter should make a recharge
 */

package test;

import core.IRecarga;

public class Main {

    public static void main(String[] args) {
	
	/* Recharge parameters (CHANGE THIS) */
        String provider = IRecarga.providersList[3];
        String ddd = "76";
        String number = "97826496";
        String value = "10";
        
        IRecarga.makeRecharge(ddd, provider,number,value);

    }

}
