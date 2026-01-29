package com.example.filmbox_front;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink; // Assuming there's a TextView to link to registration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // Corrected layout file

        etUsername = findViewById(R.id.name_input); // Corrected ID for username EditText
        etPassword = findViewById(R.id.password_input); // Corrected ID for password EditText
        btnLogin = findViewById(R.id.login_button);     // Corrected ID for login Button
        tvRegisterLink = findViewById(R.id.has_account_text); // Assuming this is the register link TextView

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Por favor, ingresa usuario y contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }

                LoginRequest loginRequest = new LoginRequest(username, password);
                ApiService apiService = RetrofitClient.getApiService();

                Call<LoginResponse> call = apiService.loginUser(loginRequest);
                call.enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse loginResponse = response.body();
                            String token = loginResponse.getToken();

                            // Save token to SharedPreferences
                            SharedPreferences sharedPref = getSharedPreferences("FilmBoxPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("AuthToken", token);
                            editor.putString("Username", loginResponse.getUsername()); // Save username too
                            editor.apply();

                            Toast.makeText(LoginActivity.this, loginResponse.getDetail(), Toast.LENGTH_SHORT).show();

                            // Navigate to main activity (e.g., MainActivity)
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class); // Assuming MainActivity is your main screen
                            startActivity(intent);
                            finish(); // Close LoginActivity so user can't go back with back button

                        } else {
                            String errorMessage = "Error en el login. Credenciales inválidas.";
                            if (response.errorBody() != null) {
                                try {
                                    // Attempt to parse error body if backend sends structured error
                                    // For simplicity, just use a generic message for now
                                    errorMessage = "Error: " + response.code() + " - " + response.message();
                                } catch (Exception e) {
                                    Log.e("LoginActivity", "Error parsing error body", e);
                                }
                            }
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        Log.e("LoginActivity", "Error de red: " + t.getMessage(), t);
                        Toast.makeText(LoginActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        // Optional: Set listener for registration link
        if (tvRegisterLink != null) {
            tvRegisterLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to registration activity
                    Intent intent = new Intent(LoginActivity.this, UserRegistration.class); // Assuming UserRegistration is your registration activity
                    startActivity(intent);
                }
            });
        }
    }
}
