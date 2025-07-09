package com.example.redsocialaio.mastodon;


import androidx.annotation.Nullable;

import com.example.redsocialaio.core.repositories.SocialAccountInfo;

import java.util.Date;

public class MastodonStatusBase {

    protected String id; // statusId
    protected Date createdAt;
    protected String content; // max char 500 para Mastodon
    @Nullable
    protected String spoilerText; // Content warning en Mastodon - max 500 char
    protected String accountId; // id del usuario que creó el status
    protected SocialAccountInfo account;
    protected Visibility visibility = Visibility.PUBLIC; // def public

    // Enum para visibilidad - similar a Misskey pero con valores de Mastodon
    public enum Visibility {
        PUBLIC("public"),
        UNLISTED("unlisted"),
        PRIVATE("private"),    // followers-only
        DIRECT("direct");      // direct message

        private final String apiValue;

        Visibility(String apiValue) {
            this.apiValue = apiValue;
        }

        public String getApiValue() {
            return apiValue;
        }

        public static Visibility fromApiValue(String value) {
            for (Visibility v : values()) {
                if (v.apiValue.equals(value)) {
                    return v;
                }
            }
            return PUBLIC; // fallback
        }
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Nullable
    public String getSpoilerText() {
        return spoilerText;
    }

    public void setSpoilerText(@Nullable String spoilerText) {
        this.spoilerText = spoilerText;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public SocialAccountInfo getAccount() {
        return account;
    }

    public void setAccount(SocialAccountInfo account) {
        this.account = account;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }
}

