package com.samagra.parent.ui.formlite;


import com.samagra.parent.ui.formlite.model.Localiation;
import com.samagra.parent.ui.formlite.model.StudentDetailsFormData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RegistrationManager {
    private static RegistrationManager sInstance;
    private Localiation dictionary;

    private RegistrationManager() {
    }

    public static RegistrationManager getInstance() {
        if (sInstance == null) {
            sInstance = new RegistrationManager();
        }
        return sInstance;
    }

    public Map<String, String> getDictionary(String lang){
        if(dictionary == null){
            StudentDetailsFormData formData = AppUtility.getStudentDetailsFormFields();
            dictionary = formData.getLocalization();
        }
        if(dictionary != null) {
            switch (lang) {
                case "hi":
                    return dictionary.getHi();

                case "en":
                    return dictionary.getEn();
            }
        }
        return null;
    }


    public Localiation getGlobalDictionary(){
        if(dictionary == null){
            StudentDetailsFormData formData = AppUtility.getStudentDetailsFormFields();
            dictionary = formData.getLocalization();
        }
        return dictionary;
    }

    public void clear() {
        sInstance = null;
    }
}
