package com.example.redsocialaio.core.checkers;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.redsocialaio.R;
import com.example.redsocialaio.core.auth.AuthManager;
import com.example.redsocialaio.core.auth.ValidationCallBack;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

public class InstanceValidation extends AppCompatActivity {

    private EditText instanceURL;
    private FloatingActionButton sendButton;
    private TextView validInstance, invalidInstance;

    private String instanceToCheck;


    private InstanceValidator.PlatformType selectedPlatform;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_instance);


        String platformName = getIntent().getStringExtra("platform");
        if (platformName == null) {
            Toast.makeText(this, "No se especificó la plataforma", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        selectedPlatform = InstanceValidator.PlatformType.valueOf(platformName);


        instanceURL = findViewById(R.id.editInstance);
        sendButton = findViewById(R.id.proceedButton);
        validInstance = findViewById(R.id.todoOk);
        invalidInstance = findViewById(R.id.AlgoMal);

        sendButton.setOnClickListener(v -> {
            instanceToCheck = instanceURL.getText().toString().trim();

            if (instanceToCheck.isEmpty()) {
                Toast.makeText(this, "Campo vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            InstanceValidator.validateInstance(instanceToCheck, selectedPlatform, new ValidationCallBack() {
                @Override
                public void onValidInstance(JSONObject meta) {
                    runOnUiThread(() -> {
                        validInstance.setVisibility(View.VISIBLE);
                        invalidInstance.setVisibility(View.GONE);

                        AuthManager authManager = AuthManager.AuthManagerFactory.create(
                                selectedPlatform, InstanceValidation.this
                        );
                        authManager.startAuthFlow(instanceToCheck);
                    });
                }

                @Override
                public void onInvalidInstance(String reason) {
                    runOnUiThread(() -> {
                        invalidInstance.setVisibility(View.VISIBLE);
                        validInstance.setVisibility(View.GONE);
                        Toast.makeText(InstanceValidation.this, "Error: " + reason, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }
}


