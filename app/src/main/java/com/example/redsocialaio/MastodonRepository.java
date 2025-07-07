package com.example.redsocialaio;

import android.util.Log;

import com.example.redsocialaio.network.MastodonApiService;
import com.example.redsocialaio.ui.home.Status;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * MastodonRepository - Capa de acceso a datos para la API de Mastodon
 * 
 * Responsabilidades:
 * - Realizar llamadas HTTP a la API de Mastodon
 * - Manejar autenticación con tokens
 * - Procesar respuestas y errores
 * - Proporcionar métodos para timeline, posts, likes, reblogs y follows
 */
public class MastodonRepository {
    // Componentes principales
    private final MastodonApiService apiService; // Servicio Retrofit para llamadas HTTP
    private final String accessToken; // Token de autenticación OAuth

    /**
     * Constructor - Inicializa el repositorio con la configuración de la instancia
     * @param instanceUrl URL de la instancia de Mastodon
     * @param accessToken Token de acceso OAuth (puede ser null para timeline público)
     */
    public MastodonRepository(String instanceUrl, String accessToken) {
        this.accessToken = accessToken;
        
        // Asegurar que la URL termine con /
        if (!instanceUrl.endsWith("/")) {
            instanceUrl += "/";
        }
        
        Log.d("MastodonRepo", "Inicializando con instancia: " + instanceUrl);
        
        // Configurar Retrofit para la API de Mastodon
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(instanceUrl)
                .addConverterFactory(GsonConverterFactory.create()) // Convertir JSON automáticamente
                .build();
        
        // Crear servicio API
        apiService = retrofit.create(MastodonApiService.class);
    }

    // Obtener timeline público (no requiere autenticación)
    public List<Status> getPublicTimeline() {
        try {
            Log.d("MastodonRepo", "Obteniendo timeline público...");
            Call<List<Status>> call = apiService.getPublicTimeline();
            Response<List<Status>> response = call.execute();
            
            if (response.isSuccessful() && response.body() != null) {
                List<Status> posts = response.body();
                Log.d("MastodonRepo", "Timeline público obtenido: " + posts.size() + " posts");
                
                // Log de los primeros posts para debug
                for (int i = 0; i < Math.min(3, posts.size()); i++) {
                    Status post = posts.get(i);
                    Log.d("MastodonRepo", "Post " + i + ": " + 
                        (post.getAccount() != null ? post.getAccount().getDisplayName() : "Unknown") + 
                        " - " + (post.getContent() != null ? post.getContent().substring(0, Math.min(50, post.getContent().length())) : "No content"));
                }
                
                return posts;
            } else {
                Log.e("MastodonRepo", "Error en la respuesta: " + response.code() + " - " + response.message());
                if (response.errorBody() != null) {
                    Log.e("MastodonRepo", "Error body: " + response.errorBody().toString());
                }
                return null;
            }
        } catch (Exception e) {
            Log.e("MastodonRepo", "Error al obtener timeline público", e);
            return null;
        }
    }
    
    // Método fallback con datos de prueba
    public List<Status> getFallbackTimeline() {
        Log.d("MastodonRepo", "Usando timeline fallback con datos de prueba");
        
        List<Status> fallbackPosts = new java.util.ArrayList<>();
        
        // Crear posts de prueba
        for (int i = 1; i <= 5; i++) {
            Status post = new Status();
            post.setId("fallback_" + i);
            post.setContent("Post público de prueba #" + i + " - Este es contenido de ejemplo para mostrar cómo se verían los posts de Mastodon.");
            post.setFavouritesCount(i * 3);
            post.setReblogsCount(i * 2);
            
            Status.Account account = new Status.Account();
            account.setId("user_" + i);
            account.setDisplayName("Usuario " + i);
            account.setAvatar("https://mastodon.social/avatars/original/missing.png");
            post.setAccount(account);
            
            fallbackPosts.add(post);
        }
        
        return fallbackPosts;
    }
    
