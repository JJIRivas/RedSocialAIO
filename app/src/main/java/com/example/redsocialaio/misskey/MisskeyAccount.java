package com.example.redsocialaio.misskey;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MisskeyAccount {

    /*Las otras variables que estan comentadas pueden servir al usuario... pero ahora por
    simplicidad y tiempo no estan agregadas. Solo las dejo ahi comentadas para saber que existen y
    que nos podrian servir.*/
    private String userName;
    private String userID;
    private String avatarURL;
    private String name;
    private String followingVisibility; //Recibe "public", no true/false - por eso es String.
    private String followersVisibility; //Es String porque no recibe true/false, sino "public" o no.
    //private String publicReactions;
    //private String[] pinnedNotes;
    //private String pinnedPageId;
    private String accessToken;
    private String instanceUrl;
    private Long followersCount;
    private Long followingCount;
    private Long notesCount;
    //private List<String> pinnedNoteIds;
    //private boolean hideOnlineStatus;
    //private boolean hasUnreadSpecifiedNotes;
    //private boolean hasUnreadChatMessages;
    //private boolean hasUnreadAnnouncement;
    //private boolean hasUnreadMentions;
    //private boolean alwaysMarkNsfw;
    private boolean isPrivate;

    public MisskeyAccount() {
    }

    public MisskeyAccount(String userName, String userID, String avatarURL, String name,
                          String followingVisibility, String followersVisibility,
                          String accessToken, String instanceUrl,
                          Long followersCount, Long followingCount, Long notesCount, boolean isPrivate) {
        this.userName = userName;
        this.userID = userID;
        this.avatarURL = avatarURL;
        this.name = name;
        this.followingVisibility = followingVisibility;
        this.followersVisibility = followersVisibility;
        this.accessToken = accessToken;
        this.instanceUrl = instanceUrl;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
        this.notesCount = notesCount;
        this.isPrivate = isPrivate;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    /*Solo se encarga de colocar los datos del usuario que estan en nuestras variables en un formato
    de HashMap para guardarlos en Firestore.*/
    public Map<String, Object> toFirestoreMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("userName", userName);
        data.put("userID", userID);
        data.put("avatarURL", avatarURL);
        data.put("name", name);
        data.put("followingVisibility", followingVisibility);
        data.put("followersVisibility", followersVisibility);
        data.put("accessToken", accessToken);
        data.put("instanceUrl", instanceUrl);
        data.put("followersCount", followersCount);
        data.put("followingCount", followingCount);
        data.put("notesCount", notesCount);
        data.put("isPrivate", isPrivate);
        return data;
    }

    /*Metodo que, con el Uid del usuario actual, crea un directorio en firestore que guarda los
    usuarios de Misskey. Los datos que guarda son obtenidos luego de ser parseados a un HashMap de
    las variables... las cuales en si pueden ser asignadas por fromJSONObject o bien por
    fromFirestoreMap*/
    public void saveToFirestore(String firebaseUid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(firebaseUid)
                .collection("accounts")
                .document("misskey")
                .set(toFirestoreMap())
                .addOnSuccessListener(aVoid -> {
                    // TODO agregar logs
                })
                .addOnFailureListener(e -> {
                    // TODO agregar manejo excepciones
                });
    }

    /*Metodo que se ocupa al iniciar sesion - recibe los datos JSON del request a la API y los
    asigna a las variables que tenemos. Posiblemente podria ser ocupado igual par cuando el usuario
    modifique informacion en su cuenta.*/
    public void fromJSONObject(JSONObject json) {
        try {
            this.userName = json.optString("username", "");
            this.userID = json.optString("id", "");
            this.avatarURL = json.optString("avatarUrl", "");
            this.name = json.optString("name", "");
            this.followingVisibility = json.optString("followingVisibility", "");
            this.followersVisibility = json.optString("followersVisibility", "");
            this.notesCount = json.optLong("notesCount", 0);
            this.followingCount = json.optLong("followingCount", 0);
            this.followersCount = json.optLong("followersCount", 0);
            this.isPrivate = json.optBoolean("isPrivate", false);

        } catch (Exception e) {
            Log.e("MisskeyAccount", "Error al parsear JSON del usuario", e);
        }
    }

    /*Metodo todaiva no ocupado/implementado completamente, como dice el nombre quiero ocuparlo para
    "cargar" la informacion del usuario que se guardo en Firestore, donde sabemos *quien* es el
    usuario al llamar FirebaseFirestore.getInstance() y tambien ocupando el Uid asociado a esta.*/


    //Recordar que firestore guarda sus datos como HashMap, por ende se ocupa eso para trabajarlos.
    public static void loadFromFirestore(String firebaseUid, LoadCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(firebaseUid)
                .collection("accounts")
                .document("misskey")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        /*Si sabemos que existe la informacion del usuario, se crea un nuevo
                        objeto de la clase MisskeyAccount y enviamos la informacion a el metodo
                        fromFirestoreMap para guardarla en sus variables.*/

                        /*Idealmente, esto se podria refactorizar a una clase diferente para que
                        MisskeyAccount solo se ocupe de representar las cuentas como tal en vez de
                        tener que cargar datos igual.*/

                        //TODO - refactorizar logica guardado/carga de datos a otra clase.
                        MisskeyAccount account = new MisskeyAccount();
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data != null) {
                            account.fromFirestoreMap(data);
                            callback.onSuccess(account);
                        } else {
                            callback.onFailure("Datos vacíos");
                        }
                    } else {
                        callback.onFailure("No hay cuenta de Misskey guardada");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("Error al leer Firestore: " + e.getMessage());
                });
    }

    /*Metodo que igual todavia no es ocupado como tal, sino que solamente es llamada para guardar
    los datos obtenidos por loadFromFirestore. Este metodo los guarda en las variables que tenemos-
    idealmente seria ocupado cuando un usuario vuelve a iniciar la app para que no tenga que
    iniciar sesion cada una vez. Los "if" es simplemente para tener un resguardo que los Longs y
    Bollean no creen NullPointerException por algun motivo. ...se podria reemplazar por
    algo mas robusto idealmente.*/

    public void fromFirestoreMap(Map<String, Object> data) {
        this.userName = (String) data.get("userName");
        this.userID = (String) data.get("userID");
        this.avatarURL = (String) data.get("avatarURL");
        this.name = (String) data.get("name");
        this.followingVisibility = (String) data.get("followingVisibility");
        this.followersVisibility = (String) data.get("followersVisibility");
        this.accessToken = (String) data.get("accessToken");
        this.instanceUrl = (String) data.get("instanceUrl");

        Object followersObj = data.get("followersCount");
        if (followersObj instanceof Number) {
            this.followersCount = ((Number) followersObj).longValue();
        }

        Object followingObj = data.get("followingCount");
        if (followingObj instanceof Number) {
            this.followingCount = ((Number) followingObj).longValue();
        }

        Object notesObj = data.get("notesCount");
        if (notesObj instanceof Number) {
            this.notesCount = ((Number) notesObj).longValue();
        }

        Object privateObj = data.get("isPrivate");
        this.isPrivate = Boolean.TRUE.equals(privateObj);
    }

}

