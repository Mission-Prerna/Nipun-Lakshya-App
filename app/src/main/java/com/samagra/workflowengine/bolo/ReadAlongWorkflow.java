package com.samagra.workflowengine.bolo;

import static com.samagra.commons.utils.CommonConstants.BOLO;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.samagra.commons.travel.BroadcastAction;
import com.samagra.commons.travel.BroadcastActionSingleton;
import com.samagra.commons.travel.BroadcastEvents;
import com.samagra.commons.travel.TravellingObserver;
import com.samagra.commons.utils.NetworkStateManager;
import com.samagra.parent.AppConstants;
import com.samagra.parent.UtilityFunctions;
import com.samagra.workflowengine.bolo.readalong.ReadAlongDisclaimerActivity;
import com.samagra.workflowengine.bolo.instruction.ReadAlongInstructionActivity;
import com.samagra.workflowengine.workflow.WorkFlow;
import com.samagra.workflowengine.workflow.WorkflowModuleCallback;
import com.samagra.workflowengine.workflow.model.stateresult.AssessmentStateResult;
import com.samagra.workflowengine.workflow.model.stateresult.ModuleResult;

import java.util.Date;

public class ReadAlongWorkflow extends WorkFlow<Context, ReadAlongProperties> {

    public static String READ_ALONG_PACKAGE = "com.google.android.apps.seekh";

    private ReadAlongProperties props;

    public ReadAlongWorkflow(WorkflowModuleCallback callback) {
        super(callback);
        props = new ReadAlongProperties();
    }

    @Override
    public void onStart(Context context) {
        if (props.shouldShowDisclaimer() || !isAppInstalled(context.getPackageManager(), READ_ALONG_PACKAGE)) {
            Intent intentToDisclaimer = new Intent(context, ReadAlongDisclaimerActivity.class);
            intentPutExtra(intentToDisclaimer, props, context);
        } else if (props.shouldShowInstructions()) {
            Intent intentToInstruction = new Intent(context, ReadAlongInstructionActivity.class);
            intentPutExtra(intentToInstruction, props, context);
        }
    }

    private void intentPutExtra(Intent intent, ReadAlongProperties props, Context context) {
        intent.putExtra(AppConstants.INTENT_RA_PROPERTIES, props);
        context.startActivity(intent);
    }

    @Override
    public void setProps(ReadAlongProperties props) {
        this.props = props;
    }

    @Override
    public void handleCallbacks() {
        TravellingObserver<BroadcastAction> broadcastActionTravellingObserver = new TravellingObserver<>(broadcastAction -> {
            if (broadcastAction.getLiveActionEvent() != BroadcastEvents.READ_ALONG_SUCCESS && broadcastAction.getLiveActionEvent() != BroadcastEvents.READ_ALONG_FAILURE) {
                return;
            }
            AssessmentStateResult res= new AssessmentStateResult();
            /*
            * assured that value would not null securing it if still gets null on some case
            * */
            if (broadcastAction.getLiveActionValue() != null) {
                res = (AssessmentStateResult) broadcastAction.getLiveActionValue();
                res.getModuleResult().setStateGrade(props.getStateGrade());
                res.setGrade(props.getGrade());
                res.setSubject(props.getSubject());
//                res.getModuleResult().setStartTime(startTime.getTime());
//                res.getModuleResult().setEndTime(new Date().getTime());
            }else{
                res.setWorkflowRefId("0");
                res.setGrade(props.getGrade());
                res.setSubject(props.getSubject());
                ModuleResult moduleResult = new ModuleResult();
                moduleResult.setModule(BOLO);
                moduleResult.setNetworkActive(NetworkStateManager.getInstance().getNetworkConnectivityStatus());
                moduleResult.setAchievement(0);
                moduleResult.setPassed(false); // false case
                moduleResult.setSessionCompleted(false);
                moduleResult.setAppVersionCode(UtilityFunctions.getVersionCode());
                moduleResult.setTotalQuestions(0);
                moduleResult.setSuccessCriteria(0);
                res.setModuleResult(moduleResult);
                res.getModuleResult().setStartTime(startTime.getTime());
                res.getModuleResult().setEndTime(new Date().getTime());
            }

            switch (broadcastAction.getLiveActionEvent()) {
                case READ_ALONG_SUCCESS:
                    callback.onSuccess(res);
                    break;
                case READ_ALONG_FAILURE:
                    callback.onFailure(res);
                    break;
            }
        }, BroadcastActionSingleton.getInstance().getLiveAppAction().version);
        BroadcastActionSingleton.getInstance().getLiveAppAction().observeForever("workflow_bus_bolo", broadcastActionTravellingObserver);
    }

    private boolean isAppInstalled(PackageManager pm, String uri) {
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
