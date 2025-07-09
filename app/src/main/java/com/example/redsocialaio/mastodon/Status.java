package com.example.redsocialaio.mastodon;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Status - Modelo de datos que representa un post de Mastodon
 * <p>
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
    private MastodonAccount mastodonAccount;

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

    @SerializedName("media_attachments")
    private List<MastodonFile> mediaAttachments;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MastodonAccount getAccount() {
        return mastodonAccount;
    }

    public void setAccount(MastodonAccount account) {
        this.mastodonAccount = account;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isFavourited() {
        return favourited;
    }

    public void setFavourited(boolean favourited) {
        this.favourited = favourited;
    }

    public boolean isReblogged() {
        return reblogged;
    }

    public void setReblogged(boolean reblogged) {
        this.reblogged = reblogged;
    }

    public int getFavouritesCount() {
        return favouritesCount;
    }

    public void setFavouritesCount(int favouritesCount) {
        this.favouritesCount = favouritesCount;
    }

    public int getReblogsCount() {
        return reblogsCount;
    }

    public void setReblogsCount(int reblogsCount) {
        this.reblogsCount = reblogsCount;
    }


}
