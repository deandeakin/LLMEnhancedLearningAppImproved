package com.deankennedy.llmenhancedlearningapp.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.deankennedy.llmenhancedlearningapp.R;
import com.deankennedy.llmenhancedlearningapp.utils.UserPrefs;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UpgradeActivity extends AppCompatActivity {

    private TextView tvCurrentPlan;
    private Button btnStarterPlan, btnIntermediatePlan, btnAdvancedPlan, btnBackToProfile;

    private PaymentsClient paymentsClient;

    private String pendingPlanName = "";
    private String pendingPrice = "";

    // Modern way to handle payment results and resolution
    private final ActivityResultLauncher<IntentSenderRequest> paymentDataLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(),
                    result -> {
                        int resultCode = result.getResultCode();
                        Intent data = result.getData();
                        handlePaymentResult(resultCode, data);
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);

        tvCurrentPlan = findViewById(R.id.tvCurrentPlan);
        btnStarterPlan = findViewById(R.id.btnStarterPlan);
        btnIntermediatePlan = findViewById(R.id.btnIntermediatePlan);
        btnAdvancedPlan = findViewById(R.id.btnAdvancedPlan);
        btnBackToProfile = findViewById(R.id.btnBackToProfile);

        paymentsClient = createPaymentsClient();

        loadCurrentPlan();
        checkGooglePayAvailability();

        btnStarterPlan.setOnClickListener(v -> confirmStarterPlan());

        btnIntermediatePlan.setOnClickListener(v ->
                confirmPaidPlanSelection("Intermediate", "4.99"));

        btnAdvancedPlan.setOnClickListener(v ->
                confirmPaidPlanSelection("Advanced", "9.99"));

        btnBackToProfile.setOnClickListener(v -> finish());
    }

    private PaymentsClient createPaymentsClient() {
        Wallet.WalletOptions walletOptions = new Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                .build();

        return Wallet.getPaymentsClient(this, walletOptions);
    }

    private void checkGooglePayAvailability() {
        try {
            IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(getIsReadyToPayRequest().toString());

            paymentsClient.isReadyToPay(request)
                    .addOnCompleteListener(task -> {
                        try {
                            Boolean result = task.getResult(ApiException.class);

                            if (result == null || !result) {
                                Toast.makeText(this, "Google Pay is not available on this device.", Toast.LENGTH_LONG).show();
                                btnIntermediatePlan.setEnabled(false);
                                btnAdvancedPlan.setEnabled(false);
                            }
                        } catch (ApiException e) {
                            Toast.makeText(this, "Could not check Google Pay availability.", Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (JSONException e) {
            Toast.makeText(this, "Google Pay request setup failed.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadCurrentPlan() {
        String currentPlan = UserPrefs.getAccountPlan(this);
        tvCurrentPlan.setText(getString(R.string.current_plan_format, currentPlan));
    }

    private void confirmStarterPlan() {
        new AlertDialog.Builder(this)
                .setTitle("Select Starter Plan")
                .setMessage("Would you like to switch to the free Starter plan?")
                .setPositiveButton("Confirm", (dialog, which) -> saveSelectedPlan("Starter"))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmPaidPlanSelection(String planName, String price) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Plan")
                .setMessage("Would you like to purchase the " + planName + " plan for $" + price + "?\n\nThis will open Google Pay in test mode.")
                .setPositiveButton("Continue", (dialog, which) -> launchGooglePay(planName, price))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void launchGooglePay(String planName, String price) {
        pendingPlanName = planName;
        pendingPrice = price;

        try {
            PaymentDataRequest request = PaymentDataRequest.fromJson(getPaymentDataRequest(price).toString());

            if (request != null) {
                // Use the Task directly instead of the old AutoResolveHelper
                paymentsClient.loadPaymentData(request)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                handlePaymentSuccess(task.getResult());
                            } else {
                                Exception exception = task.getException();
                                if (exception instanceof ResolvableApiException) {
                                    ResolvableApiException resolvable = (ResolvableApiException) exception;
                                    IntentSenderRequest intentSenderRequest =
                                            new IntentSenderRequest.Builder(resolvable.getResolution()).build();
                                    paymentDataLauncher.launch(intentSenderRequest);
                                } else {
                                    Toast.makeText(this, "Google Pay error: " + (exception != null ? exception.getMessage() : "unknown"), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Could not create Google Pay request.", Toast.LENGTH_LONG).show();
        }
    }

    private void handlePaymentResult(int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            PaymentData paymentData = PaymentData.getFromIntent(data);
            handlePaymentSuccess(paymentData);
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Google Pay payment cancelled.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Google Pay payment failed.", Toast.LENGTH_LONG).show();
        }
    }

    private void handlePaymentSuccess(@Nullable PaymentData paymentData) {
        if (paymentData != null) {
            saveSelectedPlan(pendingPlanName);
            Toast.makeText(this, "Google Pay test payment successful.", Toast.LENGTH_LONG).show();
        }
    }

    private void saveSelectedPlan(String planName) {
        UserPrefs.saveAccountPlan(this, planName);
        loadCurrentPlan();

        Toast.makeText(this, planName + " plan selected", Toast.LENGTH_SHORT).show();
    }

    private JSONObject getBaseRequest() throws JSONException {
        return new JSONObject()
                .put("apiVersion", 2)
                .put("apiVersionMinor", 0);
    }

    private JSONObject getCardPaymentMethod() throws JSONException {
        JSONObject parameters = new JSONObject()
                .put("allowedAuthMethods", new JSONArray()
                        .put("PAN_ONLY")
                        .put("CRYPTOGRAM_3DS"))
                .put("allowedCardNetworks", new JSONArray()
                        .put("AMEX")
                        .put("DISCOVER")
                        .put("INTERAC")
                        .put("JCB")
                        .put("MASTERCARD")
                        .put("VISA"));

        JSONObject tokenizationSpecification = new JSONObject()
                .put("type", "PAYMENT_GATEWAY")
                .put("parameters", new JSONObject()
                        .put("gateway", "example")
                        .put("gatewayMerchantId", "exampleGatewayMerchantId"));

        return new JSONObject()
                .put("type", "CARD")
                .put("parameters", parameters)
                .put("tokenizationSpecification", tokenizationSpecification);
    }

    private JSONObject getIsReadyToPayRequest() throws JSONException {
        JSONObject request = getBaseRequest();

        JSONObject cardPaymentMethod = new JSONObject()
                .put("type", "CARD")
                .put("parameters", new JSONObject()
                        .put("allowedAuthMethods", new JSONArray()
                                .put("PAN_ONLY")
                                .put("CRYPTOGRAM_3DS"))
                        .put("allowedCardNetworks", new JSONArray()
                                .put("AMEX")
                                .put("DISCOVER")
                                .put("INTERAC")
                                .put("JCB")
                                .put("MASTERCARD")
                                .put("VISA")));

        request.put("allowedPaymentMethods", new JSONArray().put(cardPaymentMethod));

        return request;
    }

    private JSONObject getPaymentDataRequest(String price) throws JSONException {
        JSONObject request = getBaseRequest();

        request.put("allowedPaymentMethods", new JSONArray().put(getCardPaymentMethod()));
        request.put("transactionInfo", getTransactionInfo(price));
        request.put("merchantInfo", new JSONObject()
                .put("merchantName", "LLM Enhanced Learning App"));

        return request;
    }

    private JSONObject getTransactionInfo(String price) throws JSONException {
        return new JSONObject()
                .put("totalPrice", price)
                .put("totalPriceStatus", "FINAL")
                .put("currencyCode", "AUD")
                .put("countryCode", "AU");
    }
}
