package com.deankennedy.llmenhancedlearningapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deankennedy.llmenhancedlearningapp.R;
import com.deankennedy.llmenhancedlearningapp.adapters.TaskHistoryAdapter;
import com.deankennedy.llmenhancedlearningapp.data.AppDatabase;
import com.deankennedy.llmenhancedlearningapp.data.TaskHistory;
import com.deankennedy.llmenhancedlearningapp.utils.UserPrefs;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private TextView tvEmptyHistory;
    private Button btnBackToProfile;

    private AppDatabase database;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rvHistory = findViewById(R.id.rvHistory);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        btnBackToProfile = findViewById(R.id.btnBackToProfile);

        database = AppDatabase.getInstance(this);
        username = UserPrefs.getUsername(this);

        loadHistory();

        btnBackToProfile.setOnClickListener(v -> finish());
    }

    private void loadHistory() {
        List<TaskHistory> allHistory = database.taskHistoryDao().getHistoryForUser(username);
        List<TaskHistory> submittedHistory = new ArrayList<>();

        for (TaskHistory history : allHistory) {
            if ("submit".equals(history.getUtilityUsed())) {
                submittedHistory.add(history);
            }
        }

        if (submittedHistory.isEmpty()) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
        } else {
            tvEmptyHistory.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);

            TaskHistoryAdapter adapter = new TaskHistoryAdapter(submittedHistory);
            rvHistory.setLayoutManager(new LinearLayoutManager(this));
            rvHistory.setAdapter(adapter);
        }
    }
}