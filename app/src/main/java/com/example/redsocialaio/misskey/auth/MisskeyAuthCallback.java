package com.example.redsocialaio.misskey.auth;

public interface MisskeyAuthCallback {
    void onAuthSuccess(String token);

    void onAuthFailed(String reason);
}
