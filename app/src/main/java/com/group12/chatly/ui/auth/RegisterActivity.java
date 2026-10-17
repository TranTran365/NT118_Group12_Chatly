package com.group12.chatly.ui.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.group12.chatly.R;

public class RegisterActivity extends AppCompatActivity {
    private static final int MIN_PASSWORD_LENGTH = 8;

    private TextInputLayout tilFullName, tilEmail, tilPassword;
    private TextInputEditText etFullName, etEmail, etPassword;
    private CheckBox cbTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbTerms = findViewById(R.id.cbTerms);

        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        TextView tvSignIn = findViewById(R.id.tvSignIn);
        ImageButton btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnCreateAccount != null) {
            btnCreateAccount.setOnClickListener(v -> {
                if (validateInput()) {
                    // TODO: gọi AuthRepository đăng ký bằng Firebase
                }
            });
        }

        if (tvSignIn != null) {
            tvSignIn.setOnClickListener(v -> {
                // TODO: chuyển sang LoginActivity khi có màn đăng nhập
                finish();
            });
        }
    }

    private boolean validateInput() {
        String name = String.valueOf(etFullName.getText()).trim();
        String email = String.valueOf(etEmail.getText()).trim();
        String password = String.valueOf(etPassword.getText());

        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);

        boolean valid = true;
        if (name.isEmpty()) {
            tilFullName.setError(getString(R.string.register_error_name));
            valid = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.register_error_email));
            valid = false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH || !password.matches(".*\\d.*")) {
            tilPassword.setError(getString(R.string.register_error_password));
            valid = false;
        }
        if (cbTerms != null && !cbTerms.isChecked()) {
            Toast.makeText(this, R.string.register_error_terms, Toast.LENGTH_SHORT).show();
            valid = false;
        }
        return valid;
    }
}
