package com.example.redsocialaio.misskey;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

public class ObtainMisskeyUserInfo {

    private static final String TAG = "ObtainMisskeyUserInfo";

    public interface MisskeyUserInfoCallback {
        void onSuccess(JSONObject userInfo);

        void onFailure(String reason);
    }

    public static void fetchUserInfo(String instanceUrl, String accessToken, MisskeyUserInfoCallback callback) {
        OkHttpClient client = new OkHttpClient();

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("i", accessToken);
        } catch (JSONException e) {
            callback.onFailure("No se pudo crear el JSON del cuerpo");
            return;
        }

        Request request = new Request.Builder()
                .url("https://" + instanceUrl + "/api/i")
                .post(RequestBody.create(
                        requestBody.toString(),
                        MediaType.parse("application/json; charset=utf-8")
                ))
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
                    callback.onFailure("Error al parsear la respuesta JSON");
                }
            }
        });
    }

}
