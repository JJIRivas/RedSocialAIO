package com.example.redsocialaio;

import java.util.Map;

public interface SocialAccountInfo {
    String getUserName();
    String getUserID();

    boolean IsPrivate();

    void connect(String authCode);

    void disconnect(String authCode);

    Map<String, Object> toFirestoreData();

}
