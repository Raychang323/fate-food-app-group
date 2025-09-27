package com.example.shop;


import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;
import retrofit2.*;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextUserId, editTextPassword, editTextEmail, editTextUsername, editTextPhone;
    private Spinner spinnerRole;
    private Button btnRegister;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextUserId = findViewById(R.id.editTextUserId);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPhone = findViewById(R.id.editTextPhone);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnRegister = findViewById(R.id.btnRegister);

        // Spinner 選項
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"general", "merchant"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);

        btnRegister.setOnClickListener(v -> register());
    }

    private void register() {

        String userid = editTextUserId.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();

        // --- 欄位檢查 ---
        if (userid.isEmpty()) {
            editTextUserId.setError("請輸入帳號");
            editTextUserId.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("請輸入密碼");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("密碼至少6個字元");
            editTextPassword.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("請輸入Email");
            editTextEmail.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            editTextUsername.setError("請輸入使用者名稱");
            editTextUsername.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            editTextPhone.setError("請輸入電話號碼");
            editTextPhone.requestFocus();
            return;
        }

        // --- 呼叫 API ---
        Call<Map<String, Object>> call = apiService.register(userid, password, email, username, phone, role);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();
                    if ("ok".equals(result.get("status"))) {
                        Toast.makeText(RegisterActivity.this, "註冊成功！", Toast.LENGTH_SHORT).show();
                        finish(); // 回到登入頁
                    } else {
                        Toast.makeText(RegisterActivity.this, "註冊失敗: " + result.get("message"), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "伺服器錯誤", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "連線失敗: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
