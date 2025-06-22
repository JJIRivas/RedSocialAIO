package com.example.redsocialaio.misskey;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MisskeyAccount {

    private String userName;
    private String userID;
    private boolean isPrivate;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public Map<String, Object> toFirestoreData() {
        Map<String, Object> data = new HashMap<>();
        data.put("userName", userName);
        data.put("userID", userID);
        data.put("isPrivate", isPrivate);
        return data;
    }
}

