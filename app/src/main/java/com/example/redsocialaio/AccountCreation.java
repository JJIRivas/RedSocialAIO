package com.example.redsocialaio;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class AccountCreation extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText createMail, createPassword;
    private TextView createAccount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_gallery);
        mAuth = FirebaseAuth.getInstance();
        createMail = findViewById(R.id.editTextCreateEmailAddress);
        createPassword = findViewById(R.id.editTextCreatePassword);
        createAccount = findViewById(R.id.ConfirmCreation);

        createAccount.setOnClickListener(v -> {
            String email = createMail.getText().toString().trim();
            String password = createPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(AccountCreation.this, "Invalid entry", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AccountCreation.this, "Account created.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AccountCreation.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(AccountCreation.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });


    }
}
