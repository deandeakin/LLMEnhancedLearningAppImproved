package com.deankennedy.llmenhancedlearningapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deankennedy.llmenhancedlearningapp.R;
import com.deankennedy.llmenhancedlearningapp.data.TaskHistory;

import java.util.List;

public class TaskHistoryAdapter extends RecyclerView.Adapter<TaskHistoryAdapter.TaskHistoryViewHolder> {

    private final List<TaskHistory> historyList;

    // Receives the submitted task history records shown in the history list.
    public TaskHistoryAdapter(List<TaskHistory> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public TaskHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_history, parent, false);

        return new TaskHistoryViewHolder(view);
    }

    // Populates each history card with the question, selected answer, correct answer, and result.
    @Override
    public void onBindViewHolder(@NonNull TaskHistoryViewHolder holder, int position) {
        TaskHistory history = historyList.get(position);

        String selectedAnswer = history.getSelectedAnswer();
        String correctAnswer = history.getCorrectAnswer();

        // Checks the saved selected answer with the saved correct answer.
        boolean isCorrect = selectedAnswer != null && correctAnswer != null && selectedAnswer.trim().equals(correctAnswer.trim());

        holder.tvHistoryTopic.setText("Topic: " + history.getTopic());
        holder.tvHistoryQuestion.setText("Question: " + history.getQuestion());
        holder.tvHistoryAnswer.setText("Your answer: " + selectedAnswer);
        holder.tvHistoryCorrectAnswer.setText("Correct answer: " + correctAnswer);
        holder.tvHistoryResult.setText("Result: " + (isCorrect ? "Correct" : "Incorrect"));
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    // Holds the views for each history card.
    public static class TaskHistoryViewHolder extends RecyclerView.ViewHolder {

        TextView tvHistoryTopic, tvHistoryQuestion, tvHistoryAnswer, tvHistoryCorrectAnswer, tvHistoryResult;

        public TaskHistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            tvHistoryTopic = itemView.findViewById(R.id.tvHistoryTopic);
            tvHistoryQuestion = itemView.findViewById(R.id.tvHistoryQuestion);
            tvHistoryAnswer = itemView.findViewById(R.id.tvHistoryAnswer);
            tvHistoryCorrectAnswer = itemView.findViewById(R.id.tvHistoryCorrectAnswer);
            tvHistoryResult = itemView.findViewById(R.id.tvHistoryResult);
        }
    }
}