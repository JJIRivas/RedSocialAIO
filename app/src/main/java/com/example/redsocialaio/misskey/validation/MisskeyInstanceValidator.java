package com.example.redsocialaio.misskey.validation;

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
        OkHttpClient client = new OkHttpClient(); //Creamos un cliente para hacer requests a la API.

        String url = "https://" + domain + "/api/meta"; /*El url para verificar la informacion,
        sigue la forma https://instancia/api/meta, donde "instancia" en el nombre de la
        instancia como tal (Ej: para Misskey.social seria https://misskey.social/api/meta */


        RequestBody body = RequestBody.create("{}", MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();


        client.newCall(request).enqueue(new Callback() {

            /*Actualmente, si falla el request se hace un Log que menciona el error y se hace
              una version de onInvalidInstance de la interfaz ValidationCallbakc que solo
              avisa que ocurrio un error. TODO - Reemplazar por una validacion o mensajes mas explicatorios.*/

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
