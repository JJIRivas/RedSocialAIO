package com.example.redsocialaio.mastodon;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

public class MastodonAccountRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<Void> saveAccount(String firebaseUid, MastodonAccount account) {
        return db.collection("users")
                .document(firebaseUid)
                .collection("mastodon_accounts")
                .document(account.getUserID())
                .set(MastodonAccountMapper.toFirestoreMap(account));
    }

    public Task<MastodonAccount> loadAccount(String firebaseUid) {
        return db.collection("users")
                .document(firebaseUid)
                .collection("mastodon_accounts")
                .limit(1)
                .get()
                .continueWith(snapshotTask -> {
                    if (!snapshotTask.isSuccessful()) {
                        throw snapshotTask.getException();
                    }

                    QuerySnapshot query = snapshotTask.getResult();
                    if (query == null || query.isEmpty()) {
                        throw new Exception("No se encontró cuenta de Mastodon");
                    }

                    DocumentSnapshot doc = query.getDocuments().get(0);
                    return MastodonAccountMapper.fromFirestoreMap(doc.getData());
                });
    }
}

