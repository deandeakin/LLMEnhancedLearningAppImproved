package com.deankennedy.llmenhancedlearningapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.deankennedy.llmenhancedlearningapp.R;
import com.deankennedy.llmenhancedlearningapp.data.AppDatabase;
import com.deankennedy.llmenhancedlearningapp.data.TaskHistory;
import com.deankennedy.llmenhancedlearningapp.model.LearningRequest;
import com.deankennedy.llmenhancedlearningapp.model.LearningResponse;
import com.deankennedy.llmenhancedlearningapp.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultActivity extends AppCompatActivity {

    private TextView tvResultTitle, tvPrompt, tvResponse, tvError, tvQuestion1ResultLabel, tvQuestion1Result, tvQuestion2ResultLabel, tvQuestion2Result, tvOverallSummaryLabel, tvOverallSummary;
    private ProgressBar progressBar;
    private Button btnRetry, btnBackToHome;

    private AppDatabase database;

    private String username, topic, lessonSummary, question, selectedAnswer, correctAnswer, utility;
    private String question1, selectedAnswer1, correctAnswer1, question2, selectedAnswer2, correctAnswer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvPrompt = findViewById(R.id.tvPrompt);
        tvResponse = findViewById(R.id.tvResponse);
        tvError = findViewById(R.id.tvError);
        tvQuestion1ResultLabel = findViewById(R.id.tvQuestion1ResultLabel);
        tvQuestion1Result = findViewById(R.id.tvQuestion1Result);
        tvQuestion2ResultLabel = findViewById(R.id.tvQuestion2ResultLabel);
        tvQuestion2Result = findViewById(R.id.tvQuestion2Result);
        tvOverallSummaryLabel = findViewById(R.id.tvOverallSummaryLabel);
        tvOverallSummary = findViewById(R.id.tvOverallSummary);
        progressBar = findViewById(R.id.progressBar);
        btnRetry = findViewById(R.id.btnRetry);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        database = AppDatabase.getInstance(this);

        username = getIntent().getStringExtra("username");
        topic = getIntent().getStringExtra("topic");
        lessonSummary = getIntent().getStringExtra("lessonSummary");
        question = getIntent().getStringExtra("question");
        selectedAnswer = getIntent().getStringExtra("selectedAnswer");
        correctAnswer = getIntent().getStringExtra("correctAnswer");
        utility = getIntent().getStringExtra("utility");
        question1 = getIntent().getStringExtra("question1");
        selectedAnswer1 = getIntent().getStringExtra("selectedAnswer1");
        correctAnswer1 = getIntent().getStringExtra("correctAnswer1");
        question2 = getIntent().getStringExtra("question2");
        selectedAnswer2 = getIntent().getStringExtra("selectedAnswer2");
        correctAnswer2 = getIntent().getStringExtra("correctAnswer2");

        // Main title changes depending on the utility mode.
        if ("hint".equals(utility)) {
            tvResultTitle.setText("Generated Hint");
        } else if ("explain".equals(utility)) {
            tvResultTitle.setText("Answer Explanation");
        } else {
            tvResultTitle.setText("Your Results");
        }

        btnRetry.setOnClickListener(v -> loadResult());

        if ("submit".equals(utility)) {
            btnBackToHome.setText("Back to Home");
            btnBackToHome.setOnClickListener(v -> {
                Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        } else {
            btnBackToHome.setText("Back to Task");
            btnBackToHome.setOnClickListener(v -> finish());
        }

        loadResult();
    }

    // Main entry point for the result screen. Submit mode shows local results, while hint/explain calls the backend.
    private void loadResult() {
        if ("submit".equals(utility)) {
            showSubmissionResults();
            return;
        }

        LearningRequest request = new LearningRequest(username, topic, lessonSummary, question, selectedAnswer, correctAnswer, utility);

        showLoading();

        RetrofitClient.getApiService().getLearningFeedback(request).enqueue(new Callback<LearningResponse>() {
            @Override
            public void onResponse(Call<LearningResponse> call, Response<LearningResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showSuccess(response.body().getPrompt(), response.body().getResponse());
                } else {
                    String errorText = "Backend error. Code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorText += "\n\n" + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorText += "\n\nCould not read error body: " + e.getMessage();
                    }
                    showError(errorText);
                }
            }

            @Override
            public void onFailure(Call<LearningResponse> call, Throwable t) {
                showError("Failed to connect to the backend.\n\n" + t.getMessage());
            }
        });
    }

    // Displays the final results for the two-question task submission.
    private void showSubmissionResults() {
        hideSingleResponseViews();
        hideError();

        boolean q1Correct = selectedAnswer1 != null && selectedAnswer1.equals(correctAnswer1);
        boolean q2Correct = selectedAnswer2 != null && selectedAnswer2.equals(correctAnswer2);

        String q1Text = "Question: " + question1 + "\nYour answer: " + selectedAnswer1 + "\nCorrect answer: " + correctAnswer1 + "\nResult: " + (q1Correct ? "Correct" : "Incorrect");

        String q2Text = "Question: " + question2 + "\nYour answer: " + selectedAnswer2 + "\nCorrect answer: " + correctAnswer2 + "\nResult: " + (q2Correct ? "Correct" : "Incorrect");

        int score = 0;
        if (q1Correct) score++;
        if (q2Correct) score++;

        String overallText;
        if (score == 2) {
            overallText = "You've answered both questions correctly! Awesome work!";
        } else if (score == 1) {
            overallText = "Almost perfect! You answered 1 out of 2 questions correctly. Have another try!";
        } else {
            overallText = "Nice attempt! You answered 0 out of 2 questions correctly. Try again!";
        }

        // Submission result cards.
        tvQuestion1ResultLabel.setVisibility(View.VISIBLE);
        tvQuestion1Result.setVisibility(View.VISIBLE);
        tvQuestion2ResultLabel.setVisibility(View.VISIBLE);
        tvQuestion2Result.setVisibility(View.VISIBLE);
        tvOverallSummaryLabel.setVisibility(View.VISIBLE);
        tvOverallSummary.setVisibility(View.VISIBLE);

        tvQuestion1Result.setText(q1Text);
        tvQuestion2Result.setText(q2Text);
        tvOverallSummary.setText(overallText);

        // Saves the final submission to the database.
        TaskHistory taskHistory = new TaskHistory(
                username,
                topic,
                "Final task submission",
                selectedAnswer1 + " | " + selectedAnswer2,
                correctAnswer1 + " | " + correctAnswer2,
                utility,
                "Two-question submission",
                overallText,
                System.currentTimeMillis()
        );

        database.taskHistoryDao().insert(taskHistory);
    }

    // Shows the loading state while waiting for the backend.
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);

        showSingleResponseViews();
        hideSubmissionViews();

        tvPrompt.setText("Generating prompt...");
        tvResponse.setText("Waiting for the model response...");
    }

    // Shows the backend prompt and response result and stores it in the database.
    private void showSuccess(String prompt, String response) {
        progressBar.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);

        showSingleResponseViews();
        hideSubmissionViews();

        tvPrompt.setText(prompt);
        tvResponse.setText(response);

        TaskHistory taskHistory = new TaskHistory(username, topic, question, selectedAnswer, correctAnswer, utility, prompt, response, System.currentTimeMillis());

        database.taskHistoryDao().insert(taskHistory);
    }

    // Shows and error message and displays the retry button.
    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        tvError.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.VISIBLE);
        tvError.setText(message);
    }

    // Shows the single response layout using for hint/explain.
    private void showSingleResponseViews() {
        tvPrompt.setVisibility(View.VISIBLE);
        tvResponse.setVisibility(View.VISIBLE);
    }

    // Hides the single response layout.
    private void hideSingleResponseViews() {
        tvPrompt.setVisibility(View.GONE);
        tvResponse.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
    }

    // Hides the final submission result views.
    private void hideSubmissionViews() {
        tvQuestion1ResultLabel.setVisibility(View.GONE);
        tvQuestion1Result.setVisibility(View.GONE);
        tvQuestion2ResultLabel.setVisibility(View.GONE);
        tvQuestion2Result.setVisibility(View.GONE);
        tvOverallSummaryLabel.setVisibility(View.GONE);
        tvOverallSummary.setVisibility(View.GONE);
    }

    // Hides the visible error message if one exists.
    private void hideError() {
        tvError.setVisibility(View.GONE);
    }
}
