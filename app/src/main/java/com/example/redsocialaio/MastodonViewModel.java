package com.example.redsocialaio;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.redsocialaio.ui.home.Status;

/**
 * MastodonViewModel - Maneja los datos y la lógica de negocio de Mastodon
 * 
 * Esta clase:
 * - Almacena y gestiona el timeline de posts
 * - Coordina las llamadas al repositorio
 * - Mantiene el estado de autenticación
 * - Proporciona datos a las vistas mediante LiveData
 */
public class MastodonViewModel extends AndroidViewModel {
    // Variables principales del ViewModel
    private final MutableLiveData<List<Status>> timeline = new MutableLiveData<>(); // Lista observable de posts
    private final MutableLiveData<Status.Account> currentUser = new MutableLiveData<>(); // Usuario actual
    private final MastodonRepository repository; // Repositorio para llamadas a la API
    private final ExecutorService executor = Executors.newSingleThreadExecutor(); // Hilo para operaciones en background
    private final SharedPreferences prefs; // Almacenamiento local de configuración

    /**
     * Constructor - Inicializa el ViewModel con la configuración necesaria
     */
    public MastodonViewModel(@NonNull Application application) {
        super(application);
        // Obtener preferencias guardadas
        prefs = application.getSharedPreferences("mastodon_prefs", Application.MODE_PRIVATE);
        
        // Usar instancia pública conocida para timeline público
        String instanceUrl = "https://mastodon.social";
        String accessToken = prefs.getString("access_token", null);
        
        // Inicializar repositorio con la configuración
        repository = new MastodonRepository(instanceUrl, accessToken);
    }

    public LiveData<List<Status>> getTimeline() {
        return timeline;
    }
    
    public LiveData<Status.Account> getCurrentUser() {
        return currentUser;
    }

    public void fetchTimeline() {
        executor.execute(() -> {
            android.util.Log.d("MastodonViewModel", "Fetching public timeline...");
            
            // Siempre usar timeline público para mostrar posts de la comunidad
            List<Status> statuses = repository.getPublicTimeline();
            
            android.util.Log.d("MastodonViewModel", "Timeline fetched. Posts: " + (statuses != null ? statuses.size() : "null"));
            
            if (statuses != null && !statuses.isEmpty()) {
                timeline.postValue(statuses);
            } else {
                android.util.Log.w("MastodonViewModel", "No posts received, trying fallback...");
                // Intentar con una instancia diferente como fallback
                List<Status> fallbackStatuses = repository.getFallbackTimeline();
                timeline.postValue(fallbackStatuses);
            }
        });
    }
    
    public boolean isLoggedIn() {
        return prefs.getString("access_token", null) != null;
    }
    
    public void postStatus(String content, PostCallback callback) {
        executor.execute(() -> {
            android.util.Log.d("MastodonViewModel", "Publicando post: " + content.substring(0, Math.min(50, content.length())));
            boolean success = repository.postStatus(content);
            
            if (callback != null) {
                callback.onResult(success);
            }
            
            if (success) {
                android.util.Log.d("MastodonViewModel", "Post publicado exitosamente, actualizando timeline...");
                // Esperar un poco antes de actualizar para que el post aparezca en el servidor
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                fetchTimeline();
            } else {
                android.util.Log.e("MastodonViewModel", "Error al publicar post");
            }
        });
    }
    
    public void toggleFavourite(String statusId, boolean currentlyFavourited) {
        executor.execute(() -> {
            repository.toggleFavourite(statusId, currentlyFavourited);
        });
    }
    
    public void reblogStatus(String statusId) {
        executor.execute(() -> {
            repository.reblogStatus(statusId);
        });
    }
    
    public void followAccount(String accountId) {
        executor.execute(() -> {
            repository.followAccount(accountId);
        });
    }
    
    /**
     * Obtiene los datos del usuario autenticado desde la API
     * Ejecuta la operación en un hilo separado para no bloquear la UI
     */
    public void fetchCurrentUser() {
        executor.execute(() -> {
            android.util.Log.d("MastodonViewModel", "Fetching current user...");
            // Llamar al repositorio para obtener datos del usuario
            Status.Account user = repository.getCurrentUser();
            if (user != null) {
                // Actualizar LiveData para notificar a los observadores (ProfileFragment)
                currentUser.postValue(user);
            }
        });
    }
    
    public interface PostCallback {
        void onResult(boolean success);
    }
}
