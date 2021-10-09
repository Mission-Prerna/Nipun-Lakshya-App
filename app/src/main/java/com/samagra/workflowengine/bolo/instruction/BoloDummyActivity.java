package com.samagra.workflowengine.bolo.instruction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.samagra.parent.R;
import com.samagra.parent.databinding.LayoutBoloDummyBinding;

public class BoloDummyActivity  extends AppCompatActivity {
    private LayoutBoloDummyBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.layout_bolo_dummy);
        mBinding.btnSuccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("result", 1);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        mBinding.btnFailure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("result", 0);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }
}
