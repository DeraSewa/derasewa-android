package com.example.derasewa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    TextView serverResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        serverResponse = findViewById(R.id.server_response);
        TextInputEditText emailInput = findViewById(R.id.email_input);
        TextInputEditText passwordInput = findViewById(R.id.password_input);
        MaterialButton loginButton = findViewById(R.id.login_button);
        TextView signUpLink = findViewById(R.id.signup_link);
        TextView forgotPasswordLink = findViewById(R.id.forgot_password_link);

        forgotPasswordLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> {
            String emailValue = emailInput.getText().toString();
            String passwordValue = passwordInput.getText().toString();

            loginRequest(emailValue, passwordValue);
        });

        signUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void loginRequest(String email, String password) {
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("email", email);
        jsonBody.addProperty("password", password);

        // Create request body
        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));

        // Build request
        Request request = new Request.Builder()
                .url("http://"+ BuildConfig.SERVER_IP + ":" + BuildConfig.SERVER_PORT + "/login")
                .header("x-api-key", BuildConfig.API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Login request failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        String jwtToken = jsonResponse.get("payload").getAsString();

                        // Save user login state
                        SharedPreferences sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putBoolean("isFirstTime", false);
                        editor.putString("jwtToken", jwtToken);
                        editor.apply();

                        // Show success message as Toast
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();

                        // Redirect to MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
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
