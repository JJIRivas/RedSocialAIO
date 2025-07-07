package com.example.redsocialaio.misskey.userInfo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.redsocialaio.core.auth.AuthManager;
import com.example.redsocialaio.MainActivity;
import com.example.redsocialaio.misskey.auth.MisskeyAuthManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

/*Clase que se ocupa al regresar de inciar sesion en el navegador, android deberia rederigir aqui
una vez que el usuario termina eso.*/
public class MisskeyCallbackActivity extends AppCompatActivity {

    private static final String TAG = "MisskeyCallbackActivity";
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Toma la informacion de la actividad que inicio esta clase. Deberia ser MisskeyUserInput.
        Uri callbackUri = getIntent().getData();
        if (callbackUri == null) {
            Toast.makeText(this, "Callback inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        /*Ocupamos SharedPreferences para guardar de manera segura el Token del usuario y la URL
        de la instancia que pertenece... idealmente se cambia esto con el token cifrado una vez
        que SecureTokenStorage este listo.*/


        /*Iniciamos una instancia de MisskeyAuthManager para obtener y verificar el token del
        usuario.*/
        MisskeyAuthManager authManager = new MisskeyAuthManager(this);
        SharedPreferences prefs = getSharedPreferences("misskey_auth", Context.MODE_PRIVATE);
        String instanceUrl = prefs.getString("instance_url", null);
        String checker = prefs.getString("session_token", null);


        authManager.handleCallback(instanceUrl, checker, new AuthManager.AuthCallback() {
            @Override
            public void onAuthSuccess(String token) {
                Log.d(TAG, "Token obtenido correctamente: " + token);
                /*Si se logro obtener el Token, entonces creamos una instancia de la clase
                MisskeyAccount y seguimos un proceso para guardar los datos en la clase y en
                Firestore.*/
                ObtainMisskeyUserInfo.fetchUserInfo(instanceUrl, token, new ObtainMisskeyUserInfo.MisskeyUserInfoCallback() {
                    @Override
                    public void onSuccess(JSONObject userInfo) {
                        runOnUiThread(() -> {
                            try {
                                // 1. Inicializar servicio y storage
                                MisskeyAccountService accountService = new MisskeyAccountService(MisskeyCallbackActivity.this);
                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                                if (firebaseUser == null) {
                                    Toast.makeText(MisskeyCallbackActivity.this, "Debes iniciar sesión primero", Toast.LENGTH_LONG).show();
                                    finish();
                                    return;
                                }

                                // 2. Guardar cuenta Y token de forma segura
                                accountService.saveAccountWithToken(
                                        firebaseUser.getUid(),
                                        userInfo,
                                        token,         // Token guardado localmente cifrado
                                        instanceUrl    // URL de la instancia
                                ).addOnSuccessListener(aVoid -> {
                                    Toast.makeText(MisskeyCallbackActivity.this, "¡Login exitoso!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(MisskeyCallbackActivity.this, MainActivity.class));
                                    finish();
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(MisskeyCallbackActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });

                            } catch (Exception e) {
                                Log.e(TAG, "Error procesando login", e);
                            }
                        });
                    }

                    @Override
                    public void onFailure(String reason) {
                        runOnUiThread(() -> {
                            Toast.makeText(MisskeyCallbackActivity.this, "Falló al obtener info: " + reason, Toast.LENGTH_LONG).show();
                            finish();
                        });
                    }
                });
            }

            @Override
            public void onAuthFailed(String reason) {
                runOnUiThread(() -> {
                    Toast.makeText(MisskeyCallbackActivity.this, "Fallo autenticación: " + reason, Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }
}

