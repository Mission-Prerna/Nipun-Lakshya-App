package com.samagra.workflowengine.workflow.sample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.samagra.parent.R;
import com.samagra.workflowengine.workflow.WorkflowBaseFragment;
import com.samagra.workflowengine.workflow.WorkflowModuleCallback;
import com.samagra.workflowengine.workflow.model.stateresult.StateResult;

public class WorkflowFragment extends WorkflowBaseFragment {


    public WorkflowFragment(WorkflowModuleCallback callback) {
        super(callback);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workflow, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView)view.findViewById(R.id.title_tv)).setText("Bolo Module");
        view.findViewById(R.id.success_tv).setOnClickListener(v -> {
            if (callback != null) {
                StateResult stateResult = new StateResult();
                callback.onSuccess(stateResult);
                dismiss();
            }
        });
        view.findViewById(R.id.failure_tv).setOnClickListener(v -> {
            if (callback != null) {
                StateResult stateResult = new StateResult();
                callback.onFailure(stateResult);
                dismiss();
            }
        });
    }
}
