package com.example.redsocialaio.core.repositories;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Task;

import java.util.Map;

public abstract class BaseSocialAccountRepository<T extends SocialAccountInfo> {
    protected final FirebaseFirestore db;
    private static final String USERS_COLLECTION = "users";
    private static final String ACCOUNTS_COLLECTION = "accounts";

    public BaseSocialAccountRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    protected abstract String getDocumentName();

    protected abstract Map<String, Object> toFirestoreMap(T account);

    protected abstract T fromFirestoreMap(Map<String, Object> data);

    public Task<Void> saveAccount(String firebaseUid, T account) {
        Map<String, Object> data = toFirestoreMap(account);
        return db.collection(USERS_COLLECTION)
                .document(firebaseUid)
                .collection(ACCOUNTS_COLLECTION)
                .document(getDocumentName())
                .set(data);
    }

    public Task<T> loadAccount(String firebaseUid) {
        return db.collection(USERS_COLLECTION)
                .document(firebaseUid)
                .collection(ACCOUNTS_COLLECTION)
                .document(getDocumentName())
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        Map<String, Object> data = task.getResult().getData();
                        if (data != null) {
                            return fromFirestoreMap(data);
                        }
                    }
                    throw new Exception("No se encontró cuenta de " + getDocumentName());
                });
    }
}
