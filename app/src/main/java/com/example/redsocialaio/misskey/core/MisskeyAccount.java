package com.example.redsocialaio.misskey.core;

import com.example.redsocialaio.core.repositories.SocialAccountInfo;

public class MisskeyAccount extends SocialAccountInfo {


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



}

