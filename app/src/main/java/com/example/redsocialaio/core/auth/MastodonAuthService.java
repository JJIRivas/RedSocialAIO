package com.example.redsocialaio.core.auth;

import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * MastodonAuthService - Interface para autenticación OAuth con Mastodon
 * <p>
 * Maneja el proceso de autenticación en 2 pasos:
 * 1. Registrar la aplicación en la instancia
 * 2. Intercambiar código de autorización por token de acceso
 */
public interface MastodonAuthService {

    // Paso 1: Registrar la aplicación en la instancia de Mastodon
    @FormUrlEncoded
    @POST("/api/v1/apps")
    Call<AppRegistration> registerApp(
            @Field("client_name") String clientName,
            @Field("redirect_uris") String redirectUris,
            @Field("scopes") String scopes
    );

    // Paso 2: Intercambiar código de autorización por token de acceso
    @FormUrlEncoded
    @POST("/oauth/token")
    Call<TokenResponse> getAccessToken(
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret,
            @Field("redirect_uri") String redirectUri,
            @Field("code") String code,
            @Field("grant_type") String grantType
    );

    // Respuesta del registro de aplicación
    class AppRegistration {
        @SerializedName("client_id")
        public String clientId;
        @SerializedName("client_secret")
        public String clientSecret;
    }

    // Respuesta del intercambio de token
    class TokenResponse {
        @SerializedName("access_token")
        public String accessToken;
        @SerializedName("token_type")
        public String tokenType;
        @SerializedName("scope")
        public String scope;
    }
}