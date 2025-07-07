package com.example.redsocialaio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.redsocialaio.network.MastodonAuthService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * LoginActivity - Pantalla de autenticación OAuth con Mastodon
 * 
 * Proceso de login:
 * 1. Usuario ingresa URL de la instancia de Mastodon
 * 2. Se registra la app en esa instancia
 * 3. Se abre el navegador para que el usuario autorice
 * 4. Usuario copia el código de autorización
 * 5. Se intercambia el código por un token de acceso
 * 6. Se guarda el token y se va a MainActivity
 */
public class LoginActivity extends AppCompatActivity {
    private EditText instanceUrlEdit;
    private Button loginButton;
    private SharedPreferences prefs;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        prefs = getSharedPreferences("mastodon_prefs", MODE_PRIVATE);
        
        // Si ya tiene token, ir directo a MainActivity
        if (prefs.getString("access_token", null) != null) {
            startMainActivity();
            return;
        }
        
        // Configurar elementos de la UI
        instanceUrlEdit = findViewById(R.id.edit_instance_url);
        loginButton = findViewById(R.id.btn_login);
        
        // URL por defecto
        instanceUrlEdit.setText("https://mastodon.social");
        
        // Configurar botón de login
        loginButton.setOnClickListener(v -> startOAuthFlow());
    }
    
    // Iniciar el proceso de autenticación OAuth
    private void startOAuthFlow() {
        String instanceUrl = instanceUrlEdit.getText().toString().trim();
        if (instanceUrl.isEmpty()) {
            Toast.makeText(this, "Ingresa la URL de la instancia", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Ejecutar en hilo separado para no bloquear la UI
        executor.execute(() -> {
            try {
                // Configurar Retrofit para la instancia
                Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(instanceUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
                
                MastodonAuthService authService = retrofit.create(MastodonAuthService.class);
                
                // Paso 1: Registrar la app en la instancia
                MastodonAuthService.AppRegistration app = authService.registerApp(
                    "RedSocialAIO",
                    "urn:ietf:wg:oauth:2.0:oob",
                    "read write follow"
                ).execute().body();
                
                if (app != null) {
                    // Guardar credenciales de la app
                    prefs.edit()
                        .putString("client_id", app.clientId)
                        .putString("client_secret", app.clientSecret)
                        .putString("instance_url", instanceUrl)
                        .apply();
                    
                    // Paso 2: Construir URL de autorización
                    String authUrl = instanceUrl + "/oauth/authorize?client_id=" + app.clientId +
                        "&redirect_uri=urn:ietf:wg:oauth:2.0:oob&response_type=code&scope=read+write+follow";
                    
                    runOnUiThread(() -> {
                        // Abrir navegador para que el usuario autorice
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
                        startActivity(intent);
                        
                        // Mostrar diálogo para que ingrese el código
                        showCodeInputDialog();
                    });
                }
            } catch (Exception e) {
                Log.e("LoginActivity", "Error en OAuth", e);
                runOnUiThread(() -> Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show());
            }
        });
    }
    
    private void showCodeInputDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Código de autorización");
        
        EditText input = new EditText(this);
        input.setHint("Pega aquí el código");
        builder.setView(input);
        
        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            String code = input.getText().toString().trim();
            if (!code.isEmpty()) {
                exchangeCodeForToken(code);
            }
        });
        
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
    
    // Intercambiar código de autorización por token de acceso
    private void exchangeCodeForToken(String code) {
        executor.execute(() -> {
            try {
                // Obtener credenciales guardadas
                String instanceUrl = prefs.getString("instance_url", "");
                String clientId = prefs.getString("client_id", "");
                String clientSecret = prefs.getString("client_secret", "");
                
                // Configurar Retrofit
                Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(instanceUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
                
                MastodonAuthService authService = retrofit.create(MastodonAuthService.class);
                
                // Paso 3: Intercambiar código por token
                MastodonAuthService.TokenResponse token = authService.getAccessToken(
                    clientId,
                    clientSecret,
                    "urn:ietf:wg:oauth:2.0:oob",
                    code,
                    "authorization_code"
                ).execute().body();
                
                if (token != null) {
                    // Guardar token de acceso
                    prefs.edit()
                        .putString("access_token", token.accessToken)
                        .apply();
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show();
                        startMainActivity();
                    });
                }
            } catch (Exception e) {
                Log.e("LoginActivity", "Error obteniendo token", e);
                runOnUiThread(() -> Toast.makeText(this, "Error en autenticación", Toast.LENGTH_SHORT).show());
            }
        });
    }
    
    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}