package com.example.redsocialaio.misskey;

/*Intefaz de utilidad que sirve para haqcer cosas en un caso exitoso (onSuccess) o en un error
(onFailure). Actualmente solo ocupada en la clase MisskeyAccount pero no implementada como tal,
se planea ocupar para lograr guardar el token de manera local y cifrada, permitiendo al usuario
abrir la app y acceder al contenido sin tener que iniciar sesion cada una vez que abre la app.*/

public interface LoadCallback {
    void onSuccess(MisskeyAccount account);

    void onFailure(String reason);
}
