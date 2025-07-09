package com.example.redsocialaio.mastodon;

import com.example.redsocialaio.core.repositories.SocialAccountInfo;

public class MastodonAccount extends SocialAccountInfo {

    private boolean isBot;

    @Override
    public String getNetworkType() {
        return "mastodon";
    }


    public boolean isBot() {
        return isBot;
    }

    public void setBot(boolean bot) {
        isBot = bot;
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


}
