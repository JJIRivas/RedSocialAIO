package com.example.redsocialaio.network;

import com.example.redsocialaio.ui.home.Status;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * MastodonApiService - Interface Retrofit para la API de Mastodon
 * 
 * Define todos los endpoints disponibles:
 * - Timeline público y personal
 * - Publicar posts
 * - Dar like/unlike
 * - Reblog
 * - Seguir/dejar de seguir usuarios
 */
public interface MastodonApiService {
    // Obtener timeline público (sin autenticación)
    @GET("/api/v1/timelines/public?limit=20")
    Call<List<Status>> getPublicTimeline();
    
    // Obtener timeline personal (requiere autenticación)
    @GET("/api/v1/timelines/home?limit=20")
    Call<List<Status>> getHomeTimeline(@Header("Authorization") String authorization);
    
    // Publicar un nuevo post
    @FormUrlEncoded
    @POST("/api/v1/statuses")
    Call<Status> postStatus(@Header("Authorization") String authorization, @Field("status") String status);
    
    // Dar like a un post
    @POST("/api/v1/statuses/{id}/favourite")
    Call<Status> favouriteStatus(@Header("Authorization") String authorization, @Path("id") String statusId);
    
    // Quitar like de un post
    @POST("/api/v1/statuses/{id}/unfavourite")
    Call<Status> unfavouriteStatus(@Header("Authorization") String authorization, @Path("id") String statusId);
    
    // Hacer reblog de un post
    @POST("/api/v1/statuses/{id}/reblog")
    Call<Status> reblogStatus(@Header("Authorization") String authorization, @Path("id") String statusId);
    
    // Seguir a un usuario
    @POST("/api/v1/accounts/{id}/follow")
    Call<Relationship> followAccount(@Header("Authorization") String authorization, @Path("id") String accountId);
    
    // Dejar de seguir a un usuario
    @POST("/api/v1/accounts/{id}/unfollow")
    Call<Relationship> unfollowAccount(@Header("Authorization") String authorization, @Path("id") String accountId);
    
    // Obtener información del usuario actual
    @GET("/api/v1/accounts/verify_credentials")
    Call<Status.Account> getCurrentUser(@Header("Authorization") String authorization);
    
    // Modelo para respuesta de relaciones entre usuarios
    class Relationship {
        @SerializedName("following")
        public boolean following;
    }
}

