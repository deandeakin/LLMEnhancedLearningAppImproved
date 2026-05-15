package com.deankennedy.llmenhancedlearningapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.deankennedy.llmenhancedlearningapp.R;
import com.deankennedy.llmenhancedlearningapp.model.TaskGenerationRequest;
import com.deankennedy.llmenhancedlearningapp.model.TaskGenerationResponse;
import com.deankennedy.llmenhancedlearningapp.network.RetrofitClient;
import com.deankennedy.llmenhancedlearningapp.utils.UserPrefs;

import java.util.ArrayList;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskActivity extends AppCompatActivity {

    private TextView tvTaskTitle, tvTaskDescription, tvQuestion;
    private RadioGroup rgAnswers;
    private RadioButton rbAnswer1, rbAnswer2, rbAnswer3;
    private Button btnGenerateHint, btnExplainAnswer, btnSubmitTask;

    private String username, currentTopic, lessonSummary;
    private String question1, answer1A, answer1B, answer1C, correctAnswer1;
    private String question2, answer2A, answer2B, answer2C, correctAnswer2;
    private String selectedAnswer1 = "", selectedAnswer2 = "";

    // Keeps track of which question is being shown.
    private int questionNumber = 0;

    // Becomes true if task generation fails.
    private boolean loadErrorOccurred = false;

    private ArrayList<String> interests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        tvTaskTitle = findViewById(R.id.tvTaskTitle);
        tvTaskDescription = findViewById(R.id.tvTaskDescription);
        tvQuestion = findViewById(R.id.tvQuestion);
        rgAnswers = findViewById(R.id.rgAnswers);
        rbAnswer1 = findViewById(R.id.rbAnswer1);
        rbAnswer2 = findViewById(R.id.rbAnswer2);
        rbAnswer3 = findViewById(R.id.rbAnswer3);
        btnGenerateHint = findViewById(R.id.btnGenerateHint);
        btnExplainAnswer = findViewById(R.id.btnExplainAnswer);
        btnSubmitTask = findViewById(R.id.btnSubmitTask);

        // Read user data passed to the activity.
        username = getIntent().getStringExtra("username");
        interests = getIntent().getStringArrayListExtra("interests");

        // Fall back to saved user data if no data was passed.
        if (username == null || username.isEmpty()) {
            username = UserPrefs.getUsername(this);
        }

        if (interests == null || interests.isEmpty()) {
            Set<String> savedInterestSet = UserPrefs.getInterests(this);
            interests = new ArrayList<>(savedInterestSet);
        }

        if (username == null || username.isEmpty()) {
            username = "Student";
        }

        loadGeneratedTask();

        // Opens ResultActivity with the hint utility.
        btnGenerateHint.setOnClickListener(v -> {
            if (!isTaskReady()) {
                Toast.makeText(TaskActivity.this, "Please wait for the task to load", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(TaskActivity.this, ResultActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("topic", currentTopic);
            intent.putExtra("lessonSummary", lessonSummary);

            if (questionNumber == 0) {
                intent.putExtra("question", question1);
                intent.putExtra("selectedAnswer", "");
                intent.putExtra("correctAnswer", correctAnswer1);
            } else {
                intent.putExtra("question", question2);
                intent.putExtra("selectedAnswer", "");
                intent.putExtra("correctAnswer", correctAnswer2);
            }

            intent.putExtra("utility", "hint");
            startActivity(intent);
        });

        // Opens ResultActivity in explanation mode for the selected answer.
        btnExplainAnswer.setOnClickListener(v -> {
            if (!isTaskReady()) {
                Toast.makeText(TaskActivity.this, "Please wait for the task to load", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedId = rgAnswers.getCheckedRadioButtonId();

            if (selectedId == -1) {
                Toast.makeText(TaskActivity.this, "Please select an answer first", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedRadio = findViewById(selectedId);
            String selectedAnswer = selectedRadio.getText().toString();

            Intent intent = new Intent(TaskActivity.this, ResultActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("topic", currentTopic);
            intent.putExtra("lessonSummary", lessonSummary);

            if (questionNumber == 0) {
                intent.putExtra("question", question1);
                intent.putExtra("selectedAnswer", selectedAnswer);
                intent.putExtra("correctAnswer", correctAnswer1);
            } else {
                intent.putExtra("question", question2);
                intent.putExtra("selectedAnswer", selectedAnswer);
                intent.putExtra("correctAnswer", correctAnswer2);
            }

            intent.putExtra("utility", "explain");
            startActivity(intent);
        });

        // Main task button: - retries generation if loading fails. - moves from Q1 to Q2. - submits task.
        btnSubmitTask.setOnClickListener(v -> {
            if (loadErrorOccurred) {
                loadGeneratedTask();
                return;
            }

            if (!isTaskReady()) {
                Toast.makeText(TaskActivity.this, "Please wait for the task to load", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedId = rgAnswers.getCheckedRadioButtonId();

            if (selectedId == -1) {
                Toast.makeText(TaskActivity.this, "Please select an answer before proceeding", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedRadio = findViewById(selectedId);
            String selectedAnswer = selectedRadio.getText().toString();

            // Question 1 complete: stores answe and switches to Q2 UI.
            if (questionNumber == 0) {
                selectedAnswer1 = selectedAnswer;
                questionNumber = 1;
                populateCurrentQuestion();
            } else {
                // Question 2 complete: send both answers to the result screen.
                selectedAnswer2 = selectedAnswer;

                Intent intent = new Intent(TaskActivity.this, ResultActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("topic", currentTopic);
                intent.putExtra("lessonSummary", lessonSummary);

                intent.putExtra("question1", question1);
                intent.putExtra("selectedAnswer1", selectedAnswer1);
                intent.putExtra("correctAnswer1", correctAnswer1);

                intent.putExtra("question2", question2);
                intent.putExtra("selectedAnswer2", selectedAnswer2);
                intent.putExtra("correctAnswer2", correctAnswer2);

                intent.putExtra("utility", "submit");
                startActivity(intent);
            }
        });
    }

    // Requests a new generated task from the backend API.
    private void loadGeneratedTask() {
        showTaskLoading();

        TaskGenerationRequest request = new TaskGenerationRequest(username, interests);

        RetrofitClient.getApiService().generateTask(request).enqueue(new Callback<TaskGenerationResponse>() {
            @Override
            public void onResponse(Call<TaskGenerationResponse> call, Response<TaskGenerationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateTask(response.body());
                } else {
                    showTaskError("There was an error when generating your task. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<TaskGenerationResponse> call, Throwable t) {
                showTaskError("Connection failed while generating task.\n\n" + t.getMessage());
            }
        });
    }

    // Stores the generated task data locally and then shows Q1 first.
    private void populateTask(TaskGenerationResponse task) {
        currentTopic = task.getTopic();
        lessonSummary = task.getLessonSummary();
        question1 = task.getQuestion1();
        answer1A = task.getAnswer1A();
        answer1B = task.getAnswer1B();
        answer1C = task.getAnswer1C();
        correctAnswer1 = task.getCorrectAnswer1();

        question2 = task.getQuestion2();
        answer2A = task.getAnswer2A();
        answer2B = task.getAnswer2B();
        answer2C = task.getAnswer2C();
        correctAnswer2 = task.getCorrectAnswer2();

        questionNumber = 0;
        selectedAnswer1 = "";
        selectedAnswer2 = "";

        populateCurrentQuestion();
    }

    // Updates the UI to show either Q1 or Q2.
    private void populateCurrentQuestion() {
        loadErrorOccurred = false;

        tvTaskTitle.setText("Generated Task 1");
        tvTaskDescription.setText(lessonSummary);

        rgAnswers.clearCheck();

        if (questionNumber == 0) {
            tvQuestion.setText(question1);
            rbAnswer1.setText(answer1A);
            rbAnswer2.setText(answer1B);
            rbAnswer3.setText(answer1C);
            btnSubmitTask.setText("Next");
        } else {
            tvQuestion.setText(question2);
            rbAnswer1.setText(answer2A);
            rbAnswer2.setText(answer2B);
            rbAnswer3.setText(answer2C);
            btnSubmitTask.setText("Submit");
        }

        rbAnswer1.setEnabled(true);
        rbAnswer2.setEnabled(true);
        rbAnswer3.setEnabled(true);

        btnGenerateHint.setEnabled(true);
        btnExplainAnswer.setEnabled(true);
        btnSubmitTask.setEnabled(true);
    }

    // Shows a temporary loading screen while the backend generates and loads.
    private void showTaskLoading() {
        loadErrorOccurred = false;

        tvTaskTitle.setText("Generated Task 1");
        tvTaskDescription.setText("Generating a task based off your interests.");
        tvQuestion.setText("Your question is being prepared. Please wait.");

        rbAnswer1.setText("Option 1 loading");
        rbAnswer2.setText("Option 2 loading");
        rbAnswer3.setText("Option 3 loading");

        rgAnswers.clearCheck();

        rbAnswer1.setEnabled(false);
        rbAnswer2.setEnabled(false);
        rbAnswer3.setEnabled(false);

        btnGenerateHint.setEnabled(false);
        btnExplainAnswer.setEnabled(false);
        btnSubmitTask.setEnabled(false);
    }

    // Shows an error state if the generation fails and allows the user to retry.
    private void showTaskError(String message) {
        loadErrorOccurred = true;

        tvTaskTitle.setText("Generated Task 1");
        tvTaskDescription.setText("There was an error when generating your task.");
        tvQuestion.setText(message);

        rbAnswer1.setText("Option not available");
        rbAnswer2.setText("Option not available");
        rbAnswer3.setText("Option not available");

        rgAnswers.clearCheck();

        rbAnswer1.setEnabled(false);
        rbAnswer2.setEnabled(false);
        rbAnswer3.setEnabled(false);

        btnGenerateHint.setEnabled(false);
        btnExplainAnswer.setEnabled(false);

        btnSubmitTask.setEnabled(true);
        btnSubmitTask.setText("Retry Task Generation");
    }

    // Returns true if all required data has been loaded.
    private boolean isTaskReady() {
        return currentTopic != null && lessonSummary != null && question1 != null && correctAnswer1 != null && question2 != null && correctAnswer2 != null;
    }
}