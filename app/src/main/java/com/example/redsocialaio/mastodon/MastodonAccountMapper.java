package com.example.redsocialaio.mastodon;


import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MastodonAccountMapper {

    public static MastodonAccount fromJSON(JSONObject json) {
        MastodonAccount account = new MastodonAccount();

        account.setUserName(json.optString("username", ""));
        account.setUserID(json.optString("id", ""));
        account.setAvatarURL(json.optString("avatar", ""));
        account.setDisplayName(json.optString("display_name", ""));
        account.setPrivate(json.optBoolean("locked", false));
        account.setFollowersCount(json.optLong("followers_count", 0));
        account.setFollowingCount(json.optLong("following_count", 0));
        account.setPostsCount(json.optLong("statuses_count", 0));
        account.setBot(json.optBoolean("bot", false));

        return account;
    }

    public static Map<String, Object> toFirestoreMap(MastodonAccount account) {
        Map<String, Object> data = new HashMap<>();
        data.put("userName", account.getUserName());
        data.put("userID", account.getUserID());
        data.put("avatarURL", account.getAvatarURL());
        data.put("displayName", account.getDisplayName());
        data.put("instanceUrl", account.getInstanceUrl());
        data.put("followersCount", account.getFollowersCount());
        data.put("followingCount", account.getFollowingCount());
        data.put("postsCount", account.getPostsCount());
        data.put("isPrivate", account.isPrivate());
        data.put("isBot", account.isBot());
        return data;
    }

    public static MastodonAccount fromFirestoreMap(Map<String, Object> data) {
        MastodonAccount account = new MastodonAccount();

        account.setUserName((String) data.get("userName"));
        account.setUserID((String) data.get("userID"));
        account.setAvatarURL((String) data.get("avatarURL"));
        account.setDisplayName((String) data.get("displayName"));
        account.setInstanceUrl((String) data.get("instanceUrl"));

        account.setFollowersCount(((Number) data.getOrDefault("followersCount", 0)).longValue());
        account.setFollowingCount(((Number) data.getOrDefault("followingCount", 0)).longValue());
        account.setPostsCount(((Number) data.getOrDefault("postsCount", 0)).longValue());
        account.setPrivate(Boolean.TRUE.equals(data.get("isPrivate")));
        account.setBot(Boolean.TRUE.equals(data.get("isBot")));

        return account;
    }
}

