package com.group12.chatly.ui.auth;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Patterns;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.group12.chatly.R;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

        TextView tvCreateAccount = findViewById(R.id.tv_create_account);
        colorBoldSpans(tvCreateAccount, ContextCompat.getColor(this, R.color.register_blue));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.tv_forgot_password).setOnClickListener(v -> {
            // TODO: mở ForgotPasswordActivity khi có màn này
        });

        findViewById(R.id.btn_sign_in).setOnClickListener(v -> {
            if (validateInput()) {
                // TODO: gọi AuthRepository đăng nhập bằng Firebase
            }
        });

        findViewById(R.id.btn_google).setOnClickListener(v -> {
            // TODO: đăng nhập bằng Google
        });

        tvCreateAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    private boolean validateInput() {
        String email = String.valueOf(etEmail.getText()).trim();
        String password = String.valueOf(etPassword.getText());

        tilEmail.setError(null);
        tilPassword.setError(null);

        boolean valid = true;
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.register_error_email));
            valid = false;
        }
        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.login_error_password));
            valid = false;
        }
        return valid;
    }


    private void colorBoldSpans(TextView tv, int color) {
        SpannableString s = new SpannableString(tv.getText());
        for (StyleSpan span : s.getSpans(0, s.length(), StyleSpan.class)) {
            if (span.getStyle() == Typeface.BOLD) {
                s.setSpan(new ForegroundColorSpan(color),
                        s.getSpanStart(span), s.getSpanEnd(span),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        tv.setText(s);
    }
}