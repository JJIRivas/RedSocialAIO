package com.example.redsocialaio.misskey.auth;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.example.redsocialaio.core.auth.AuthManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

import okhttp3.*;

public class MisskeyAuthManager implements AuthManager {

    //Variables que representan el contexto, url de la instancia, donde retornar despues de iniciar sesion, etc. Los permisos que pide nuestra app son fullPermissions... son hartos ya que es un cliente.
    private static final String TAG = "MisskeyAuthManager";
    private final OkHttpClient client = new OkHttpClient();
    private final Context context;
    private final String appName = "RedSocialAIO";
    private final String callbackUrl = "redsocialaio://misskey/callback";
    private final String fullPermissions = "read:account,write:account,read:blocks,write:blocks," +
            "read:drive,write:drive,read:favorites,write:favorites,read:following,write:following," +
            "read:messaging,write:messaging,read:mutes,write:mutes,write:notes,read:notifications," +
            "write:notifications,read:reactions,write:reactions,write:votes,read:pages,write:pages," +
            "write:page-likes,read:page-likes,read:user-groups,write:user-groups,read:channels," +
            "write:channels,read:gallery,write:gallery,read:gallery-likes,write:gallery-likes," +
            "read:flash,write:flash,read:flash-likes,write:flash-likes,write:clip-favorite," +
            "read:clip-favorite,read:federation,write:chat,read:chat,read:notes";

    private String obtainedToken; // Token final, persistente
    private String currentSessionToken;


    public MisskeyAuthManager(Context context) {
        this.context = context;
    }

    @Override
    public void startAuthFlow(String instanceUrl) {
        // Generar token de sesión
        currentSessionToken = UUID.randomUUID().toString();

        // Construir URL de auth
        Uri authUri = Uri.parse("https://" + instanceUrl + "/miauth/" + currentSessionToken)
                .buildUpon()
                .appendQueryParameter("name", appName)
                .appendQueryParameter("callback", callbackUrl)
                .appendQueryParameter("permission", fullPermissions)
                .build();

        // Guardar datos temporales
        context.getSharedPreferences("misskey_auth", Context.MODE_PRIVATE)
                .edit()
                .putString("session_token", currentSessionToken)
                .putString("instance_url", instanceUrl)
                .apply();

        // Abrir navegador
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, authUri);
        context.startActivity(browserIntent);
    }



    @Override
    public void handleCallback(String instanceUrl, String sec, AuthCallback callback) {

        if (sec == null) {
            callback.onAuthFailed("Sesion no iniciada.");
            return;
        }


        String url = "https://" + instanceUrl + "/api/miauth/" + sec + "/check";
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        JSONObject requestBody = new JSONObject();

        try {
            requestBody.put("token", sec);
        } catch (JSONException e) {
            callback.onAuthFailed("No se pudo crear el JSON del cuerpo");
            return;
        }

        RequestBody body = RequestBody.create(requestBody.toString(), mediaType);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "MiAuth check failed: " + e.getMessage());
                callback.onAuthFailed("No se pudo verificar la sesión.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onAuthFailed("Respuesta inválida del servidor.");
                    return;
                }


                String responseBody = response.body().string();
                try {
                    JSONObject json = new JSONObject(responseBody);
                    if (json.has("token")) {
                        obtainedToken = json.getString("token");
                        callback.onAuthSuccess(obtainedToken); //Implementada en MisskeyCallbackActivity.
                    } else {
                        callback.onAuthFailed("Token no encontrado.");
                    }
                } catch (Exception e) {
                    callback.onAuthFailed("Error al interpretar respuesta.");
                }
            }

        });
    }

}