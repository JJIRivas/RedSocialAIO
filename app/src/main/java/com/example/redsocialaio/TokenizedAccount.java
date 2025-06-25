package com.example.redsocialaio;

/*Interfaz hecha al principio del proyecto... en teoria deberia ocuparse para cuentas que ocupen
Tokens de manera heavy, pero esta sin un uso realmente.*/
public interface TokenizedAccount {
    String getAccessToken();

    void setAccessToken(String token);

    void ObtainTokenToSave(String authCode);

    void savePersistentToken(String token);

    String loadPersistentToken();

    void refreshAccessToken(String refreshToken);
}
