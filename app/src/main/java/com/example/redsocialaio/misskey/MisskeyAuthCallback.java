package com.example.redsocialaio.misskey;

public interface MisskeyAuthCallback {
    void onAuthSuccess(String token);

    void onAuthFailed(String reason);
}
