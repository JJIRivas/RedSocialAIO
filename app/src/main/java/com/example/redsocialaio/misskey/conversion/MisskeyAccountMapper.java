package com.example.redsocialaio.misskey.conversion;

import com.example.redsocialaio.misskey.core.MisskeyAccount;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MisskeyAccountMapper {

    public static MisskeyAccount fromJSON(JSONObject json) {
        MisskeyAccount account = new MisskeyAccount();

        account.setUserName(json.optString("username", ""));
        account.setUserID(json.optString("id", ""));
        account.setAvatarURL(json.optString("avatarUrl", ""));
        account.setDisplayName(json.optString("name", ""));
        account.setFollowingVisibility(json.optString("followingVisibility", "public"));
        account.setFollowersVisibility(json.optString("followersVisibility", "public"));
        account.setPostsCount(json.optLong("notesCount", 0));
        account.setFollowingCount(json.optLong("followingCount", 0));
        account.setFollowersCount(json.optLong("followersCount", 0));
        account.setPrivate(json.optBoolean("isPrivate", false));

        return account;
    }

    public static Map<String, Object> toFirestoreMap(MisskeyAccount account) {
        Map<String, Object> data = new HashMap<>();
        data.put("userName", account.getUserName());
        data.put("userID", account.getUserID());
        data.put("avatarURL", account.getAvatarURL());
        data.put("displayName", account.getDisplayName());
        data.put("followingVisibility", account.getFollowingVisibility());
        data.put("followersVisibility", account.getFollowersVisibility());
        data.put("instanceUrl", account.getInstanceUrl());
        data.put("followersCount", account.getFollowersCount());
        data.put("followingCount", account.getFollowingCount());
        data.put("notesCount", account.getPostsCount());
        data.put("isPrivate", account.isPrivate());
        // NUNCA incluir el token aquí
        return data;
    }

    public static MisskeyAccount fromFirestoreMap(Map<String, Object> data) {
        MisskeyAccount account = new MisskeyAccount();

        account.setUserName((String) data.get("userName"));
        account.setUserID((String) data.get("userID"));
        account.setAvatarURL((String) data.get("avatarURL"));
        account.setDisplayName((String) data.get("displayName"));
        account.setFollowingVisibility((String) data.get("followingVisibility"));
        account.setFollowersVisibility((String) data.get("followersVisibility"));
        account.setInstanceUrl((String) data.get("instanceUrl"));

        // Manejo seguro de números
        Object followersObj = data.get("followersCount");
        if (followersObj instanceof Number) {
            account.setFollowersCount(((Number) followersObj).longValue());
        }

        Object followingObj = data.get("followingCount");
        if (followingObj instanceof Number) {
            account.setFollowingCount(((Number) followingObj).longValue());
        }

        Object notesObj = data.get("notesCount");
        if (notesObj instanceof Number) {
            account.setPostsCount(((Number) notesObj).longValue());
        }

        account.setPrivate(Boolean.TRUE.equals(data.get("isPrivate")));

        return account;
    }
}