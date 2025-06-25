package com.example.redsocialaio;

import java.util.Map;


/*Intefaz hecha al principio del proyecto, en su momento debia representar cuentas de redes sociales
en general, pero especificamente la cuenta de FirebaseAuth.... podria ser util mas adelante.*/
public interface SocialAccountInfo {
    String getUserName();
    String getUserID();

    boolean IsPrivate();

    void connect(String authCode);

    void disconnect(String authCode);

    Map<String, Object> toFirestoreData();

}