    // Obtener timeline personal (requiere autenticación)
    public List<Status> getHomeTimeline() {
        // Si no hay token, usar timeline público
        if (accessToken == null) {
            Log.d("MastodonRepo", "Sin token, usando timeline público");
            return getPublicTimeline();
        }
        
        try {
            Log.d("MastodonRepo", "Obteniendo timeline personal...");
            Call<List<Status>> call = apiService.getHomeTimeline("Bearer " + accessToken);
            Response<List<Status>> response = call.execute();
            
            if (response.isSuccessful() && response.body() != null) {
                Log.d("MastodonRepo", "Timeline personal obtenido: " + response.body().size() + " posts");
                return response.body();
            } else {
                Log.e("MastodonRepo", "Error en timeline personal: " + response.code() + " - " + response.message());
                Log.d("MastodonRepo", "Fallback a timeline público");
                return getPublicTimeline();
            }
        } catch (Exception e) {
            Log.e("MastodonRepo", "Error al obtener timeline personal", e);
            Log.d("MastodonRepo", "Fallback a timeline público");
            return getPublicTimeline();
        }
    }
    
    // Publicar un nuevo post
    public boolean postStatus(String content) {
        if (accessToken == null) return false;
        
        try {
            Call<Status> call = apiService.postStatus("Bearer " + accessToken, content);
            Response<Status> response = call.execute();
            return response.isSuccessful();
        } catch (Exception e) {
            Log.e("MastodonRepo", "Error al postear", e);
            return false;
        }
    }
    
    // Dar o quitar like a un post
    public boolean toggleFavourite(String statusId, boolean currentlyFavourited) {
        if (accessToken == null) return false;
        
        try {
            // Elegir endpoint según el estado actual
            Call<Status> call = currentlyFavourited ? 
                apiService.unfavouriteStatus("Bearer " + accessToken, statusId) :
                apiService.favouriteStatus("Bearer " + accessToken, statusId);
            Response<Status> response = call.execute();
            return response.isSuccessful();
        } catch (Exception e) {
            Log.e("MastodonRepo", "Error en favourite", e);
            return false;
        }
    }
    
    // Hacer reblog de un post
    public boolean reblogStatus(String statusId) {
        if (accessToken == null) return false;
        
        try {
            Call<Status> call = apiService.reblogStatus("Bearer " + accessToken, statusId);
            Response<Status> response = call.execute();
            return response.isSuccessful();
        } catch (Exception e) {
            Log.e("MastodonRepo", "Error en reblog", e);
            return false;
        }
    }
    
    // Seguir a un usuario
    public boolean followAccount(String accountId) {
        if (accessToken == null) return false;
        
        try {
            Call<com.example.redsocialaio.network.MastodonApiService.Relationship> call = 
                apiService.followAccount("Bearer " + accessToken, accountId);
            Response<com.example.redsocialaio.network.MastodonApiService.Relationship> response = call.execute();
            return response.isSuccessful();
        } catch (Exception e) {
            Log.e("MastodonRepo", "Error al seguir", e);
            return false;
        }
    }
    
    /**
     * Obtiene información completa del usuario autenticado
     * Usa el endpoint verify_credentials de Mastodon
     * @return Account con datos del usuario o null si hay error
     */
    public Status.Account getCurrentUser() {
        // Verificar que hay token de autenticación
        if (accessToken == null) return null;
        
        try {
            Log.d("MastodonRepo", "Obteniendo usuario actual...");
            // Llamar al endpoint de verificación de credenciales
            Call<Status.Account> call = apiService.getCurrentUser("Bearer " + accessToken);
            Response<Status.Account> response = call.execute();
            
            // Verificar que la respuesta sea exitosa
            if (response.isSuccessful() && response.body() != null) {
                Log.d("MastodonRepo", "Usuario obtenido: " + response.body().getDisplayName());
                return response.body(); // Retornar datos del usuario
            } else {
                Log.e("MastodonRepo", "Error al obtener usuario: " + response.code());
                return null;
            }
        } catch (Exception e) {
            Log.e("MastodonRepo", "Error al obtener usuario actual", e);
            return null;
        }
    }
}
