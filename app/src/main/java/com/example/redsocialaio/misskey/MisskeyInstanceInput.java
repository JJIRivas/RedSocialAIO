package com.example.redsocialaio.misskey;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.redsocialaio.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

public class MisskeyInstanceInput extends AppCompatActivity {

    private EditText instanceURL;
    private FloatingActionButton sendButton;
    private String instanceToCheck;
    private TextView validInstance, InvalidInstance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_misskey_ask_instance);

        instanceURL = findViewById(R.id.editInstance);
        sendButton = findViewById(R.id.proceedButton);
        validInstance = findViewById(R.id.todoOk);
        InvalidInstance = findViewById(R.id.AlgoMal);


        sendButton.setOnClickListener(v -> {

            instanceToCheck = instanceURL.getText().toString().trim();

            if (instanceToCheck.isEmpty()) {
                Toast.makeText(MisskeyInstanceInput.this, "empty field", Toast.LENGTH_SHORT).show();
                return;
            }

            MisskeyInstanceValidator.validateInstance(instanceToCheck, new ValidationCallBack() {
                @Override
                public void onValidInstance(JSONObject meta) {
                    runOnUiThread(() -> {
                        validInstance.setVisibility(View.VISIBLE);
                        InvalidInstance.setVisibility(View.GONE);
                    });
                }

                @Override
                public void onInvalidInstance(String reason) {
                    runOnUiThread(() -> {
                        InvalidInstance.setVisibility(View.VISIBLE);
                        validInstance.setVisibility(View.GONE);
                        Toast.makeText(MisskeyInstanceInput.this, "Error: " + reason, Toast.LENGTH_SHORT).show();
                    });
                }
            });

        });
    }
}
