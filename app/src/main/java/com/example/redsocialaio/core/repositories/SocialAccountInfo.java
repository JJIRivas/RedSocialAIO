package com.example.redsocialaio.core.repositories;

import com.example.redsocialaio.core.checkers.InstanceValidator;

public abstract class SocialAccountInfo {
    protected String userName;
    protected String userID;
    protected String avatarURL;
    protected String displayName;
    protected String instanceUrl;
    protected Long followersCount;
    protected Long followingCount;
    protected Long postsCount;
    protected boolean isPrivate;
    private InstanceValidator.PlatformType platform;


    // Método abstracto que cada red debe implementar
    public abstract String getNetworkType();


    public SocialAccountInfo() {
    }
    public String getUserName() {
        return userName;
    }
    public String getUserID() {
        return userID;
    }
    public String getAvatarURL() {
        return avatarURL;
    }
    public String getDisplayName() {
        return displayName;
    }
    public String getInstanceUrl() {
        return instanceUrl;
    }
    public Long getFollowersCount() {
        return followersCount;
    }
    public Long getFollowingCount() {
        return followingCount;
    }
    public Long getPostsCount() {
        return postsCount;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public void setInstanceUrl(String instanceUrl) {
        this.instanceUrl = instanceUrl;
    }
    public void setFollowersCount(Long followersCount) {
        this.followersCount = followersCount;
    }
    public void setFollowingCount(Long followingCount) {
        this.followingCount = followingCount;
    }
    public void setPostsCount(Long postsCount) {
        this.postsCount = postsCount;
    }
    public boolean isPrivate() {
        return isPrivate;
    }
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

}
