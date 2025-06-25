package com.example.redsocialaio.FirebaseAuthStuff;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.redsocialaio.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

//Clase simple que se encarga de iniciar sesion en firebaseAuth.
public class AccountLogin extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText loginMail, loginPassword;
    private TextView forgotPassword, registerNewUser;
    private FloatingActionButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home);

        mAuth = FirebaseAuth.getInstance();

        loginMail = findViewById(R.id.editTextLoginEmail);
        loginPassword = findViewById(R.id.editTextLoginPassword);
        loginButton = findViewById(R.id.loginButtonToProceed);
        forgotPassword = findViewById(R.id.resetPasswordButton);
        registerNewUser = findViewById(R.id.createAccountButton);
        if (loginButton == null) {
            Toast.makeText(AccountLogin.this, "wawa", Toast.LENGTH_SHORT).show();
        }

        loginButton.setOnClickListener(v -> {
            String email = loginMail.getText().toString().trim();
            String password = loginPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(AccountLogin.this, "Invalid entry", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        registerNewUser.setOnClickListener(v -> {
            startActivity(new Intent(this, AccountCreation.class));
        });

        forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, AccountRecovery.class));
        });
    }
}
