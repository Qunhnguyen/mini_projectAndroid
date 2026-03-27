package com.example.shopping_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.shopping_app.data.AppDatabase;
import com.example.shopping_app.data.entity.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private EditText etUser, etPass;
    private Button btnLogin;
    private ImageButton btnClose;
    private AppDatabase db;
    private PrefManager pref;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = AppDatabase.getInstance(this);
        pref = new PrefManager(this);

        etUser = findViewById(R.id.et_username);
        etPass = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnClose = findViewById(R.id.btn_login_close);

        btnLogin.setOnClickListener(v -> {
            String u = etUser.getText().toString();
            String p = etPass.getText().toString();

            executor.execute(() -> {
                User user = db.userDao().login(u, p);
                runOnUiThread(() -> {
                    if (user != null) {
                        pref.setUserId(user.getUserId());
                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK); // Trả kết quả thành công về cho màn hình trước
                        finish();
                    } else {
                        Toast.makeText(this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        btnClose.setOnClickListener(v -> finish());
    }
}