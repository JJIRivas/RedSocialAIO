package com.example.redsocialaio;

import java.util.Map;

public interface CuentaUsuarioRedSocial {
    String getUserName();

    String getAccessToken();

    String getUserID();

    boolean IsConnected();

    void connect();

    void disconnect();

    Map<String, Object> toFirestoreData();

}
