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

    // Size used when generating the profile QR code bitmap.
    private static final int QR_CODE_SIZE = 800;

    private TextView tvProfileDetails, tvProfileStats;

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
        Button btnViewHistory = findViewById(R.id.btnViewHistory);
        Button btnShareProfile = findViewById(R.id.btnShareProfile);
        Button btnUpgradeAccount = findViewById(R.id.btnUpgradeAccount);
        Button btnBackToHome = findViewById(R.id.btnBackToHome);
        Button btnShowQrCode = findViewById(R.id.btnShowQrCode);

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

    // Loads the saved user profile details and current account plan.
    private void loadProfile() {
        Set<String> interests = UserPrefs.getInterests(this);

        if (interests.isEmpty()) {
            interestsText = "No interests selected";
        } else {
            interestsText = String.join(", ", interests);
        }

        String detailsText = "Username: " + username + "\nEmail: " + email + "\nPhone: " + phoneNumber + "\nInterests: " + interestsText + "\nCurrent Plan: " + UserPrefs.getAccountPlan(this);

        tvProfileDetails.setText(detailsText);
    }

    // Calculates the user's learning stats from submitted task history.
    private void loadLearningStats() {
        List<TaskHistory> historyList = database.taskHistoryDao().getHistoryForUser(username);

        totalQuestionsAnswered = 0;
        correctlyAnswered = 0;
        incorrectlyAnswered = 0;

        // Only final answers are counted.
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

    // Builds the public profile summary for sharing.
    private String buildProfileShareText() {
        return "My Learning Profile\n\n" + "Username: " + username + "\n" + "Interests: " + interestsText + "\n" + "Current Plan: " + UserPrefs.getAccountPlan(this) + "\n" + "Total Questions Answered: " + totalQuestionsAnswered + "\n" + "Correctly Answered: " + correctlyAnswered + "\n" + "Incorrectly Answered: " + incorrectlyAnswered;
    }

    // Opens Android share dialog to share the user's profile.
    private void shareProfile() {
        String shareText = buildProfileShareText();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Learning Profile");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        startActivity(Intent.createChooser(shareIntent, "Share Profile"));
    }

    // Generates and displays a QR code containing the user's profile summary.
    private void showProfileQrCode() {
        String profileText = buildProfileShareText();

        // Runs QR generation on a background thread to keep the UI responsive.
        new Thread(() -> {
            try {
                Bitmap qrBitmap = generateQrCode(profileText);

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

    // Converts profile into a QR code bitmap (black and white).
    private Bitmap generateQrCode(String text) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);

        int[] pixels = new int[QR_CODE_SIZE * QR_CODE_SIZE];
        for (int y = 0; y < QR_CODE_SIZE; y++) {
            int offset = y * QR_CODE_SIZE;
            for (int x = 0; x < QR_CODE_SIZE; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(QR_CODE_SIZE, QR_CODE_SIZE, Bitmap.Config.RGB_565);
        bitmap.setPixels(pixels, 0, QR_CODE_SIZE, 0, 0, QR_CODE_SIZE, QR_CODE_SIZE);
        return bitmap;
    }

    // Refreshes the profile when returning.
    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
        loadLearningStats();
    }
}
