package com.example.shopping_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.shopping_app.data.AppDatabase;
import com.example.shopping_app.data.entity.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity xử lý đăng nhập người dùng.
 * Hỗ trợ xác thực từ database và quản lý trạng thái qua SharedPreferences.
 */
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

        initUI();
    }

    private void initUI() {
        etUser = findViewById(R.id.et_username);
        etPass = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnClose = findViewById(R.id.btn_login_close);

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnClose.setOnClickListener(v -> finish());
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu đầu vào và thực hiện đăng nhập
     */
    private void attemptLogin() {
        String username = etUser.getText().toString().trim();
        String password = etPass.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUser.setError("Vui lòng nhập tên đăng nhập");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPass.setError("Vui lòng nhập mật khẩu");
            return;
        }

        // Thực hiện truy vấn database trên luồng nền
        executor.execute(() -> {
            try {
                User user = db.userDao().login(username, password);
                runOnUiThread(() -> {
                    if (user != null) {
                        handleLoginSuccess(user);
                    } else {
                        showError("Tài khoản hoặc mật khẩu không chính xác!");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> showError("Lỗi hệ thống: " + e.getMessage()));
            }
        });
    }

    private void handleLoginSuccess(User user) {
        pref.setUserId(user.getUserId());
        Toast.makeText(this, "Chào mừng " + user.getFullName() + " đã quay trở lại!", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
