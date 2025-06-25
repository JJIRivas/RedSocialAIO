package com.example.redsocialaio.firebaseFirestore;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/*Clase hecha al principio del proyecto- debia guardar datos de Mastodon en firestore.... nose
que tan bien escrita esta.*/
public class MastodonConnection {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public MastodonConnection() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void saveMastodonAccount(String username, String serverDomain, String accessToken) {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.e("MastodonConnection", "Usuario no autenticado.");
            return;
        }

        String uid = currentUser.getUid();

        // Creamos el objeto que se va a guardar
        Map<String, Object> mastodonData = new HashMap<>();
        mastodonData.put("username", username);
        mastodonData.put("serverDomain", serverDomain);
        mastodonData.put("accessToken", accessToken); // Recomendado cifrar si hay tiempo

        // Guardamos en: users/{uid}/mastodon/account
        db.collection("users")
                .document(uid)
                .collection("mastodon")
                .document("account")
                .set(mastodonData)
                .addOnSuccessListener(aVoid -> Log.d("MastodonConnection", "Cuenta Mastodon guardada con éxito."))
                .addOnFailureListener(e -> Log.e("MastodonConnection", "Error al guardar Mastodon", e));
    }
}


