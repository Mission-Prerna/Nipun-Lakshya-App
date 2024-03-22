package com.samagra.workflowengine.web;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.samagra.parent.R;
import com.samagra.workflowengine.workflow.WorkflowModuleCallback;
import com.samagra.workflowengine.web.model.ui.QuestionResult;
import com.samagra.workflowengine.workflow.model.stateresult.StateResult;

import java.util.List;

public class ResultsFragment extends Fragment {

    private List<QuestionResult> data;
    private WorkflowModuleCallback callback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                StateResult stateResult = new StateResult();
                callback.onSuccess(stateResult);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quml_results, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = getView().findViewById(R.id.results_rv);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        rv.setAdapter(new ResultsAdapter(getContext(), data));
        setStats();
    }

    public void setData(List<QuestionResult> data) {
        this.data = data;
    }

    private void setStats() {
        int correct = 0;
        int wrong = 0;
        int skipped = 0;
        int unattempted = 0;
        for (QuestionResult result : data) {
            if (result.isCorrectAnswer()) {
                correct++;
            } else if (result.getAnswerStatus().equalsIgnoreCase("wrong")) {
                wrong++;
            } else if (result.getAnswerStatus().equalsIgnoreCase("skipped")) {
                skipped++;
            } else {
                unattempted++;
            }
        }

        ((TextView) getView().findViewById(R.id.total_attempted_tv)).setText("Total : " + data.size());
        ((TextView) getView().findViewById(R.id.correct_attempted_tv)).setText("Correct : " + correct);
        ((TextView) getView().findViewById(R.id.incorrect_attempted_tv)).setText("Wrong : " + wrong);
        ((TextView) getView().findViewById(R.id.unattempted_tv)).setText("Unattempted : " + unattempted);
        ((TextView) getView().findViewById(R.id.skipped_tv)).setText("Skipped : " + skipped);
        getView().findViewById(R.id.proceed_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StateResult stateResult = new StateResult();
                callback.onSuccess(stateResult);
            }
        });
    }

    public void setCallback(WorkflowModuleCallback callback) {
        this.callback = callback;
    }


}
