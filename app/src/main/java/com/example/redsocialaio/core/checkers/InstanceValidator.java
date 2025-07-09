package com.example.redsocialaio.core.checkers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.redsocialaio.core.auth.ValidationCallBack;

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

public class InstanceValidator {

    private static final String TAG = "InstanceValidator";

    public enum PlatformType {
        MISSKEY, MASTODON
    }

    public static void validateInstance(String domain, PlatformType platform, ValidationCallBack callback) {
        OkHttpClient client = new OkHttpClient();
        domain = domain.replaceFirst("^https?://", "");

        String url;

        switch (platform) {
            case MISSKEY:
                url = "https://" + domain + "/api/meta";
                break;
            case MASTODON:
                url = "https://" + domain + "/api/v1/instance";
                break;
            default:
                callback.onInvalidInstance("Tipo de plataforma no soportado.");
                return;
        }
        Log.d(TAG, "Instance written: " + url);

        Request.Builder requestBuilder = new Request.Builder().url(url);

        // Misskey usa POST Mastodon GET
        if (platform == PlatformType.MISSKEY) {
            RequestBody body = RequestBody.create("{}", MediaType.parse("application/json"));
            requestBuilder.post(body);
        } else {
            requestBuilder.get();
        }

        client.newCall(requestBuilder.build()).enqueue(new Callback() {

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

                    boolean valid;

                    valid = json.has("version") && json.has("uri");

                    if (valid) {
                        callback.onValidInstance(json);
                    } else {
                        callback.onInvalidInstance("Respuesta inválida para " + platform.name());
                    }

                } catch (JSONException e) {
                    callback.onInvalidInstance("Error al parsear respuesta JSON.");
                }
            }
        });
    }
}

