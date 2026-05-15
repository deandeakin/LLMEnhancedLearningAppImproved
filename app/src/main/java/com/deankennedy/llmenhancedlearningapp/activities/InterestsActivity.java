package com.deankennedy.llmenhancedlearningapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.deankennedy.llmenhancedlearningapp.R;
import com.deankennedy.llmenhancedlearningapp.utils.UserPrefs;

import java.util.ArrayList;
import java.util.HashSet;

public class InterestsActivity extends AppCompatActivity {

    private CheckBox cbAlgorithms, cbDataStructures, cbWebDevelopment, cbTesting, cbDatabases, cbNetworks, cbAI, cbCloudComputing, cbCyberSecurity, cbIoT, cbMachineLearning;

    private Button btnSaveInterests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        cbAlgorithms = findViewById(R.id.cbAlgorithms);
        cbDataStructures = findViewById(R.id.cbDataStructures);
        cbWebDevelopment = findViewById(R.id.cbWebDevelopment);
        cbTesting = findViewById(R.id.cbTesting);
        cbDatabases = findViewById(R.id.cbDatabases);
        cbNetworks = findViewById(R.id.cbNetworks);
        cbAI = findViewById(R.id.cbAI);
        cbCloudComputing = findViewById(R.id.cbCloudComputing);
        cbCyberSecurity = findViewById(R.id.cbCyberSecurity);
        cbIoT = findViewById(R.id.cbIoT);
        cbMachineLearning = findViewById(R.id.cbMachineLearning);
        btnSaveInterests = findViewById(R.id.btnSaveInterests);
        btnSaveInterests.setOnClickListener(v -> {
            ArrayList<String> selectedInterests = new ArrayList<>();

            if (cbAlgorithms.isChecked()) {
                selectedInterests.add("Algorithms");
            }
            if (cbDataStructures.isChecked()) {
                selectedInterests.add("Data Structures");
            }
            if (cbWebDevelopment.isChecked()) {
                selectedInterests.add("Web Development");
            }
            if (cbTesting.isChecked()) {
                selectedInterests.add("Software Testing");
            }
            if (cbDatabases.isChecked()) {
                selectedInterests.add("Databases");
            }
            if (cbNetworks.isChecked()) {
                selectedInterests.add("Computer Networks");
            }
            if (cbAI.isChecked()) {
                selectedInterests.add("AI");
            }
            if (cbCloudComputing.isChecked()) {
                selectedInterests.add("Cloud Computing");
            }
            if (cbCyberSecurity.isChecked()) {
                selectedInterests.add("Cyber Security");
            }
            if (cbIoT.isChecked()) {
                selectedInterests.add("IoT");
            }
            if (cbMachineLearning.isChecked()) {
                selectedInterests.add("Machine Learning");
            }

            if (selectedInterests.isEmpty()) {
                Toast.makeText(InterestsActivity.this, "Please select at least one interest", Toast.LENGTH_SHORT).show();
                return;
            }

            String username = getIntent().getStringExtra("username");
            String email = getIntent().getStringExtra("email");
            String phoneNumber = getIntent().getStringExtra("phoneNumber");

            // Save the user's selected interests before moving to the main activity.
            UserPrefs.saveInterests(InterestsActivity.this, new HashSet<>(selectedInterests));

            Intent intent = new Intent(InterestsActivity.this, MainActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("email", email);
            intent.putExtra("phoneNumber", phoneNumber);
            intent.putStringArrayListExtra("interests", selectedInterests);
            startActivity(intent);
            finish();
        });
    }
}
