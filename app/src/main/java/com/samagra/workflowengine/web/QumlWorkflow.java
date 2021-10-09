package com.samagra.workflowengine.web;

import android.content.Context;
import android.content.Intent;

import com.samagra.workflowengine.workflow.WorkFlow;
import com.samagra.workflowengine.workflow.WorkflowModuleCallback;
import com.samagra.commons.travel.BroadcastAction;
import com.samagra.commons.travel.BroadcastActionSingleton;
import com.samagra.commons.travel.TravellingObserver;
import com.samagra.workflowengine.workflow.model.stateresult.StateResult;

public class QumlWorkflow extends WorkFlow<Context, String> {

    public QumlWorkflow(WorkflowModuleCallback callback) {
        super(callback);
    }

    @Override
    public void onStart(Context context) {
        context.startActivity(new Intent(context, WebViewActivity.class));
    }

    @Override
    public void setProps(String props) {

    }

    @Override
    public void handleCallbacks() {
        TravellingObserver<BroadcastAction> broadcastActionTravellingObserver = new TravellingObserver<>(broadcastAction -> {
            switch (broadcastAction.getLiveActionEvent()) {
                case QUML_MODULE_SUCCESS:
                    StateResult stateResult = new StateResult();
                    callback.onSuccess(stateResult); break;
                case QUML_MODULE_FAILURE:
                    StateResult stateResult1 = new StateResult();
                    callback.onFailure(stateResult1);
                    break;
            }
        }, BroadcastActionSingleton.getInstance().getLiveAppAction().version);
        BroadcastActionSingleton.getInstance().getLiveAppAction().observeForever("workflow_bus", broadcastActionTravellingObserver);
    }


}
