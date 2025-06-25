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

/*Clase que se encarga de verificar si la instancia escrita por el usuario en MisskeyInstanceInput
es una instancia real o no.*/

public class MisskeyInstanceValidator {

    //String ocupado como un "TAG" para filtrar logs en LogCat.
    private static final String TAG = "MisskeyInstanceValidator";

    /*Metodo clave, se encarga de enviar el request a la url donde se puede verificar la
    informacion de la instancia- si *no* retorna nada o algo invalido entonces la instancia no
    existe. Recibe como parametros el nombre de la instancia (ej: misskey.social) y la interfaz
    ValidationCallback, donde se implementan los metodos aqui (adentro del metodo validateInstance)
    en vez de manera general (como un metodo separado en la clase).*/

    public static void validateInstance(String domain, ValidationCallBack callback) {
        OkHttpClient client = new OkHttpClient(); //Creamos un cliente para hacer requests a la API.

        String url = "https://" + domain + "/api/meta"; /*El url para verificar la informacion,
        sigue la forma https://instancia/api/meta, donde "instancia" en el nombre de la
        instancia como tal (Ej: para Misskey.social seria https://misskey.social/api/meta */

        /*reamos un request a la url, donde el "body" representa el JSON que retorna la API, las
         llaves vacias representan un cuerpo vacio que se llenara con la informacion obtenida-
         igual se especifica el tipo con MediaType.parse donde en este caso es JSON
         (basicamente asegurando al request que este es un JSON, evitando errores).*/
        RequestBody body = RequestBody.create("{}", MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        /*Aqui decimos que el request se hara dentro de esta parte con el enqueue- ocupamos la
         interfaz Callback de Okhttp que nos permite especificar que hacer en cada caso si la
         respuesta es exitosa o falla (onFailure y onResponse). Podemos ocupar la interfaz como
         entrada en el metodo para evitar la implementacion general de los metodos, lo que nos
         permite solo ocuparlos aqui- como se hizo con la interfaz ValidationCallback callback, la
         cual ocupamos adentro de los metodos onFailure y onResponse de hecho. (Entonces,
         quedan los metodos de la interfaz ValidationCallback adentro de los metodos de la interfaz
         Callback*/

        client.newCall(request).enqueue(new Callback() {

            /*Actualmente, si falla el request se hace un Log que menciona el error y se hace
              una version de onInvalidInstance de la interfaz ValidationCallbakc que solo
              avisa que ocurrio un error. TODO - Reemplazar por una validacion o mensajes mas explicatorios.*/

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Network error: " + e.getMessage());
                callback.onInvalidInstance("Network error: " + e.getMessage());
            }

            /*Si se recibe la respuesta de manera exitosa (asegurandonos nuevamente con el
            if(!response.isSuccessful(), pasa a lo siguiente.*/
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.w(TAG, "HTTP error code: " + response.code());
                    callback.onInvalidInstance("HTTP error code: " + response.code());
                    return;
                }

                String body = response.body().string();
                Log.d(TAG, "Response body: " + body); //nuevamente, para debug hacemos un LOG que muestra lo recibido en LogCat como un String.

                /*Aqui hacemos algo interesante (segun yo, por lo menos) - intentamos pasar el
                String de lo que recibimos a un  objeto JSON y logeamos la respuesta- si se falla
                el parseo entonces damos errores comunes, pero si todo va bien, instanciamos
                el metodo onValidInstance de la interfaz que creamos- pero *NO* lo implementamos
                como tal, sino que decimos "Alguna clase *debe* implementar este metodo" (por eso
                solo lo declaramos/instanciamos sin cuerpo). Hacemos esto para simplificar esta
                parte del codigo, evitando que se llene de muchas cosas que hacer.*/

                //Ah, y implementamos el onValidInstance como tal en la clase MisskeyInstanceInput.

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
