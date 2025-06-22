package com.example.redsocialaio.misskey;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

import okhttp3.*;

public class MisskeyAuthManager {

    private static final String TAG = "MisskeyAuthManager";
    private final OkHttpClient client = new OkHttpClient();
    private final Context context;
    private final String instanceUrl;
    private final String appName = "RedSocialAIO";
    private final String callbackUrl = "redsocialaio://misskey/callback";
    private final String fullPermissions = "read:account,write:account,read:blocks,write:blocks," +
            "read:drive,write:drive,read:favorites,write:favorites,read:following,write:following," +
            "read:messaging,write:messaging,read:mutes,write:mutes,write:notes,read:notifications," +
            "write:notifications,read:reactions,write:reactions,write:votes,read:pages,write:pages," +
            "write:page-likes,read:page-likes,read:user-groups,write:user-groups,read:channels," +
            "write:channels,read:gallery,write:gallery,read:gallery-likes,write:gallery-likes," +
            "read:flash,write:flash,read:flash-likes,write:flash-likes,write:clip-favorite," +
            "read:clip-favorite,read:federation,write:chat,read:chat";

    private String sessionToken; // Se genera y se usa una vez
    private String obtainedToken; // Token final, persistente

    public MisskeyAuthManager(Context context, String instanceUrl) {
        this.context = context;
        this.instanceUrl = instanceUrl;
    }

    public void startSession() {
        sessionToken = UUID.randomUUID().toString();
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public String generateAuthUrl() {
        if (sessionToken == null) {
            throw new IllegalStateException("No se llamo a startSession() primero.");
        }

        Uri uri = Uri.parse("https://" + instanceUrl + "/miauth/" + sessionToken)
                .buildUpon()
                .appendQueryParameter("name", appName)
                .appendQueryParameter("callback", callbackUrl)
                .appendQueryParameter("permission", fullPermissions)
                .build();
        return uri.toString();
    }

    public void checkMiAuthSession(MisskeyAuthCallback callback) {
        if (sessionToken == null) {
            callback.onAuthFailed("Sesion no iniciada.");
            return;
        }

        String url = "https://" + instanceUrl + "/api/miauth/" + sessionToken + "/check";
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        JSONObject requestBody = new JSONObject();

        try {
            requestBody.put("token", sessionToken);
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
                        callback.onAuthSuccess(obtainedToken);
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