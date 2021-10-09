package com.samagra.ancillaryscreens.di;

import org.odk.collect.android.contracts.IFormManagementContract;

public class FormManagementCommunicator {

    private static IFormManagementContract iFormManagementContract;
    public static void setContract(IFormManagementContract formContract){
        iFormManagementContract = formContract;
    }

    public static IFormManagementContract getContract(){
        return iFormManagementContract;
    }
}
