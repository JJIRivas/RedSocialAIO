package com.example.redsocialaio.misskey;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MisskeyInstanceValidator {

    private static final String TAG = "MisskeyInstanceValidator";

    public static void validateInstance(String domain, ValidationCallBack callback) {
        OkHttpClient client = new OkHttpClient();

        String url = "https://" + domain + "/api/meta";

        RequestBody body = RequestBody.create("{}", MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Network error: " + e.getMessage());
                callback.onInvalidInstance("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.w(TAG, "HTTP error code: " + response.code());
                    callback.onInvalidInstance("HTTP error code: " + response.code());
                    return;
                }

                String body = response.body().string();
                Log.d(TAG, "Response body: " + body);

                try {
                    JSONObject json = new JSONObject(body);
                    Log.d("MisskeyValidator", "Respuesta cruda: " + body);
                    if (json.has("version") && json.has("uri")) {
                        callback.onValidInstance(json);
                    } else {
                        callback.onInvalidInstance("Instancia parece invalida para la app.");
                    }
                } catch (JSONException e) {
                    callback.onInvalidInstance("Error al parsear respuesta.");
                }

            }

        });

    }


}
