package com.example.redsocialaio;

public interface TokenizedAccount {
    String getAccessToken();

    void setAccessToken(String token);

    void ObtainTokenToSave(String authCode);

    void savePersistentToken(String token);

    String loadPersistentToken();

    void refreshAccessToken(String refreshToken);
}
