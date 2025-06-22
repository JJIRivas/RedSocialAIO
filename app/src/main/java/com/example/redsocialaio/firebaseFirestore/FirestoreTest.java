package com.example.redsocialaio.firebaseFirestore;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreTest {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public void saveToFirestore() {

        if (user != null) {
            String uid = user.getUid();

            FirestoreConnection firestoreConnection = new FirestoreConnection("miUsuarioMastodon",
                    "tokenMastodon123",
                    "mastodon.social",
                    "miUsuarioMisskey",
                    "tokenMisskey456",
                    "misskey.io"
            );

            db.collection("users")
                    .document(uid)
                    .set(firestoreConnection)
                    .addOnSuccessListener(Void ->
                            Log.d("Firestore", "Datos guardados"))
                    .addOnFailureListener(e -> Log.w("Firestore", "Error, datos no guardados.", e));
        }
    }
}
