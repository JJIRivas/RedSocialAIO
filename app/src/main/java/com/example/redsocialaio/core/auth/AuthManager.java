package com.example.redsocialaio.core.auth;

import android.content.Context;

import com.example.redsocialaio.core.checkers.InstanceValidator;
import com.example.redsocialaio.mastodon.MastodonAuthManager;
import com.example.redsocialaio.misskey.auth.MisskeyAuthManager;

public interface AuthManager {

    void startAuthFlow(String instanceUrl);

    void handleCallback(String callbackData, String something, AuthCallback callback);

    interface AuthCallback {
        void onAuthSuccess(String token);

        void onAuthFailed(String reason);
    }

    class AuthManagerFactory {
        public static AuthManager create(InstanceValidator.PlatformType platform, Context context) {
            switch (platform) {
                case MISSKEY:
                    return new MisskeyAuthManager(context);
                case MASTODON:
                    return new MastodonAuthManager(context);
                default:
                    throw new IllegalArgumentException("Plataforma no soportada");
            }
        }
    }
}



