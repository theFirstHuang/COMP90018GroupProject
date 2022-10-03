package com.thefirsthuang.authapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity implements View.OnClickListener{

    private TextView bannerForgotpwd;
    private EditText emailForgotpwd;
    private Button resetPassword;
    private ProgressBar progressBarForgotpwd;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        bannerForgotpwd = (TextView) findViewById(R.id.bannerForgotpwd);
        bannerForgotpwd.setOnClickListener(this);

        emailForgotpwd = (EditText) findViewById(R.id.emailForgotpwd);
        resetPassword = (Button) findViewById(R.id.resetPassword);
        progressBarForgotpwd = (ProgressBar) findViewById(R.id.progressBarForgotpwd);

        auth = FirebaseAuth.getInstance();

        resetPassword.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bannerForgotpwd:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.resetPassword:
                resetPassword();
                break;
        }
    }


    private void resetPassword() {
        String email = emailForgotpwd.getText().toString().trim();

        if (email.isEmpty()) {
            emailForgotpwd.setError("Email is required!");
            emailForgotpwd.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailForgotpwd.setError("Email is not valid");
            emailForgotpwd.requestFocus();
            return;
        }

        progressBarForgotpwd.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ForgotPassword.this,"Check your email to rest pwd!",Toast.LENGTH_LONG).show();
                    progressBarForgotpwd.setVisibility(View.GONE);
                    startActivity(new Intent(ForgotPassword.this, MainActivity.class));
                } else {
                    Toast.makeText(ForgotPassword.this,"Reset failed, try again!",Toast.LENGTH_LONG).show();
                    progressBarForgotpwd.setVisibility(View.GONE);
                }
            }
        });

    }


}