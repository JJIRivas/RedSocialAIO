package com.example.redsocialaio;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.redsocialaio.ui.home.Status;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TestActivity - Pantalla para probar la conexión con la API de Mastodon
 * 
 * Funciones:
 * - Prueba la conexión con mastodon.social
 * - Obtiene posts del timeline público
 * - Muestra el resultado de la prueba
 * - Útil para debugging y verificar que la API funciona
 */
public class TestActivity extends AppCompatActivity {
    private TextView resultText;
    private Button testButton;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Crear layout programáticamente (sin XML)
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        
        // Crear botón de prueba
        testButton = new Button(this);
        testButton.setText("Test API Connection");
        
        // Crear texto para mostrar resultados
        resultText = new TextView(this);
        resultText.setText("Presiona el botón para probar");
        
        // Agregar elementos al layout
        layout.addView(testButton);
        layout.addView(resultText);
        setContentView(layout);
        
        // Configurar acción del botón
        testButton.setOnClickListener(v -> testConnection());
    }
    
    // Probar conexión con la API de Mastodon
    private void testConnection() {
        // Desactivar botón durante la prueba
        testButton.setEnabled(false);
        resultText.setText("Probando conexión...");
        
        // Ejecutar prueba en hilo separado
        executor.execute(() -> {
            try {
                // Crear repositorio sin autenticación (solo timeline público)
                MastodonRepository repo = new MastodonRepository("https://mastodon.social/", null);
                List<Status> statuses = repo.getPublicTimeline();
                
                // Mostrar resultado en UI
                runOnUiThread(() -> {
                    if (statuses != null && !statuses.isEmpty()) {
                        // Mostrar información del primer post como ejemplo
                        String firstPostContent = statuses.get(0).getContent() != null ? 
                            statuses.get(0).getContent().replaceAll("<.*?>", "").substring(0, Math.min(100, statuses.get(0).getContent().length())) : "Sin contenido";
                        
                        resultText.setText("✅ Conexión exitosa!\nPosts obtenidos: " + statuses.size() + 
                            "\nPrimer post: " + firstPostContent);
                    } else {
                        resultText.setText("❌ No se obtuvieron posts");
                    }
                    testButton.setEnabled(true);
                });
            } catch (Exception e) {
                Log.e("TestActivity", "Error en prueba de conexión", e);
                runOnUiThread(() -> {
                    resultText.setText("❌ Error: " + e.getMessage());
                    testButton.setEnabled(true);
                });
            }
        });
    }
}