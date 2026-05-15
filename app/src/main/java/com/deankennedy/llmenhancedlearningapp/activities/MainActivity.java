package com.deankennedy.llmenhancedlearningapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.deankennedy.llmenhancedlearningapp.utils.UserPrefs;
import com.deankennedy.llmenhancedlearningapp.R;
import com.deankennedy.llmenhancedlearningapp.data.AppDatabase;
import com.deankennedy.llmenhancedlearningapp.data.TaskHistory;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private TextView tvMainTitle, tvWelcome, tvNotification, tvGeneratedTaskLabel, tvGeneratedTaskDescription, tvRecentActivityLabel, tvRecentActivity;
    private Button btnStartTask, btnLogout;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prevents access to the dashboard if the user is not logged in.
        if (!UserPrefs.isLoggedIn(this)) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        tvMainTitle = findViewById(R.id.tvMainTitle);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvNotification = findViewById(R.id.tvNotification);
        tvGeneratedTaskLabel = findViewById(R.id.tvGeneratedTaskLabel);
        tvGeneratedTaskDescription = findViewById(R.id.tvGeneratedTaskDescription);
        tvRecentActivityLabel = findViewById(R.id.tvRecentActivityLabel);
        tvRecentActivity = findViewById(R.id.tvRecentActivity);
        database = AppDatabase.getInstance(this);
        btnStartTask = findViewById(R.id.btnStartTask);
        btnLogout = findViewById(R.id.btnLogout);

        String username = UserPrefs.getUsername(this);
        Set<String> savedInterestSet = UserPrefs.getInterests(this);
        ArrayList<String> interests = new ArrayList<>(savedInterestSet);

        // Populates the home screen text.
        tvWelcome.setText("Hello, " + username + "!");
        tvNotification.setText("You have 1 task due today.");
        tvGeneratedTaskLabel.setText("Generated Task 1");
        tvGeneratedTaskDescription.setText("Practice a short activity based on your selected interests.");

        // Loads the most recent AI/Task from the room history.
        loadRecentActivity(username);

        // Open the task screen and pass the user's interests forward.
        btnStartTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TaskActivity.class);
            intent.putExtra("username", username);
            intent.putStringArrayListExtra("interests", interests);
            startActivity(intent);
        });

        // Log the user out and returns to the login screen.
        btnLogout.setOnClickListener(v -> {
            UserPrefs.logout(MainActivity.this);

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // Loads the user's latest history item from room and turns it into a friendly summary.
    private void loadRecentActivity(String username) {
        TaskHistory latestHistory = database.taskHistoryDao().getLatestHistoryForUser(username);

        if (latestHistory != null) {
            String utilityLabel = latestHistory.getUtilityUsed();

            if ("hint".equals(utilityLabel)) {
                utilityLabel = "Generate Hint";
            } else if ("explain".equals(utilityLabel)) {
                utilityLabel = "Explain My Answer";
            } else if ("submit".equals(utilityLabel)) {
                utilityLabel = "Submit Task";
            }

            tvRecentActivity.setText("Last topic practised: " + latestHistory.getTopic() + "\nLast action: " + utilityLabel);
        } else {
            tvRecentActivity.setText("No recent activity yet.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        String username = UserPrefs.getUsername(this);
        loadRecentActivity(username);
    }
}