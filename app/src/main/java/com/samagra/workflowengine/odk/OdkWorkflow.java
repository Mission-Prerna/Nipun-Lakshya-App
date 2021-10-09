package com.samagra.workflowengine.odk;

import static com.samagra.commons.utils.CommonConstants.ODK;

import android.content.Context;
import android.content.Intent;

import com.samagra.commons.travel.BroadcastAction;
import com.samagra.commons.travel.BroadcastActionSingleton;
import com.samagra.commons.travel.BroadcastEvents;
import com.samagra.commons.travel.TravellingObserver;
import com.samagra.commons.utils.NetworkStateManager;
import com.samagra.parent.AppConstants;
import com.samagra.parent.UtilityFunctions;
import com.samagra.workflowengine.workflow.WorkFlow;
import com.samagra.workflowengine.workflow.WorkflowModuleCallback;
import com.samagra.workflowengine.workflow.model.stateresult.AssessmentStateResult;
import com.samagra.workflowengine.workflow.model.stateresult.ModuleResult;

import java.util.Date;

public class OdkWorkflow extends WorkFlow<Context, OdkProperties> {

    private OdkProperties mProps;

    public OdkWorkflow(WorkflowModuleCallback callback) {
        super(callback);
        mProps = new OdkProperties();
    }

    @Override
    public void onStart(Context context) {
        redirectToODKInstruction(context);
    }

    @Override
    public void setProps(OdkProperties props) {
        mProps = props;
    }

    @Override
    public void handleCallbacks() {
        TravellingObserver<BroadcastAction> broadcastActionTravellingObserver = new TravellingObserver<>(broadcastAction -> {
            if (broadcastAction.getLiveActionEvent() != BroadcastEvents.ODK_SUCCESS && broadcastAction.getLiveActionEvent() != BroadcastEvents.ODK_FAILURE) {
                return;
            }
            AssessmentStateResult res = new AssessmentStateResult();
            if (broadcastAction.getLiveActionValue() != null) {
                res = (AssessmentStateResult) broadcastAction.getLiveActionValue();
//                res.getModuleResult().setStateGrade(ReadAlongManager.getInstance().getProps().getStateGrade());
                res.setGrade(mProps.getGrade());
                res.setWorkflowRefId(mProps.getFormID());
                res.setSubject(mProps.getSubject());
//                res.getModuleResult().setStartTime(startTime.getTime());
//                res.getModuleResult().setEndTime(new Date().getTime());
            } else {
                res.setWorkflowRefId(mProps.getFormID());
                res.setGrade(mProps.getGrade());
                res.setSubject(mProps.getSubject());
                ModuleResult moduleResult = new ModuleResult();
                moduleResult.setNetworkActive(NetworkStateManager.getInstance().getNetworkConnectivityStatus());
                moduleResult.setModule(ODK);
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
                case ODK_SUCCESS:
//                    Log.e("-->>", "ODK work flow Handle Callback success.");
                    callback.onSuccess(res);
                    break;
                case ODK_FAILURE:
//                    Log.e("-->>", "ODK work flow Handle Callback failure.");
                    callback.onFailure(res);
                    break;
            }
        }, BroadcastActionSingleton.getInstance().getLiveAppAction().version);
        BroadcastActionSingleton.getInstance().getLiveAppAction().observeForever("workflow_bus", broadcastActionTravellingObserver);
    }

    private void redirectToODKInstruction(Context context) {
        Intent intent = new Intent(context, OdkInstructionActivity.class);
        intent.putExtra(AppConstants.INTENT_ODK_PROPERTIES, mProps);
        context.startActivity(intent);
    }
}