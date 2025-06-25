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

    //Variables que representan el contexto, url de la instancia, donde retornar despues de iniciar sesion, etc. Los permisos que pide nuestra app son fullPermissions... son hartos ya que es un cliente.
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

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public MisskeyAuthManager(Context context, String instanceUrl) {
        this.context = context;
        this.instanceUrl = instanceUrl;
    }

    //Inicia el proceso de inicio sesion con la creacion del UID aleatorio de un uso para el URL
    public void startSession() {
        sessionToken = UUID.randomUUID().toString();
    }

    public String getSessionToken() {
        return sessionToken;
    }

    /*Genera el URL para el inicio de sesion/creacion de cuenta como tal, este se arma como un Uri
    para enviarse al navegador. Sigue la forma https://instancia/miauth/tokenUUid
    (ej: https://misskey.social/miauth/1234), el resto de los parametros es el nombre de la app,
    donde retornar una vez listo, y los permisos que se van a pedir. Este URL generado es para el
    usuario, no deberia retornar nada cuando se ocupa.*/

    /*Con respecto a callbackUrl- este es el "url" que indica a donde regresar luego de completar el
    proceso... esta definido en el AndroidManifest.xml y retorna el usuario a la clase MisskeyCallbackActivity
    (por eso el nombre)*/

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

    /*checkMiAuthSession es el metodo principal de esta clase, el nombre viene porque Misskey puede
    ocupar otro metodo de verificacion aparte de OAuth, el cual se denomino MiAuth, esta descrito
    en la documentacion de la API de manera basica, pero para verlo mas a fondo se reviso
    https://misskey.io/api-doc y https://misskey.social/api-doc - en teoria puede que cada una
    instancia siga unos endpoints diferentes, pero en practica el 90% de las instancias siguen
    los endpoints de la instancia principal "Misskey.io" */


    public void checkMiAuthSession(MisskeyAuthCallback callback) {

        //Primero, revisa si el token temporal  es nulo o no, si es, se retorna a la pantalla anterior.
        if (sessionToken == null) {
            callback.onAuthFailed("Sesion no iniciada.");
            return;
        }

        /*Definimos el url que *verifica* si es que el usuario pudo iniciar sesion,
        donde sigue un orden de https://url/api/miauth/token/check - Este URL *si* deberia retornar
        informacion, lo mas importante seria el URL de la cuenta del usuario como tal (considerando
        que el sessionToken es el URL temporal o UUIDToken (misma cosa).*/
        String url = "https://" + instanceUrl + "/api/miauth/" + sessionToken + "/check";
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        JSONObject requestBody = new JSONObject();

        /*En la siguiente parte se crea un request con la URL creada (la que termina con /check)
        en donde en el request se envia un JSON con el unico campo siendo token: UUID para que el
        API sepa que estamos pidiendo el token de esa cuenta que nos dio permiso como tal.*/

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

        /*Aqui nuevamente se ocupo la tecnica de una interfaz como variable de entrada, en donde
        se implemento como tal sus metodos en la clase MisskeyCallbackActivity. Notar que estan
        los metodos de la interfaz Callback (que es una interfaz necesaria por Okhttp), y los
        metodos de nuestra interfaz MisskeyAuthCallback*/
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

                /*Si lo anterior salio bien, entonces con el Json que se recibio de la llamada
                tendriamos el token "final" que es como accedemos a la cuenta del usuario.*/
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