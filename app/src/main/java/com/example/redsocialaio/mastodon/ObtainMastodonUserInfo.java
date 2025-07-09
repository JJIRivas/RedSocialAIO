package com.example.redsocialaio.mastodon;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

public class ObtainMastodonUserInfo {

    public interface MastodonUserInfoCallback {
        void onSuccess(JSONObject userInfo);

        void onFailure(String reason);
    }

    public static void fetchUserInfo(String instanceUrl, String accessToken, MastodonUserInfoCallback callback) {
        OkHttpClient client = new OkHttpClient();
        String fixedInstanceUrl = instanceUrl.startsWith("http") ? instanceUrl : "https://" + instanceUrl;

        Request request = new Request.Builder()
                .url(fixedInstanceUrl + "/api/v1/accounts/verify_credentials")
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Error de conexión: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure("Código de error: " + response.code());
                    return;
                }

                String responseBody = response.body().string();
                try {
                    JSONObject json = new JSONObject(responseBody);
                    callback.onSuccess(json);
                } catch (JSONException e) {
                    callback.onFailure("Error al parsear respuesta");
                }
            }
        });
    }
}

