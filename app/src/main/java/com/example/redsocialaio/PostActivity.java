package com.example.redsocialaio;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

/**
 * PostActivity - Pantalla para crear y publicar nuevos posts
 * 
 * Funciones:
 * - Permite al usuario escribir un mensaje
 * - Valida que el contenido no esté vacío
 * - Envía el post a Mastodon usando el ViewModel
 * - Muestra feedback al usuario sobre el resultado
 */
public class PostActivity extends AppCompatActivity {
    // Elementos de la interfaz
    private EditText contentEdit;    // Campo de texto para el contenido
    private Button postButton;       // Botón para publicar
    private MastodonViewModel viewModel; // ViewModel para manejar la lógica

    /**
     * onCreate - Inicializa la actividad y configura los elementos
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configurar el layout
        setContentView(R.layout.activity_post);

        // Inicializar elementos de la UI
        contentEdit = findViewById(R.id.edit_post_content);
        postButton = findViewById(R.id.btn_post);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(MastodonViewModel.class);

        // Configurar acción del botón
        postButton.setOnClickListener(v -> {
            // Obtener el contenido del post
            String content = contentEdit.getText().toString().trim();
            
            // Validar que no esté vacío
            if (!content.isEmpty()) {
                // Desactivar botón para evitar envíos múltiples
                postButton.setEnabled(false);
                
                // Enviar post usando el ViewModel
                viewModel.postStatus(content, success -> {
                    // Actualizar UI en el hilo principal
                    runOnUiThread(() -> {
                        // Reactivar botón
                        postButton.setEnabled(true);
                        
                        // Mostrar resultado
                        if (success) {
                            Toast.makeText(this, "Post publicado", Toast.LENGTH_SHORT).show();
                            finish(); // Cerrar actividad
                        } else {
                            Toast.makeText(this, "Error al publicar", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            } else {
                Toast.makeText(this, "Escribe algo", Toast.LENGTH_SHORT).show();
            }
        });
    }
}