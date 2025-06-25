package com.example.redsocialaio.misskey;

import org.json.JSONObject;

/*Interfaz que ocupamos en la validacion de instancia, revisar MisskeyInstanceInput y
MisskeyInstanceValidator para su uso. (MisskeyInstanceInput tiene la implementacion como tal)*/
public interface ValidationCallBack {
    void onValidInstance(JSONObject meta);

    void onInvalidInstance(String reason);
}
