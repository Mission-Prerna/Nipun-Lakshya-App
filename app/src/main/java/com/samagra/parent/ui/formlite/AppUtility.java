package com.samagra.parent.ui.formlite;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.samagra.parent.ui.formlite.model.InputField;
import com.samagra.parent.ui.formlite.model.StudentDetailsFormData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AppUtility {

    public static final String FORM_FIELDS = "{\"formFields\":[{\"widget\":\"dropdown\",\"key\":\"nl_survey_1\",\"label\":\"q1Title\",\"required\":true,\"options\":[{\"value\":\"true\",\"label\":\"yes\"},{\"value\":\"false\",\"label\":\"no\"}],\"canEdit\":true,\"validationMessage\":\"selectOption\"},{\"widget\":\"dropdown\",\"key\":\"nl_survey_2\",\"label\":\"q2Title\",\"required\":true,\"options\":[{\"value\":\"1\",\"label\":\"1\"},{\"value\":\"2\",\"label\":\"2\"},{\"value\":\"3\",\"label\":\"3\"},{\"value\":\"4\",\"label\":\"4\"},{\"value\":\"5\",\"label\":\"5\"},{\"value\":\"6\",\"label\":\"6\"},{\"value\":\"7\",\"label\":\"7\"},{\"value\":\"8\",\"label\":\"8\"},{\"value\":\"9\",\"label\":\"9\"},{\"value\":\"10\",\"label\":\"10\"},{\"value\":\"11\",\"label\":\"11\"},{\"value\":\"12\",\"label\":\"12\"},{\"value\":\"13\",\"label\":\"13\"},{\"value\":\"14\",\"label\":\"14\"},{\"value\":\"15\",\"label\":\"15\"},{\"value\":\"16\",\"label\":\"16\"},{\"value\":\"17\",\"label\":\"17\"},{\"value\":\"18\",\"label\":\"18\"},{\"value\":\"19\",\"label\":\"19\"},{\"value\":\"20\",\"label\":\"20\"},{\"value\":\"21\",\"label\":\"21\"},{\"value\":\"22\",\"label\":\"22\"}],\"canEdit\":true,\"validationMessage\":\"selectOption\"},{\"widget\":\"dropdown\",\"key\":\"nl_survey_3\",\"label\":\"q3Title\",\"required\":true,\"options\":[{\"value\":\"true\",\"label\":\"yes\"},{\"value\":\"false\",\"label\":\"no\"}],\"canEdit\":true,\"validationMessage\":\"selectOption\"}],\"localization\":{\"en\":{\"q1Title\":\"आधारशिला क्रियान्वयन संदर्शिका का उपयोग:\\nक्या शिक्षक संदर्शिका के अनुसार शिक्षण-कार्य कर रहे हैं? (शिक्षक के पास उपस्थित संदर्शिका तथा संदर्शिका में दर्ज वार्षिक ट्रैकर के आधार पर अवलोकित करें)\",\"q2Title\":\"संदर्शिका के अनुसार शिक्षक किस सप्ताह का शिक्षण-कार्य कर रहे हैं? (वार्षिक ट्रैकर में दर्ज जानकारी के आधार पर)\",\"q3Title\":\"क्या कक्षा में कराया गया शिक्षण संदर्शिका में वर्णित शिक्षण योजना के चरणों के अनुसार कराया गया हैं?\",\"yes\":\"Yes\",\"no\":\"No\",\"selectOption\":\"कृपया एक उत्तर चुनें\"},\"hi\":{\"q1Title\":\"आधारशिला क्रियान्वयन संदर्शिका का उपयोग: क्या शिक्षक संदर्शिका के अनुसार शिक्षण-कार्य कर रहे हैं? (शिक्षक के पास उपस्थित संदर्शिका तथा संदर्शिका में दर्ज वार्षिक ट्रैकर के आधार पर अवलोकित करें)\",\"q2Title\":\"संदर्शिका के अनुसार शिक्षक किस सप्ताह का शिक्षण-कार्य कर रहे हैं? (वार्षिक ट्रैकर में दर्ज जानकारी के आधार पर)\",\"q3Title\":\"क्या कक्षा में कराया गया शिक्षण संदर्शिका में वर्णित शिक्षण योजना के चरणों के अनुसार कराया गया हैं?\",\"yes\":\"हाँ\",\"no\":\"नहीं\",\"selectOption\":\"कृपया एक उत्तर चुनें\"}}}";

    public static boolean isValueExist(Object obj){
        if(obj == null){
            return false;
        }else if(obj instanceof String){
            return !TextUtils.isEmpty((String)obj.toString());
        }else{
            return true;
        }
    }

    public static boolean parseToBoolean(Object obj){
        if(obj == null){
            return false;
        }else if(obj instanceof String){
            if(TextUtils.isEmpty((String)obj.toString())){
              return false;
            }else {
                return obj.toString().equalsIgnoreCase("true");
            }
        }else if(obj instanceof Boolean){
            return (Boolean)obj;
        }else if(obj instanceof Integer){
            return ((Integer)obj) != 0;
        }else return false;
    }

    public static Date getDateFromString(String dateStr, String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try{
            return sdf.parse(dateStr);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String getFormattedDate(Date dateStr, String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try{
            return sdf.format(dateStr);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static int getTimedDifferenceInYear(Date futureDate, Date pastDate){
        if(futureDate != null && pastDate != null){
            long differenceInMills = futureDate.getTime() - pastDate.getTime();
            long days =  TimeUnit.MILLISECONDS.toDays(differenceInMills);
            return (int)days / 365;
        }
        return 0;

    }

    public static String getSLCFileName(String id){
        String formattedDate = getFormattedDate(new Date(), "dd-MM-yyyy-HH:mm:ss");
        return  "SLC_"+id+"_"+formattedDate+".pdf";
    }

    public static String getSupportedId(int num){
        String output = Integer.toString(num);
        String appendedZeros = "";
        while(output.length() < 8){
            output =  "0" + output;
        }
        return output;
    }

    public static StudentDetailsFormData getStudentDetailsFormFields() {
        StudentDetailsFormData studentDetailsData = new Gson().fromJson(FORM_FIELDS, StudentDetailsFormData.class);
        return studentDetailsData;
    }

    public static InputField getField(List<InputField> formFields, String key) {
        if(formFields != null){
            for(InputField field : formFields){
                if(field.getKey().equalsIgnoreCase(key)){
                    return field;
                }
            }

        }
        return null;
    }

}
