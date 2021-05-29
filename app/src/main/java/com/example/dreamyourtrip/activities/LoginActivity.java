package com.example.dreamyourtrip.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dreamyourtrip.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener,
        View.OnTouchListener {

    private FirebaseAuth mAuth;

    private EditText    etEmail, etPassword;
    private ImageButton imgBtnShowPassword;
    private TextView    tvLoginError;
    private Button      btnSignIn, btnRegister;
    private ProgressBar pbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        if (isUser()) {
            startActivity(new Intent(LoginActivity.this, MapsActivity.class));
        }

        initViews();
    }

    private boolean isUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null)
            return false;
        else
            return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().equals("") && !etPassword.getText().toString().trim().equals("")) {
                    btnSignIn.setEnabled(true);
                    btnRegister.setEnabled(true);
                } else {
                    btnSignIn.setEnabled(false);
                    btnRegister.setEnabled(false);
                }
            }
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().equals("") && !etEmail.getText().toString().trim().equals("")) {
                    btnSignIn.setEnabled(true);
                    btnRegister.setEnabled(true);
                } else {
                    btnSignIn.setEnabled(false);
                    btnRegister.setEnabled(false);
                }
            }
        });

        imgBtnShowPassword = findViewById(R.id.img_btn_show_password);
        imgBtnShowPassword.setOnTouchListener(this);

        tvLoginError = findViewById(R.id.tv_login_error);

        btnSignIn = findViewById(R.id.btn_sign_in);
        btnSignIn.setOnClickListener(this);

        btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(this);

        pbLoading = findViewById(R.id.pb_loading);
    }

    void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            Log.d("SIGN IN", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user.isEmailVerified()) {
                                Log.d("EMAIL VERIFIED", "signInWithEmailVerified:success");
                                startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                            } else {
                                tvLoginError.setText("Verify your email!");
                                Log.d("EMAIL VERIFIED", "signInWithEmailVerified:failed");
                            }


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SIGN IN", "signInWithEmail:failure", task.getException());

                            tvLoginError.setText(R.string.auth_failed);
                            etPassword.setText("");
                        }
                    }
                });
    }

    void createUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("REGISTER", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            sendVerificationEmail();

                            mAuth.signOut();
                            tvLoginError.setText("Verify your account, check your mailbox");
                        } else {
                            Log.w("REGISTER", "createUserWithEmail:failure", task.getException());

                            tvLoginError.setText("Email is in use");
                        }
                    }
                });
    }

    private void sendVerificationEmail() {
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d("EMAIL SEND", "sendEmailVerification:success");
                        } else {
                            Log.w("EMAIL SEND", "sendEmailVerification:failure");
                        }
                    }
                });
    }


    @Override
    public void onClick(View v) {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (v == btnSignIn) {
            pbLoading.setVisibility(View.VISIBLE);
            signIn(email, password);
            pbLoading.setVisibility(View.GONE);
        } else if (v == btnRegister) {
            pbLoading.setVisibility(View.VISIBLE);
            createUser(email, password);
            pbLoading.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == imgBtnShowPassword) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    break;
                case MotionEvent.ACTION_UP:
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    break;
            }
            return true;
        }

        return false;
    }
}
