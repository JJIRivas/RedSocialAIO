package com.example.redsocialaio.misskey.core;

import com.example.redsocialaio.core.repositories.SocialAccountInfo;

public class MisskeyAccount extends SocialAccountInfo {

    /*Las otras variables que estan comentadas pueden servir al usuario... pero ahora por
    simplicidad y tiempo no estan agregadas. Solo las dejo ahi comentadas para saber que existen y
    que nos podrian servir.*/

    /*private String publicReactions;
    private String[] pinnedNotes;
    private String pinnedPageId;
    private List<String> pinnedNoteIds;
    private boolean hideOnlineStatus;
    private boolean hasUnreadSpecifiedNotes;
    private boolean hasUnreadChatMessages;
    private boolean hasUnreadAnnouncement;
    private boolean hasUnreadMentions;
    private boolean alwaysMarkNsfw;*/

    private String followingVisibility; //Recibe "public", no true/false - por eso es String.
    private String followersVisibility; //Es String porque no recibe true/false, sino "public" o no.


    @Override
    public String getNetworkType() {
        return "misskey";
    }


    public String getFollowingVisibility() {
        return followingVisibility;
    }

    public void setFollowingVisibility(String followingVisibility) {
        this.followingVisibility = followingVisibility;
    }

    public String getFollowersVisibility() {
        return followersVisibility;
    }

    public void setFollowersVisibility(String followersVisibility) {
        this.followersVisibility = followersVisibility;
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


    /*Metodo que, con el Uid del usuario actual, crea un directorio en firestore que guarda los
    usuarios de Misskey. Los datos que guarda son obtenidos luego de ser parseados a un HashMap de
    las variables... las cuales en si pueden ser asignadas por fromJSONObject o bien por
    fromFirestoreMap*/


    /*Metodo que se ocupa al iniciar sesion - recibe los datos JSON del request a la API y los
    asigna a las variables que tenemos. Posiblemente podria ser ocupado igual par cuando el usuario
    modifique informacion en su cuenta.*/


    /*Metodo todaiva no ocupado/implementado completamente, como dice el nombre quiero ocuparlo para
    "cargar" la informacion del usuario que se guardo en Firestore, donde sabemos *quien* es el
    usuario al llamar FirebaseFirestore.getInstance() y tambien ocupando el Uid asociado a esta.*/


    //Recordar que firestore guarda sus datos como HashMap, por ende se ocupa eso para trabajarlos.


    /*Metodo que igual todavia no es ocupado como tal, sino que solamente es llamada para guardar
    los datos obtenidos por loadFromFirestore. Este metodo los guarda en las variables que tenemos-
    idealmente seria ocupado cuando un usuario vuelve a iniciar la app para que no tenga que
    iniciar sesion cada una vez. Los "if" es simplemente para tener un resguardo que los Longs y
    Bollean no creen NullPointerException por algun motivo. ...se podria reemplazar por
    algo mas robusto idealmente.*/


}

