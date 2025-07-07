package com.example.redsocialaio.misskey.core.notes;

import androidx.annotation.Nullable;

import com.example.redsocialaio.misskey.core.MisskeyAccount;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class MisskeyNoteBase {




    /*
    private boolean noExtractMentions; // def false
    private boolean noExtractHashtags; // def false
    private boolean noExtractEmojis; // def false
    private Set<String> mediaIds;

    private boolean isHidden;

    private Set<String> visibleUserIds;

    private boolean localOnly; // def false
    private ReactionAcceptance reactionAcceptance;


    private boolean local;


    */

    protected String id; //postId
    protected Date createdAt;
    protected String text; //max char 3000
    @Nullable
    protected String cw; //Content warning - max 100 char
    protected String userId; // id del usuario que creo el note
    protected MisskeyAccount user;
    protected Visibility visibility = Visibility.PUBLIC; // def public


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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Nullable
    public String getCw() {
        return cw;
    }

    public void setCw(@Nullable String cw) {
        this.cw = cw;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public MisskeyAccount getUser() {
        return user;
    }

    public void setUser(MisskeyAccount user) {
        this.user = user;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }


}
