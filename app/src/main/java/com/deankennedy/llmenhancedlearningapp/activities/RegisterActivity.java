package com.deankennedy.llmenhancedlearningapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.deankennedy.llmenhancedlearningapp.R;
import com.deankennedy.llmenhancedlearningapp.utils.UserPrefs;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegisterUsername, etRegisterEmail, etConfirmEmail, etRegisterPassword, etConfirmPassword, etPhoneNumber;
    private Button btnCreateAccount, btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegisterUsername = findViewById(R.id.etRegisterUsername);
        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etConfirmEmail = findViewById(R.id.etConfirmEmail);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnCreateAccount.setOnClickListener(v -> {
            String username = etRegisterUsername.getText().toString().trim();
            String email = etRegisterEmail.getText().toString().trim();
            String confirmEmail = etConfirmEmail.getText().toString().trim();
            String password = etRegisterPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            String phoneNumber = etPhoneNumber.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || confirmEmail.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phoneNumber.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!email.equals(confirmEmail)) {
                Toast.makeText(RegisterActivity.this, "The entered emails do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "The entered passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!email.contains("@")) {
                Toast.makeText(RegisterActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            UserPrefs.registerUser(RegisterActivity.this, username, email, password, phoneNumber);
            UserPrefs.setLoggedIn(RegisterActivity.this, true);
            // Prevents previously saved interests from persisting.
            UserPrefs.clearInterests(RegisterActivity.this);

            Toast.makeText(RegisterActivity.this, "Your account has been created!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(RegisterActivity.this, InterestsActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("email", email);
            intent.putExtra("phoneNumber", phoneNumber);
            startActivity(intent);
            finish();
        });

        btnBackToLogin.setOnClickListener(v -> finish());
    }
}