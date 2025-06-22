package com.example.redsocialaio.misskey;

public interface MisskeyAuth {

    void startMiAuthSession(String instanceUrl);

    void receiveMiAuthToken(String sessionToken);

    String getAccessToken();

    void saveToken(String token);

    String loadToken();
}