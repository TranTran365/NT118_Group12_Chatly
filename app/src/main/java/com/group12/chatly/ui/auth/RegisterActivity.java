package com.group12.chatly.ui.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.group12.chatly.R;

public class RegisterActivity extends AppCompatActivity {
    private static final int MIN_PASSWORD_LENGTH = 6;

    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tilName = findViewById(R.id.til_name);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        Button btnSignUp = findViewById(R.id.btn_sign_up);
        TextView tvLoginLink = findViewById(R.id.tv_login_link);

        btnSignUp.setOnClickListener(v -> {
            if (validateInput()) {
                // TODO: gọi AuthRepository đăng ký bằng Firebase
            }
        });

        tvLoginLink.setOnClickListener(v -> {
            // TODO: chuyển sang LoginActivity khi có màn đăng nhập
            finish();
        });
    }

    private boolean validateInput() {
        String name = String.valueOf(etName.getText()).trim();
        String email = String.valueOf(etEmail.getText()).trim();
        String password = String.valueOf(etPassword.getText());
        String confirmPassword = String.valueOf(etConfirmPassword.getText());

        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        boolean valid = true;
        if (name.isEmpty()) {
            tilName.setError("Vui lòng nhập tên");
            valid = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            valid = false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            tilPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            valid = false;
        }
        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Mật khẩu không khớp");
            valid = false;
        }
        return valid;
    }
}