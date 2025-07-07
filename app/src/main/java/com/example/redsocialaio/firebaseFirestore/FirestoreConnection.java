package com.example.redsocialaio.firebaseFirestore;

//
//public abstract class FirestoreConnection {
//
//    public static void loadFromFirestore(String firebaseUid, LoadCallback callback) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("users")
//                .document(firebaseUid)
//                .collection("accounts")
//                .document("misskey")
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//
//                        /*Si sabemos que existe la informacion del usuario, se crea un nuevo
//                        objeto de la clase MisskeyAccount y enviamos la informacion a el metodo
//                        fromFirestoreMap para guardarla en sus variables.*/
//
//                        /*Idealmente, esto se podria refactorizar a una clase diferente para que
//                        MisskeyAccount solo se ocupe de representar las cuentas como tal en vez de
//                        tener que cargar datos igual.*/
//
//                        MisskeyAccount account = new MisskeyAccount();
//                        Map<String, Object> data = documentSnapshot.getData();
//                        if (data != null) {
//                            account.fromFirestoreMap(data);
//                            callback.onSuccess(account);
//                        } else {
//                            callback.onFailure("Datos vacíos");
//                        }
//                    } else {
//                        callback.onFailure("No hay cuenta de Misskey guardada");
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    callback.onFailure("Error al leer Firestore: " + e.getMessage());
//                });
//    }
//
//
//}
