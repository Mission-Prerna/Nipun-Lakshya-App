package org.odk.collect.android.contracts;


public class ComponentManager {
    public static IFormManagementContract iFormManagementContract;

    /**
     *
     * @param formManagmentClassImpl Interface Contract
     */
    public static void registerFormManagementPackage(IFormManagementContract formManagmentClassImpl) {
        if(formManagmentClassImpl == null)
            formManagmentClassImpl = new FormManagementSectionInteractor();
        iFormManagementContract = formManagmentClassImpl;
    }

}