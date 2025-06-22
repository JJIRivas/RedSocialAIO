package com.example.redsocialaio.mastodon;

import com.example.redsocialaio.SocialAccountInfo;
import com.example.redsocialaio.TokenizedAccount;

import java.util.Collections;
import java.util.Map;

public class MastodonAccount implements TokenizedAccount, SocialAccountInfo {
    @Override
    public String getUserName() {
        return "";
    }

    @Override
    public String getAccessToken() {
        return "";
    }

    @Override
    public void setAccessToken(String token) {

    }

    @Override
    public void ObtainTokenToSave(String authCode) {

    }

    @Override
    public void savePersistentToken(String token) {

    }

    @Override
    public String loadPersistentToken() {
        return "";
    }

    @Override
    public void refreshAccessToken(String refreshToken) {

    }

    @Override
    public String getUserID() {
        return "";
    }

    @Override
    public boolean IsPrivate() {
        return false;
    }

    @Override
    public void connect(String authCode) {

    }

    @Override
    public void disconnect(String authCode) {

    }

    @Override
    public Map<String, Object> toFirestoreData() {
        return Collections.emptyMap();
    }

    public String getServerDomain() {
        return "";
    }
}
