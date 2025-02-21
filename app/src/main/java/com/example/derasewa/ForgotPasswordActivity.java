package com.example.derasewa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class ForgotPasswordActivity extends AppCompatActivity {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    TextView serverResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextInputEditText emailInput = findViewById(R.id.email_input);
        TextInputEditText newPasswordInput = findViewById(R.id.new_password_input);
        MaterialButton changePasswordButton = findViewById(R.id.change_password_button);
        serverResponse = findViewById(R.id.server_response);

        changePasswordButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String newPassword = newPasswordInput.getText().toString();
            forgotPasswordRequest(email, newPassword);
        });
    }

    private void forgotPasswordRequest(String email, String newPassword) {
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("email", email);
        jsonBody.addProperty("newPassword", newPassword);

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("http://"+ BuildConfig.SERVER_IP + ":" + BuildConfig.SERVER_PORT + "/validate-forgot-password")
                .header("x-api-key", BuildConfig.API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(ForgotPasswordActivity.this, "Login request failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
                        intent.putExtra("type", "change-password");
                        intent.putExtra("email", email);
                        intent.putExtra("newPassword", newPassword);
                        startActivity(intent);
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