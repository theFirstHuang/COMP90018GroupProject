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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private boolean skipEmailVerification;

    private TextView register,forgotPassword;
    private EditText editTextEmail, editTextPassword;
    private Button signIn;

    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize components
        register = (TextView) findViewById(R.id.register);
        register.setOnClickListener(this);

        forgotPassword = (TextView) findViewById(R.id.forgetPassword);
        forgotPassword.setOnClickListener(this);

        signIn = (Button) findViewById(R.id.signIn);
        signIn.setOnClickListener(this);

        editTextEmail = (EditText) findViewById(R.id.email);
        editTextPassword = (EditText) findViewById(R.id.password);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        skipEmailVerification = true;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.register:
                startActivity(new Intent(this, RegisterUser.class));
                break;
            case R.id.signIn:
                userLogin();
                break;
            case R.id.forgetPassword:
                startActivity(new Intent (this,ForgotPassword.class));
                break;

        }
    }

    private void userLogin () {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty()) {
            editTextEmail.setError("Email is required!");
            editTextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Email is not valid");
            editTextEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            editTextPassword.setError("Password is required!");
            editTextPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Minimum password length required is 6！");
            editTextPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    //you can comment code below to turn off email verification
                    skipEmailVerification = false;
                    if (!skipEmailVerification) {
                        //check if email is been verified
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        if (user.isEmailVerified()) {
                            //redirect to user profile interface
                            Toast.makeText(MainActivity.this,"Successfully logged in!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                            progressBar.setVisibility(View.GONE);
                        } else {
                            user.sendEmailVerification();
                            Toast.makeText(MainActivity.this,"Check your email to verify your account", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    } else {//just login without email verification
                        //redirect to user profile interface
                        Toast.makeText(MainActivity.this,"Successfully logged in!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                        progressBar.setVisibility(View.GONE);
                    }




                } else {
                    Toast.makeText(MainActivity.this,"Failed to Login, please check your credentials", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}