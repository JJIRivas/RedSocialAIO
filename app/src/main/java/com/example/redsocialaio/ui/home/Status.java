package com.example.redsocialaio.ui.home;

import com.google.gson.annotations.SerializedName;

/**
 * Status - Modelo de datos que representa un post de Mastodon
 * 
 * Contiene:
 * - ID único del post
 * - Contenido del mensaje
 * - Información del autor (Account)
 * - Estado de interacciones (favourited, reblogged)
 * - Contadores de likes y reblogs
 */
public class Status {
    // Datos básicos del post
    @SerializedName("id")
    private String id;
    
    @SerializedName("account")
    private Account account;
    
    @SerializedName("content")
    private String content;
    
    // Estado de interacciones del usuario actual
    @SerializedName("favourited")
    private boolean favourited;
    
    @SerializedName("reblogged")
    private boolean reblogged;
    
    // Contadores públicos
    @SerializedName("favourites_count")
    private int favouritesCount;
    
    @SerializedName("reblogs_count")
    private int reblogsCount;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isFavourited() { return favourited; }
    public void setFavourited(boolean favourited) { this.favourited = favourited; }
    public boolean isReblogged() { return reblogged; }
    public void setReblogged(boolean reblogged) { this.reblogged = reblogged; }
    public int getFavouritesCount() { return favouritesCount; }
    public void setFavouritesCount(int favouritesCount) { this.favouritesCount = favouritesCount; }
    public int getReblogsCount() { return reblogsCount; }
    public void setReblogsCount(int reblogsCount) { this.reblogsCount = reblogsCount; }

    /**
     * Account - Información del usuario autor del post
     * 
     * Contiene datos básicos del perfil como ID, nombre y avatar
     */
    public static class Account {
        @SerializedName("id")
        private String id;
        
        @SerializedName("username")
        private String username;
        
        @SerializedName("display_name")
        private String displayName;
        
        @SerializedName("avatar")
        private String avatar;
        
        @SerializedName("note")
        private String note;
        
        @SerializedName("statuses_count")
        private int statusesCount;
        
        @SerializedName("followers_count")
        private int followersCount;
        
        @SerializedName("following_count")
        private int followingCount;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        // Getter para biografía - retorna string vacío si es null
        public String getNote() { return note != null ? note : ""; }
        public void setNote(String note) { this.note = note; }
        
        // Getters y setters para estadísticas del usuario
        public int getStatusesCount() { return statusesCount; } // Número de posts publicados
        public void setStatusesCount(int statusesCount) { this.statusesCount = statusesCount; }
        public int getFollowersCount() { return followersCount; } // Número de seguidores
        public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }
        public int getFollowingCount() { return followingCount; } // Número de usuarios que sigue
        public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }
    }
}
