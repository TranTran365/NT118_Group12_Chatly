package com.group12.chatly.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.group12.chatly.MainActivity;
import com.group12.chatly.R;
import com.group12.chatly.viewmodel.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {
    private static final int MIN_PASSWORD_LENGTH = 8;

    private TextInputLayout tilFullName, tilEmail, tilPassword;
    private TextInputEditText etFullName, etEmail, etPassword;
    private CheckBox cbTerms;
    private Button btnCreateAccount;

    private AuthViewModel authViewModel;
    private CredentialManager credentialManager;
    private View btnGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        credentialManager = CredentialManager.create(this);

        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbTerms = findViewById(R.id.cbTerms);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnGoogle = findViewById(R.id.btnGoogle);
        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> startGoogleSignIn());
        }

        TextView tvSignIn = findViewById(R.id.tvSignIn);
        ImageButton btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnCreateAccount != null) {
            btnCreateAccount.setOnClickListener(v -> {
                if (validateInput()) {
                    String name = String.valueOf(etFullName.getText()).trim();
                    String email = String.valueOf(etEmail.getText()).trim();
                    String password = String.valueOf(etPassword.getText());
                    authViewModel.register(name, email, password);
                }
            });
        }

        if (tvSignIn != null) {
            tvSignIn.setOnClickListener(v -> {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        }

        setupObservers();
    }

    private void startGoogleSignIn() {
        GetSignInWithGoogleOption googleOption =
                new GetSignInWithGoogleOption.Builder(getString(R.string.default_web_client_id)).build();
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build();

        credentialManager.getCredentialAsync(
                this, request, new CancellationSignal(), ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        Credential credential = result.getCredential();
                        if (credential instanceof CustomCredential
                                && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                        .equals(credential.getType())) {
                            try {
                                GoogleIdTokenCredential google = GoogleIdTokenCredential
                                        .createFrom(((CustomCredential) credential).getData());
                                authViewModel.loginWithGoogleIdToken(google.getIdToken());
                            } catch (Exception e) {
                                Toast.makeText(RegisterActivity.this,
                                        "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        if (e instanceof GetCredentialCancellationException) return;
                        Toast.makeText(RegisterActivity.this,
                                "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setupObservers() {
        authViewModel.getRegisterState().observe(this, state -> {
            if (state == null || btnCreateAccount == null) return;

            if (state.isLoading()) {
                btnCreateAccount.setEnabled(false);
                btnCreateAccount.setText(R.string.register_creating_account);
            } else if (state.isSuccess()) {
                btnCreateAccount.setEnabled(true);
                btnCreateAccount.setText(R.string.register_create_account);
                Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (state.isError()) {
                btnCreateAccount.setEnabled(true);
                btnCreateAccount.setText(R.string.register_create_account);
                Toast.makeText(this, state.getErrorMessage(), Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.getLoginState().observe(this, state -> {
            if (state == null) return;
            if (state.isLoading()) {
                if (btnGoogle != null) btnGoogle.setEnabled(false);
            } else if (state.isSuccess()) {
                if (btnGoogle != null) btnGoogle.setEnabled(true);
                Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (state.isError()) {
                if (btnGoogle != null) btnGoogle.setEnabled(true);
                Toast.makeText(this, state.getErrorMessage(), Toast.LENGTH_LONG).show();
            }
        });
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
