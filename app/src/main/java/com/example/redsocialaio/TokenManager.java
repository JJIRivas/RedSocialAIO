package com.example.redsocialaio;


import static android.content.Intent.getIntent;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;


import java.io.IOException;
import java.security.GeneralSecurityException;

import social.bigbone.MastodonClient;
import social.bigbone.api.Scope;
import social.bigbone.api.entity.Instance;

/*Clase hecha al principio del proyecto... se encargaba de manejar tokens de manera segura.
Todavia podria ser ocupada, pero necesitaria un rework grande.*/
public class TokenManager extends AppCompatActivity implements TokenizedAccount {

    public TokenManager() {
    }

    private static final String PREF_FILE = "secure_tokens";
    private static final String TOKEN_KEY = "mastodon_token";
    private SharedPreferences sharedPreferences;

    public TokenManager(Context context) throws GeneralSecurityException, IOException {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        sharedPreferences = EncryptedSharedPreferences.create(
                context,
                PREF_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    @Override
    public void savePersistentToken(String token) {
        sharedPreferences.edit().putString(TOKEN_KEY, token).apply();
    }

    @Override
    public void ObtainTokenToSave(String authCode) {
        //TODO
    }

    @Override
    public String getAccessToken() {
//        final String instanceName;
//        final String clientId;
//        final String clientSecret;
//        final String redirectUri;
//        MastodonAccount mastodonUserAccount = new MastodonAccount();
//
//        final MastodonClient client = MastodonInstanceCreator.ClientCreator((mastodonUserAccount.getServerDomain()));
//        instanceName = client.getInstance().getDomain();
//        clientId = mastodonUserAccount.getUserID();
//
//        final Scope fullScope = new Scope(Scope.READ.ALL, Scope.WRITE.ALL, Scope.PUSH.ALL);
//        final String state = "example_state";
//
//        Uri uri = getIntent().getData();
//        redirectUri = uri.g
//        final String codeVerifier = "8XGvwPXc434EwIYnNbw1w-RZhuM-OoYwxqDuz-At1kOmPgKi659mJPjF3YhDq6r5O_7vCT-UGiJ-5uYp5oNqg6-crINhv4AcUk0ewFmY-vtiwaE5iqAdcSUNAkQ3IJ7D";
//        final String codeChallenge = "w-5-Rln3XgXQvzuUS7IBduSwyN7D39z1d7JSd8YJfzM";
//
//        final String url = client.oauth().getOAuthUrl(clientId, redirectUri, fullScope, state, codeChallenge, "S256");
//
//        //TODO - Check if its really necessary to have this method considering loadPersistentToken already does the same/something similar.
        return "";
    }

    @Override
    public void setAccessToken(String token) {
        savePersistentToken(token);
    }

    @Override
    public String loadPersistentToken() {
        return sharedPreferences.getString(TOKEN_KEY, null);
    }

    @Override
    public void refreshAccessToken(String refreshToken) {
        //TODO
    }

    public void clearToken() {
        sharedPreferences.edit().remove(TOKEN_KEY).apply();
    }
}
