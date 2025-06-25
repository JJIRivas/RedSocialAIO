package com.example.redsocialaio.misskey;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

//Clase que hace una llamada al API de misskey para obtener la informacion del usuario.
public class ObtainMisskeyUserInfo {

    private static final String TAG = "ObtainMisskeyUserInfo"; //Tag para Logcat

    /*Aqui implementamos una interfaz local... mas que nada para no sobrecargar con una interfaz de
    nombre similar a las otras, pero seria mejor pasarla a una interfaz como tal en vez de local.*/
    public interface MisskeyUserInfoCallback {
        void onSuccess(JSONObject userInfo);

        void onFailure(String reason);
    }


    /*Teniendo el token del usuario, el url de su instancia, y ocupando la interfaz para los metodos
    internos a este, podemos hacer la llamada.*/
    public static void fetchUserInfo(String instanceUrl, String accessToken, MisskeyUserInfoCallback callback) {
        OkHttpClient client = new OkHttpClient();

        JSONObject requestBody = new JSONObject(); //Creamos un nuevo objeto Json que tenga como valor i y el token, que se necesitara enviar y recibir los datos del usuario.
        try {
            requestBody.put("i", accessToken);
        } catch (JSONException e) {
            callback.onFailure("No se pudo crear el JSON del cuerpo");
            return;
        }

        /*Hacemos el request con el url y pedimos la informacion de la cuenta relacionada
        al token que enviamos- creamos otro body para asegurarnos que este en el formato correcto
        antes de enviar el request como tal.*/

        //El url para pedir la informacion del usuario en Misskey sigue este orden: https://url/api/i, donde el url es el link de la instancia (ej: https://misskey.social/api/i)
        Request request = new Request.Builder()
                .url("https://" + instanceUrl + "/api/i")
                .post(RequestBody.create(
                        requestBody.toString(),
                        MediaType.parse("application/json; charset=utf-8")
                ))
                .build();

        //hacemos el request como tal, implementando los metodos de nuestra interfaz y la interfaz que ocupa Okhttp.
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

                /*A este punto, sabemos que deberia estar OK la respuesta ya que paso los dos if,
                entonces pasamos el cuerpo del JSON que recibimos a un string para luego... pasarlo
                de nuevo a Json, esto mas que nada para asegurarse que este ordenado y tenga toda
                la informacion.*/

                /*Notar que nuevamente dejamos a un metodo declarado pero sin cuerpo, este se
                implementa en otra clase, MisskeyAccount.*/
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
