package com.example.redsocialaio;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import java.util.Objects;

import social.bigbone.MastodonClient;
import social.bigbone.api.Scope;

import com.example.redsocialaio.mastodon.MastodonAccount;
import com.example.redsocialaio.mastodon.MastodonInstanceCreator;

/*Clase no ocupada. Hecha cuando se inicio el proyecto... no la borro porque no me he dado el
trabajo de revisarla de nuevo y ver que hacia o si es que sirve de algo para despues.*/
public class AuthCallbackActivity extends AppCompatActivity {
    public AuthCallbackActivity() {
    }

    MastodonInstanceCreator client = new MastodonInstanceCreator();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();

        if (uri != null && Objects.requireNonNull(uri.getScheme()).equals("redsocialaio")) {
            String authCode = uri.getQueryParameter("code");
            TokenManager tokenManager = new TokenManager();
            MastodonAccount mastodonAccount = new MastodonAccount();
            MastodonClient client = MastodonInstanceCreator.ClientCreator(mastodonAccount.getServerDomain());

            String url = client.oauth().getOAuthUrl(mastodonAccount.getUserID(), authCode, new Scope(Scope.READ.ALL, Scope.WRITE.ALL, Scope.PUSH.ALL));
            String accessToken = tokenManager.getAccessToken();

        }
    }
}

/*/*import java.util.HashMap;
import java.util.Map;

String url = client.getOauth().getOAuthUrl(appRegistration.getClientId(), new Scope());

// This URL will have the following format:
// https://<instance_name>/oauth/authorize?client_id=<client_id>&redirect_uri=<redirect_uri>&response_type=code&scope=<scope>

// Opening this URL will allow the user to perform an OAuth login, after which
// the previously defined redirect_uri will be called with an auth code in the query like this:
// <redirect_uri>?code=<auth_code>

String authCode = ...; // retrieved from redirect_uri query parameter

// we use this auth code to get an access token
String accessToken = client.getOauth().getAccessToken(
    appRegistration.getClientId(),
    appRegistration.getClientSecret(),
    appRegistration.getRedirectUri(),
    authCode,
    "authorization_code"
);

*/