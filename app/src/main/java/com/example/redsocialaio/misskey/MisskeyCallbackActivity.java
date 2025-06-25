package com.example.redsocialaio.misskey;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.redsocialaio.MainActivity;
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
        SharedPreferences prefs = getSharedPreferences("misskey_auth", MODE_PRIVATE);
        String sessionToken = prefs.getString("session_token", null);
        String instanceUrl = prefs.getString("instance_url", null);

        if (sessionToken == null || instanceUrl == null) {
            Toast.makeText(this, "No se pudo iniciar sesión (falta token)", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        /*Iniciamos una instancia de MisskeyAuthManager para obtener y verificar el token del
        usuario.*/
        MisskeyAuthManager authManager = new MisskeyAuthManager(this, instanceUrl);
        authManager.setSessionToken(sessionToken); //el sessionToken en este caso es el token temporal- este lo obtenemos con la informacion de MisskeyInstanceInput mediante su SharedPreferences.

        authManager.checkMiAuthSession(new MisskeyAuthCallback() {
            @Override
            public void onAuthSuccess(String token) {
                Log.d(TAG, "Token obtenido correctamente: " + token);
                /*Si se logro obtener el Token, entonces creamos una instancia de la clase
                MisskeyAccount y seguimos un proceso para guardar los datos en la clase y en
                Firestore.*/
                ObtainMisskeyUserInfo.fetchUserInfo(instanceUrl, token, new ObtainMisskeyUserInfo.MisskeyUserInfoCallback() {
                    @Override
                    public void onSuccess(JSONObject userInfo) {
                        Log.d(TAG, "Usuario: " + userInfo.toString());
                        runOnUiThread(() -> {
                            MisskeyAccount misskeyAccount = new MisskeyAccount();
                            misskeyAccount.setAccessToken(token);
                            misskeyAccount.fromJSONObject(userInfo);
                            misskeyAccount.toFirestoreMap();
                            misskeyAccount.saveToFirestore(currentUser.getUid());

                            Toast.makeText(MisskeyCallbackActivity.this, "¡Login exitoso!", Toast.LENGTH_SHORT).show();
                            // TODO Ver a que conviene redireccionar, ahora solo esta a la pantalla principal.
                            Intent i = new Intent(MisskeyCallbackActivity.this, MainActivity.class);
                            i.putExtra("user_info", userInfo.toString());
                            startActivity(i);
                            finish();
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

