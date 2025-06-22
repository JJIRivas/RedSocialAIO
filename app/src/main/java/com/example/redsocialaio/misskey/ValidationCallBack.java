package com.example.redsocialaio.misskey;

import org.json.JSONObject;

public interface ValidationCallBack {
    void onValidInstance(JSONObject meta);

    void onInvalidInstance(String reason);
}
