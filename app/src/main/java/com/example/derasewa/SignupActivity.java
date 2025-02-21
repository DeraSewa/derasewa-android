package com.example.derasewa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
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

public class SignupActivity extends AppCompatActivity {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    TextView serverResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView loginLink = findViewById(R.id.login_link);
        MaterialButton signupButton = findViewById(R.id.signup_button);

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        signupButton.setOnClickListener(v -> {
            TextInputEditText fullNameInput = findViewById(R.id.full_name_input);
            TextInputEditText emailInput = findViewById(R.id.email_input);
            TextInputEditText passwordInput = findViewById(R.id.password_input);
            CheckBox referralCheckBox = findViewById(R.id.referral_checkbox);
            TextInputEditText referralInput = findViewById(R.id.referral_input);

            String fullNameValue = fullNameInput.getText().toString();
            String emailValue = emailInput.getText().toString();
            String passwordValue = passwordInput.getText().toString();
            Boolean usingReferral = referralCheckBox.isChecked();
            String referralValue = referralInput.getText().toString();

            signUpRequest(fullNameValue, emailValue, passwordValue, usingReferral, referralValue);
        });
    }

    private void signUpRequest(String fullName, String email, String password, Boolean usingReferralCode, String referralCode){
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("fullName", fullName);
        jsonBody.addProperty("email", email);
        jsonBody.addProperty("password", password);
        jsonBody.addProperty("usingReferralCode", usingReferralCode);
        jsonBody.addProperty("referralCode", referralCode);

        // Create request body
        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));

        // Build request
        Request request = new Request.Builder()
                .url("http://"+ BuildConfig.SERVER_IP + ":" + BuildConfig.SERVER_PORT + "/validate-account")
                .header("x-api-key", BuildConfig.API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(SignupActivity.this, "Signup request failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(SignupActivity.this, message, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(SignupActivity.this, OtpVerification.class);
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