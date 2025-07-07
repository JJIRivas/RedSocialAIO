package com.example.redsocialaio.core.auth;

public interface AuthManager {
    void startAuthFlow(String instanceUrl);

    void handleCallback(String callbackData, String something, AuthCallback callback);

    interface AuthCallback {
        void onAuthSuccess(String token);

        void onAuthFailed(String reason);
    }
}
