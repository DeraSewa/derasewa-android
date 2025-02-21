package com.example.derasewa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.chaos.view.PinView;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OtpVerificationActivity extends AppCompatActivity {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    TextView serverResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otp_verification);

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get references to the views
        PinView otpView = findViewById(R.id.otp_view); // PinView for OTP input
        MaterialButton verifyButton = findViewById(R.id.verify_button); // VERIFY button
        TextView infoText = findViewById(R.id.info_text); // Text for displaying information
        serverResponse = findViewById(R.id.server_response);

        infoText.setText("We have sent a OTP code to your email " + getIntent().getStringExtra("email"));

        // Set OnClickListener on the VERIFY button
        verifyButton.setOnClickListener(v -> {
            // Get the OTP entered by the user
            String otp = otpView.getText().toString();
            String type = getIntent().getStringExtra("type");

            if(type.equals("register-account")){
                String fullName = getIntent().getStringExtra("fullName");
                String email = getIntent().getStringExtra("email");
                String password = getIntent().getStringExtra("password");
                Boolean usingReferralCode = getIntent().getBooleanExtra("usingReferralCode", false);
                String referralCode = getIntent().getStringExtra("referralCode");

                registerAccountRequest(fullName, email, password, otp, usingReferralCode, referralCode);
            } else if (type.equals("change-password")) {
                String email = getIntent().getStringExtra("email");
                String newPassword = getIntent().getStringExtra("newPassword");

                changePasswordRequest(email, newPassword, otp);
            }
        });
    }

    private void registerAccountRequest(String fullName, String email, String password,String otp,Boolean usingReferralCode, String referralCode) {
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("fullName", fullName);
        jsonBody.addProperty("email", email);
        jsonBody.addProperty("password", password);
        jsonBody.addProperty("otp", otp);
        jsonBody.addProperty("usingReferralCode", usingReferralCode);
        jsonBody.addProperty("referralCode", referralCode);

        // Create request body
        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));

        // Build request
        Request request = new Request.Builder()
                .url("http://"+ BuildConfig.SERVER_IP + ":" + BuildConfig.SERVER_PORT + "/register-account")
                .header("x-api-key", BuildConfig.API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(OtpVerificationActivity.this, "Login request failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

                String type = jsonResponse.get("type").getAsString();
                String message = jsonResponse.get("message").getAsString();

                runOnUiThread(() -> {
                    if ("success".equals(type)) {
                        // Show success message as Toast
                        Toast.makeText(OtpVerificationActivity.this, message, Toast.LENGTH_SHORT).show();

                        // Redirect to MainActivity
                        Intent intent = new Intent(OtpVerificationActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // If login failed, display message
                        serverResponse.setText(message);
                        serverResponse.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    private void changePasswordRequest(String email, String newPassword, String otp) {
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("email", email);
        jsonBody.addProperty("newPassword", newPassword);
        jsonBody.addProperty("otp", otp);

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("http://"+ BuildConfig.SERVER_IP + ":" + BuildConfig.SERVER_PORT + "/change-password")
                .header("x-api-key", BuildConfig.API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(OtpVerificationActivity.this, "Login request failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

                String type = jsonResponse.get("type").getAsString();
                String message = jsonResponse.get("message").getAsString();

                runOnUiThread(() -> {
                    if ("success".equals(type)) {
                        // Show success message as Toast
                        Toast.makeText(OtpVerificationActivity.this, message, Toast.LENGTH_SHORT).show();

                        // Redirect to MainActivity
                        Intent intent = new Intent(OtpVerificationActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // If login failed, display message
                        serverResponse.setText(message);
                        serverResponse.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }
}
