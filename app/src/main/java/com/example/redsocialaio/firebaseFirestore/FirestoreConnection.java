package com.example.redsocialaio.firebaseFirestore;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreConnection {

    private String mastodonUserName;
    private String mastodonAccessToken;
    private String mastodonServerDomain;

    private String misskeyUserName;
    private String misskeyAccessToken;
    private String misskeyServerDomain;

    public FirestoreConnection() {
    }

    public FirestoreConnection(String mastodonUserName, String mastodonAccessToken,
                               String mastodonServerDomain, String misskeyUserName,
                               String misskeyAccessToken, String misskeyServerDomain) {
        this.mastodonUserName = mastodonUserName;
        this.mastodonAccessToken = mastodonAccessToken;
        this.mastodonServerDomain = mastodonServerDomain;
        this.misskeyUserName = misskeyUserName;
        this.misskeyAccessToken = misskeyAccessToken;
        this.misskeyServerDomain = misskeyServerDomain;
    }

    public String getMastodonUserName() {
        return mastodonUserName;
    }

    public String getMastodonAccessToken() {
        return mastodonAccessToken;
    }

    public String getMastodonServerDomain() {
        return mastodonServerDomain;
    }

    public void setMastodonUserName(String mastodonUserName) {
        this.mastodonUserName = mastodonUserName;
    }

    public void setMastodonAccessToken(String mastodonAccessToken) {
        this.mastodonAccessToken = mastodonAccessToken;
    }

    public void setMastodonServerDomain(String mastodonServerDomain) {
        this.mastodonServerDomain = mastodonServerDomain;
    }

    public String getMisskeyUserName() {
        return misskeyUserName;
    }

    public String getMisskeyAccessToken() {
        return misskeyAccessToken;
    }

    public String getMisskeyServerDomain() {
        return misskeyServerDomain;
    }

    public void setMisskeyUserName(String misskeyUserName) {
        this.misskeyUserName = misskeyUserName;
    }

    public void setMisskeyAccessToken(String misskeyAccessToken) {
        this.misskeyAccessToken = misskeyAccessToken;
    }

    public void setMisskeyServerDomain(String misskeyServerDomain) {
        this.misskeyServerDomain = misskeyServerDomain;
    }

}
