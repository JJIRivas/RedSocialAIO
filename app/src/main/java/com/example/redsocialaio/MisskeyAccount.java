package com.example.redsocialaio;

import java.util.Collections;
import java.util.Map;

public class MisskeyAccount implements CuentaUsuarioRedSocial {
    @Override
    public String getUserName() {
        return "";
    }

    @Override
    public String getAccessToken() {
        return "";
    }

    @Override
    public String getUserID() {
        return "";
    }

    @Override
    public boolean IsConnected() {
        return false;
    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public Map<String, Object> toFirestoreData() {
        return Collections.emptyMap();
    }
}
