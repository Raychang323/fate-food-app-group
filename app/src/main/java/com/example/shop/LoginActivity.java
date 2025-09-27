package com.example.shop;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextUserId, editTextPassword;
    private Button btnLogin;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUserId = findViewById(R.id.editTextUserId);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.btnLogin);

        apiService = ApiClient.getClient().create(ApiService.class);

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String userid = editTextUserId.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        Call<Map<String, Object>> call = apiService.login(userid, password);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();
                    if ("ok".equals(result.get("status"))) {
                        Toast.makeText(LoginActivity.this, "歡迎 " + result.get("username"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "登入失敗: " + result.get("message"), Toast.LENGTH_SHORT).show();
                        System.out.println(result.get("message"));
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "伺服器回應錯誤", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "連線失敗: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

