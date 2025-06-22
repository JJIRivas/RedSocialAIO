package com.example.redsocialaio.FirebaseAuthStuff;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.redsocialaio.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class AccountRecovery extends AppCompatActivity {
    private TextView confirmSendCode;
    private EditText emailForRecovery;
    private FloatingActionButton returnToPreviousScreen;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery);

        confirmSendCode = findViewById(R.id.confirmRecoveryButton);
        emailForRecovery = findViewById(R.id.emailToRecoverPassword);
        returnToPreviousScreen = findViewById(R.id.buttonToGoBack);

        confirmSendCode.setOnClickListener(v -> {
            String email = emailForRecovery.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Sent recovery email", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Invalid Email" + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        returnToPreviousScreen.setOnClickListener(v -> {
            finish();
        });

    }
}
