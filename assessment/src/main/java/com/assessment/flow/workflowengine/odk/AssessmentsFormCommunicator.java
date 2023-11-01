package com.assessment.flow.workflowengine.odk;

import org.odk.collect.android.contracts.IFormManagementContract;

public class AssessmentsFormCommunicator {

    private static IFormManagementContract iFormManagementContract;
    public static void setContract(IFormManagementContract formContract){
        iFormManagementContract = formContract;
    }

    public static IFormManagementContract getContract(){
        return iFormManagementContract;
    }
}
