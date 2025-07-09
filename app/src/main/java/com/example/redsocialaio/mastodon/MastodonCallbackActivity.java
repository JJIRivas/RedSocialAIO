package com.example.redsocialaio.mastodon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.redsocialaio.core.auth.AuthManager;
import com.example.redsocialaio.ui.Unified.UnifiedTimeline;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

public class MastodonCallbackActivity extends AppCompatActivity {

    private static final String TAG = "MastodonCallbackActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EditText codeInput = new EditText(this);
        codeInput.setHint("Pega aquí el código de autorización");

        new AlertDialog.Builder(this)
                .setTitle("Código de autorización")
                .setView(codeInput)
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    String code = codeInput.getText().toString().trim();
                    if (!code.isEmpty()) {
                        procesarCallback(code);
                    } else {
                        Toast.makeText(this, "Código vacío", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> finish())
                .show();
    }

    private void procesarCallback(String code) {
        SharedPreferences prefs = getSharedPreferences("mastodon_prefs", MODE_PRIVATE);
        String instanceUrl = prefs.getString("instance_url", "");
        MastodonAuthManager authManager = new MastodonAuthManager(this);

        authManager.handleCallback(instanceUrl, code, new AuthManager.AuthCallback() {
            @Override
            public void onAuthSuccess(String token) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(MastodonCallbackActivity.this, "Inicia sesión primero", Toast.LENGTH_LONG).show();
                        finish();
                    });
                    return;
                }

                ObtainMastodonUserInfo.fetchUserInfo(instanceUrl, token, new ObtainMastodonUserInfo.MastodonUserInfoCallback() {
                    @Override
                    public void onSuccess(JSONObject userInfo) {
                        runOnUiThread(() -> {
                            MastodonAccountService accountService = new MastodonAccountService(MastodonCallbackActivity.this);
                            accountService.saveAccountWithToken(
                                    currentUser.getUid(),
                                    userInfo,
                                    token,
                                    instanceUrl
                            ).addOnSuccessListener(aVoid -> {
                                runOnUiThread(() -> {
                                    Toast.makeText(MastodonCallbackActivity.this, "¡Login exitoso!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(MastodonCallbackActivity.this, UnifiedTimeline.class));
                                    finish();
                                });
                            }).addOnFailureListener(e -> {
                                runOnUiThread(() -> {
                                    Toast.makeText(MastodonCallbackActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    finish();
                                });
                            });
                        });
                    }

                    @Override
                    public void onFailure(String reason) {
                        runOnUiThread(() -> {
                            Log.d("MastodonCallbackActivity", instanceUrl);
                            Toast.makeText(MastodonCallbackActivity.this, "Falló al obtener info: " + reason, Toast.LENGTH_LONG).show();
                            Log.d("MastodonCallbackActivity", reason);
                            finish();
                        });
                    }
                });
            }

            @Override
            public void onAuthFailed(String reason) {
                runOnUiThread(() -> {
                    Toast.makeText(MastodonCallbackActivity.this, "Error: " + reason, Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }


}

