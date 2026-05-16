package com.deankennedy.llmenhancedlearningapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;

import androidx.appcompat.app.AppCompatActivity;

import com.deankennedy.llmenhancedlearningapp.R;
import com.deankennedy.llmenhancedlearningapp.data.AppDatabase;
import com.deankennedy.llmenhancedlearningapp.data.TaskHistory;
import com.deankennedy.llmenhancedlearningapp.utils.UserPrefs;

import java.util.List;
import java.util.Set;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileDetails, tvProfileStats;
    private Button btnViewHistory, btnShareProfile, btnUpgradeAccount, btnBackToHome, btnShowQrCode;

    private AppDatabase database;

    private String username, email, phoneNumber;
    private String interestsText = "";
    private int totalQuestionsAnswered = 0;
    private int correctlyAnswered = 0;
    private int incorrectlyAnswered = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvProfileDetails = findViewById(R.id.tvProfileDetails);
        tvProfileStats = findViewById(R.id.tvProfileStats);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnShareProfile = findViewById(R.id.btnShareProfile);
        btnUpgradeAccount = findViewById(R.id.btnUpgradeAccount);
        btnBackToHome = findViewById(R.id.btnBackToHome);
        btnShowQrCode = findViewById(R.id.btnShowQrCode);

        database = AppDatabase.getInstance(this);

        username = UserPrefs.getUsername(this);
        email = UserPrefs.getEmail(this);
        phoneNumber = UserPrefs.getPhone(this);

        loadProfile();
        loadLearningStats();

        btnViewHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        btnShareProfile.setOnClickListener(v -> shareProfile());

        btnShowQrCode.setOnClickListener(v -> showProfileQrCode());

        btnUpgradeAccount.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, UpgradeActivity.class);
            startActivity(intent);
        });

        btnBackToHome.setOnClickListener(v -> finish());
    }

    private void loadProfile() {
        Set<String> interests = UserPrefs.getInterests(this);
        interestsText = String.join(", ", interests);

        String detailsText = "Username: " + username + "\nEmail: " + email + "\nPhone: " + phoneNumber + "\nInterests: " + interestsText + "\nCurrent Plan: " + UserPrefs.getAccountPlan(this);

        tvProfileDetails.setText(detailsText);
    }

    private void loadLearningStats() {
        List<TaskHistory> historyList = database.taskHistoryDao().getHistoryForUser(username);

        totalQuestionsAnswered = 0;
        correctlyAnswered = 0;
        incorrectlyAnswered = 0;

        for (TaskHistory history : historyList) {
            if ("submit".equals(history.getUtilityUsed())) {
                String selectedAnswer = history.getSelectedAnswer();
                String correctAnswer = history.getCorrectAnswer();

                if (selectedAnswer != null && correctAnswer != null) {
                    totalQuestionsAnswered++;

                    if (selectedAnswer.trim().equals(correctAnswer.trim())) {
                        correctlyAnswered++;
                    } else {
                        incorrectlyAnswered++;
                    }
                }
            }
        }

        String statsText = "Total Questions Answered: " + totalQuestionsAnswered + "\nCorrectly Answered: " + correctlyAnswered + "\nIncorrectly Answered: " + incorrectlyAnswered;

        tvProfileStats.setText(statsText);
    }

    private String buildProfileShareText() {
        return "My Learning Profile\n\n" + "Username: " + username + "\n" + "Interests: " + interestsText + "\n" + "Current Plan: " + UserPrefs.getAccountPlan(this) + "\n" + "Total Questions Answered: " + totalQuestionsAnswered + "\n" + "Correctly Answered: " + correctlyAnswered + "\n" + "Incorrectly Answered: " + incorrectlyAnswered;
    }

    private void shareProfile() {
        String shareText = buildProfileShareText();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Learning Profile");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        startActivity(Intent.createChooser(shareIntent, "Share Profile"));
    }

    private void showProfileQrCode() {
        String profileText = buildProfileShareText();

        // Run on background thread to keep UI responsive
        new Thread(() -> {
            try {
                Bitmap qrBitmap = generateQrCode(profileText, 800, 800);

                runOnUiThread(() -> {
                    ImageView imageView = new ImageView(this);
                    imageView.setImageBitmap(qrBitmap);
                    imageView.setAdjustViewBounds(true);
                    imageView.setPadding(32, 32, 32, 32);

                    new AlertDialog.Builder(this)
                            .setTitle(R.string.qr_code_title)
                            .setView(imageView)
                            .setPositiveButton(R.string.close, null)
                            .show();
                });

            } catch (WriterException e) {
                runOnUiThread(() -> new AlertDialog.Builder(this)
                        .setTitle(R.string.qr_code_error_title)
                        .setMessage(R.string.qr_code_error_msg)
                        .setPositiveButton(android.R.string.ok, null)
                        .show());
            }
        }).start();
    }

    private Bitmap generateQrCode(String text, int width, int height) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height);

        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
        loadLearningStats();
    }
}
