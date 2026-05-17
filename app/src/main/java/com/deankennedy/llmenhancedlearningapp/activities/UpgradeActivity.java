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

    private PaymentsClient paymentsClient;

    // Stores the selected paid plan while the Google Pay flow is running.
    private String pendingPlanName = "";

    // Handles the result returned from the Google Pay.
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
        Button btnStarterPlan = findViewById(R.id.btnStarterPlan);
        Button btnIntermediatePlan = findViewById(R.id.btnIntermediatePlan);
        Button btnAdvancedPlan = findViewById(R.id.btnAdvancedPlan);
        Button btnBackToProfile = findViewById(R.id.btnBackToProfile);

        paymentsClient = createPaymentsClient();

        loadCurrentPlan();
        checkGooglePayAvailability(btnIntermediatePlan, btnAdvancedPlan);

        btnStarterPlan.setOnClickListener(v -> confirmStarterPlan());

        btnIntermediatePlan.setOnClickListener(v ->
                confirmPaidPlanSelection("Intermediate", "4.99"));

        btnAdvancedPlan.setOnClickListener(v ->
                confirmPaidPlanSelection("Advanced", "9.99"));

        btnBackToProfile.setOnClickListener(v -> finish());
    }

    // Creates the Google Play client in TEST mode.
    private PaymentsClient createPaymentsClient() {
        Wallet.WalletOptions walletOptions = new Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                .build();

        return Wallet.getPaymentsClient(this, walletOptions);
    }

    // Checks if Google Pay is available.
    private void checkGooglePayAvailability(Button btnIntermediate, Button btnAdvanced) {
        try {
            IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(getIsReadyToPayRequest().toString());

            if (request == null) {
                Toast.makeText(this, R.string.google_pay_setup_failed, Toast.LENGTH_LONG).show();
                btnIntermediate.setEnabled(false);
                btnAdvanced.setEnabled(false);
                return;
            }

            paymentsClient.isReadyToPay(request)
                    .addOnCompleteListener(task -> {
                        try {
                            Boolean result = task.getResult(ApiException.class);

                            if (result == null || !result) {
                                Toast.makeText(this, R.string.google_pay_not_available, Toast.LENGTH_LONG).show();
                                btnIntermediate.setEnabled(false);
                                btnAdvanced.setEnabled(false);
                            }
                        } catch (ApiException e) {
                            Toast.makeText(this, "Could not check Google Pay availability.", Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (JSONException e) {
            Toast.makeText(this, R.string.google_pay_setup_failed, Toast.LENGTH_LONG).show();
        }
    }

    // Displays the user's current account plan.
    private void loadCurrentPlan() {
        String currentPlan = UserPrefs.getAccountPlan(this);
        tvCurrentPlan.setText(getString(R.string.current_plan_format, currentPlan));
    }

    // Method for switching to the free starter plan.
    private void confirmStarterPlan() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.select_starter_plan)
                .setMessage(R.string.starter_plan_message)
                .setPositiveButton(R.string.confirm, (dialog, which) -> saveSelectedPlan("Starter"))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // Method for switching to a paid plan.
    private void confirmPaidPlanSelection(String planName, String price) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_plan)
                .setMessage(getString(R.string.confirm_paid_plan_message, planName, price))
                .setPositiveButton(R.string.continue_button, (dialog, which) -> launchGooglePay(planName, price))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // Builds the Google Pay request and launches the test payment flow.
    private void launchGooglePay(String planName, String price) {
        pendingPlanName = planName;

        try {
            PaymentDataRequest request = PaymentDataRequest.fromJson(getPaymentDataRequest(price).toString());

            if (request == null) {
                Toast.makeText(this, R.string.google_pay_request_error, Toast.LENGTH_LONG).show();
                return;
            }

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
                                String errorMsg = exception != null ? exception.getMessage() : getString(R.string.unknown_error);
                                Toast.makeText(this, getString(R.string.google_pay_error_format, errorMsg), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } catch (JSONException e) {
            Toast.makeText(this, R.string.google_pay_request_error, Toast.LENGTH_LONG).show();
        }
    }

    // Handles the result  after the user completes or exits the Google Pay sheet.
    private void handlePaymentResult(int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            PaymentData paymentData = PaymentData.getFromIntent(data);
            handlePaymentSuccess(paymentData);
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, R.string.payment_cancelled, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.payment_failed, Toast.LENGTH_LONG).show();
        }
    }

    // Applies the selected plan after a successful Google Pay test payment.
    private void handlePaymentSuccess(@Nullable PaymentData paymentData) {
        if (paymentData != null) {
            saveSelectedPlan(pendingPlanName);
            Toast.makeText(this, R.string.payment_success, Toast.LENGTH_LONG).show();
        }
    }

    // Saves the selected account plan locally and refreshes the screen.
    private void saveSelectedPlan(String planName) {
        UserPrefs.saveAccountPlan(this, planName);
        loadCurrentPlan();

        Toast.makeText(this, getString(R.string.plan_selected_format, planName), Toast.LENGTH_SHORT).show();
    }

    // Base Google Pay requests information shareb by payment requestion objects.
    private JSONObject getBaseRequest() throws JSONException {
        return new JSONObject()
                .put("apiVersion", 2)
                .put("apiVersionMinor", 0);
    }

    // Defines the accepted card payment method and test gateway details.
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

    // Builds the request used to check if Google Pay is ready/available.
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

    // Builds the full payment request for the selected plan.
    private JSONObject getPaymentDataRequest(String price) throws JSONException {
        JSONObject request = getBaseRequest();
        request.put("allowedPaymentMethods", new JSONArray().put(getCardPaymentMethod()));
        request.put("transactionInfo", getTransactionInfo(price));
        request.put("merchantInfo", new JSONObject().put("merchantName", "LLM Enhanced Learning App"));
        return request;
    }

    // Adds the transaction amount, currency, and country to the payment request.
    private JSONObject getTransactionInfo(String price) throws JSONException {
        return new JSONObject().put("totalPrice", price)
                .put("totalPriceStatus", "FINAL")
                .put("currencyCode", "AUD")
                .put("countryCode", "AU");
    }
}
