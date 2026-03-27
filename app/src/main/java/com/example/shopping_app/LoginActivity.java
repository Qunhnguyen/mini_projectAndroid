package com.example.shopping_app;

import android.os.Bundle;
import android.text.TextUtils;
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
    private static final int MIN_PASSWORD_LENGTH = 6;

    private EditText etUser;
    private EditText etPass;
    private AppDatabase db;
    private PrefManager pref;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = AppDatabase.getInstance(this);
        pref = new PrefManager(this);

        initUi();
    }

    private void initUi() {
        etUser = findViewById(R.id.et_username);
        etPass = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);
        ImageButton btnClose = findViewById(R.id.btn_login_close);

        etUser.setText(AppConstants.DEMO_USERNAME);
        etPass.setText(AppConstants.DEMO_PASSWORD);

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnClose.setOnClickListener(v -> finish());
    }

    private void attemptLogin() {
        String username = readTrimmedText(etUser);
        String password = readTrimmedText(etPass);

        etUser.setError(null);
        etPass.setError(null);

        if (TextUtils.isEmpty(username)) {
            etUser.setError(getString(R.string.error_empty_username));
            etUser.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPass.setError(getString(R.string.error_empty_password));
            etPass.requestFocus();
            return;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            etPass.setError(getString(R.string.error_short_password, MIN_PASSWORD_LENGTH));
            etPass.requestFocus();
            return;
        }

        executor.execute(() -> {
            try {
                User user = db.userDao().login(username, password);
                runOnUiThread(() -> {
                    if (user != null) {
                        handleLoginSuccess(user);
                    } else {
                        showError(getString(R.string.error_invalid_credentials));
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> showError(getString(R.string.error_system, e.getMessage())));
            }
        });
    }

    private String readTrimmedText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void handleLoginSuccess(User user) {
        pref.setUserId(user.getUserId());
        Toast.makeText(
                this,
                getString(R.string.login_success, user.getFullName()),
                Toast.LENGTH_SHORT
        ).show();
        setResult(RESULT_OK);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
