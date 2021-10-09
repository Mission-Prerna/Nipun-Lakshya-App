package com.samagra.workflowengine.workflow;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.samagra.parent.R;

public class WorkflowActivity extends AppCompatActivity {

    private TextView streamTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.s_workflow);
        streamTv = findViewById(R.id.stream_tv);
        String configJson = "{\"flowConfigs\":[{\"gradeNumber\":3,\"subject\":\"english\",\"states\":[1,2,3]}],\"actions\":[{\"id\":1,\"count\":null,\"type\":\"POP\",\"futureStateId\":null},{\"id\":2,\"count\":null,\"type\":\"CLEAR\",\"futureStateId\":null},{\"id\":3,\"count\":2,\"type\":\"PUSH\",\"futureStateId\":1}],\"states\":[{\"id\":1,\"maxFailureCount\":3,\"subject\":\"english\",\"gradeNumber\":\"3\",\"type\":\"bolo\",\"stateData\":null,\"decision\":{\"id\":1,\"meta\":null,\"successActions\":[1],\"failureActions\":[2,3]}},{\"id\":2,\"subject\":\"english\",\"gradeNumber\":\"4\",\"type\":\"bolo\",\"maxFailureCount\":3,\"stateData\":null,\"decision\":{\"id\":1,\"meta\":null,\"successActions\":[1],\"failureActions\":[1]}},{\"id\":3,\"subject\":\"english\",\"gradeNumber\":\"5\",\"type\":\"quml\",\"maxFailureCount\":3,\"stateData\":null,\"decision\":{\"id\":1,\"meta\":null,\"successActions\":[1],\"failureActions\":[1]}},{\"id\":4,\"subject\":\"english\",\"gradeNumber\":\"3\",\"type\":\"quml\",\"maxFailureCount\":3,\"stateData\":null,\"decision\":{\"id\":1,\"meta\":null,\"successActions\":[1],\"failureActions\":[1]}}]}";
        WorkflowManager.getInstance().loadConfig(configJson);
        streamTv.setText("Workflow not started");
    }

    public void onStart(View view) {

    }

    public void onInterrupt(View view) {

    }

    public void onEnd(View view) {

    }
}
