package com.samagra.workflowengine.web;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.samagra.parent.R;
import com.samagra.workflowengine.web.model.ui.QuestionResult;

import java.util.List;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {

    private final List<QuestionResult> results;
    private final Context context;

    public ResultsAdapter(Context context, List<QuestionResult> results) {
        this.results = results;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(context)
                .inflate(R.layout.item_quml_results, parent, false);
        return new ViewHolder(itemLayoutView, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.updateItem(results.get(position));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView userAnswerTv;
        private final TextView correctAnswerTv;
        private final TextView statusTv;
        private final ImageView statusIv;
        private final Context context;
        private final TextView yourAnswerTv;
        TextView question;

        ViewHolder(final View itemLayoutView, Context context) {
            super(itemLayoutView);
            question = itemLayoutView.findViewById(R.id.question_tv);
            userAnswerTv = itemLayoutView.findViewById(R.id.user_answer_tv);
            correctAnswerTv = itemLayoutView.findViewById(R.id.correct_answer_tv);
            statusTv = itemLayoutView.findViewById(R.id.status_tv);
            yourAnswerTv = itemLayoutView.findViewById(R.id.your_answer_tv);
            statusIv = itemLayoutView.findViewById(R.id.status_iv);
            this.context = context;
        }

        public void updateItem(QuestionResult questionResult) {
            question.setText(questionResult.getQuestion());
            userAnswerTv.setText(questionResult.getUserAnswer());
            correctAnswerTv.setText(questionResult.getCorrectAnswer());
            statusTv.setText(questionResult.getAnswerStatus());

            if (questionResult.isCorrectAnswer()) {
                userAnswerTv.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                yourAnswerTv.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                statusIv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_correct_answer));
            } else if (questionResult.getAnswerStatus().equalsIgnoreCase("wrong")) {
                userAnswerTv.setTextColor(ContextCompat.getColor(context, R.color.wrong_answer));
                yourAnswerTv.setTextColor(ContextCompat.getColor(context, R.color.wrong_answer));
                statusIv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_wrong_answer));
            } else {
                statusTv.setTextColor(ContextCompat.getColor(context, R.color.gray));
            }
        }
    }
}
