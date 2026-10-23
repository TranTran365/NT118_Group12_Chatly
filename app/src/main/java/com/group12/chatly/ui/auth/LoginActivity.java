package com.group12.chatly.ui.auth;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.group12.chatly.MainActivity;
import com.group12.chatly.R;
import com.group12.chatly.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private Button btnSignIn;

    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnSignIn = findViewById(R.id.btn_sign_in);

        TextView tvCreateAccount = findViewById(R.id.tv_create_account);
        colorBoldSpans(tvCreateAccount, ContextCompat.getColor(this, R.color.register_blue));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.tv_forgot_password).setOnClickListener(v -> {
            // TODO: mở ForgotPasswordActivity khi có màn này
            Toast.makeText(this, "Forgot password feature is coming soon.", Toast.LENGTH_SHORT).show();
        });

        if (btnSignIn != null) {
            btnSignIn.setOnClickListener(v -> {
                if (validateInput()) {
                    String email = String.valueOf(etEmail.getText()).trim();
                    String password = String.valueOf(etPassword.getText());
                    authViewModel.login(email, password);
                }
            });
        }

        findViewById(R.id.btn_google).setOnClickListener(v -> {
            // TODO: đăng nhập bằng Google
            Toast.makeText(this, "Google Sign-In is coming soon.", Toast.LENGTH_SHORT).show();
        });

        tvCreateAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        setupObservers();
    }

    private void setupObservers() {
        authViewModel.getLoginState().observe(this, state -> {
            if (state == null || btnSignIn == null) return;

            if (state.isLoading()) {
                btnSignIn.setEnabled(false);
                btnSignIn.setText(R.string.login_signing_in);
            } else if (state.isSuccess()) {
                btnSignIn.setEnabled(true);
                btnSignIn.setText(R.string.login_sign_in);
                Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (state.isError()) {
                btnSignIn.setEnabled(true);
                btnSignIn.setText(R.string.login_sign_in);
                Toast.makeText(this, state.getErrorMessage(), Toast.LENGTH_LONG).show();
            }
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
        if (tv == null) return;
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
