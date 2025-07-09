package com.example.redsocialaio.mastodon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.redsocialaio.core.auth.AuthManager;
import com.example.redsocialaio.core.auth.MastodonAuthService;
//import com.example.redsocialaio.network.MastodonAuthService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MastodonAuthManager implements AuthManager {
    private static final String TAG = "MastodonAuthManager";
    private final Context context;
    private final SharedPreferences prefs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MastodonAuthManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("mastodon_prefs", Context.MODE_PRIVATE);
    }

    @Override
    public void startAuthFlow(String instanceUrl) {
        if (!instanceUrl.startsWith("http://") && !instanceUrl.startsWith("https://")) {
            instanceUrl = "https://" + instanceUrl;
        }

        String finalInstanceUrl = instanceUrl;
        executor.execute(() -> {
            try {

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(finalInstanceUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                MastodonAuthService service = retrofit.create(MastodonAuthService.class);
                MastodonAuthService.AppRegistration app = service.registerApp(
                        "RedSocialAIO",
                        "urn:ietf:wg:oauth:2.0:oob",
                        "read write follow"
                ).execute().body();

                if (app != null) {
                    prefs.edit()
                            .putString("client_id", app.clientId)
                            .putString("client_secret", app.clientSecret)
                            .putString("instance_url", finalInstanceUrl)
                            .apply();

                    String authUrl = finalInstanceUrl + "/oauth/authorize?client_id=" + app.clientId +
                            "&redirect_uri=urn:ietf:wg:oauth:2.0:oob&response_type=code&scope=read+write+follow";

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
                    context.startActivity(intent);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Intent callbackIntent = new Intent(context, MastodonCallbackActivity.class);
                        callbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(callbackIntent);
                    }, 10000);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error durante el flujo de autenticación", e);
            }
        });
    }

    @Override
    public void handleCallback(String instanceUrl, String code, AuthCallback callback) {
        executor.execute(() -> {
            try {
                Log.d("MastodonAuthManager", instanceUrl + " " + code);
                String clientId = prefs.getString("client_id", "");
                String clientSecret = prefs.getString("client_secret", "");
                Log.d("MastodonAuthManager", clientId + " " + clientSecret);

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(instanceUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                MastodonAuthService service = retrofit.create(MastodonAuthService.class);
                MastodonAuthService.TokenResponse token = service.getAccessToken(
                        clientId,
                        clientSecret,
                        "urn:ietf:wg:oauth:2.0:oob",
                        code,
                        "authorization_code"
                ).execute().body();

                if (token != null) {
                    prefs.edit()
                            .putString("access_token", token.accessToken)
                            .apply();

                    callback.onAuthSuccess(token.accessToken);
                } else {
                    callback.onAuthFailed("No se recibió token.");
                }

            } catch (Exception e) {
                Log.e(TAG, "Error en callback OAuth", e);
                callback.onAuthFailed("Fallo al obtener token.");
            }
        });
    }
}

