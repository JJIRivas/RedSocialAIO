package com.example.redsocialaio;


import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

import com.example.redsocialaio.mastodon.MastodonAccount;
import com.example.redsocialaio.mastodon.MastodonAccountMapper;

import java.util.Optional;

public class MastodonAccountMapperTest {

    //Pruebas para fromJSON

    @Test
    public void fromJSONWithData() throws JSONException {

        String jsonString = "{"
                + "\"id\": \"12345\","
                + "\"username\": \"claude\","
                + "\"display_name\": \"Claude AI\","
                + "\"avatar\": \"http://example.com/avatar.png\","
                + "\"locked\": true,"
                + "\"followers_count\": 150,"
                + "\"following_count\": 50,"
                + "\"statuses_count\": 1000,"
                + "\"bot\": false"
                + "}";
        JSONObject json = new JSONObject(jsonString);

        MastodonAccount account = MastodonAccountMapper.fromJSON(json);

        assertNotNull(account);
        assertEquals("12345", account.getUserID());
        assertEquals("claude", account.getUserName());
        assertEquals("Claude AI", account.getDisplayName());
        assertEquals("http://example.com/avatar.png", account.getAvatarURL());
        assertTrue(account.isPrivate());
        assertEquals(Optional.of(150L), Optional.of(account.getFollowersCount()));
        assertEquals(Optional.of(50L), Optional.of(account.getFollowingCount()));
        assertEquals(Optional.of(1000L), Optional.of(account.getPostsCount()));
        assertFalse(account.isBot());
    }

    @Test
    public void fromJSONMissingData() throws JSONException {
        String jsonString = "{\"id\": \"67890\", \"username\": \"anthropic\"}";
        JSONObject json = new JSONObject(jsonString);

        MastodonAccount account = MastodonAccountMapper.fromJSON(json);

        assertNotNull(account);
        assertEquals("67890", account.getUserID());
        assertEquals("anthropic", account.getUserName());
        assertEquals("", account.getDisplayName());
        assertEquals("", account.getAvatarURL());
        assertFalse(account.isPrivate());
        assertEquals(Optional.of(0L), Optional.of(account.getFollowersCount()));
        assertFalse(account.isBot());
    }

    //Pruebas para toFirestoreMap

    @Test
    public void toFirestoreMapOK() {
        MastodonAccount account = new MastodonAccount();
        account.setUserID("user-1");
        account.setUserName("testuser");
        account.setFollowersCount(99L);
        account.setFollowingCount(199L);
        account.setPostsCount(299L);

        Map<String, Object> firestoreMap = MastodonAccountMapper.toFirestoreMap(account);

        assertEquals(99L, firestoreMap.get("followersCount"));
        assertEquals(199L, firestoreMap.get("followingCount"));
        assertEquals(299L, firestoreMap.get("postsCount"));
    }

    //Pruebas para fromFirestoreMap

    @Test
    public void fromFirestoreMapOK2() {
        Map<String, Object> firestoreMap = new HashMap<>();
        firestoreMap.put("userID", "firestore-user");
        firestoreMap.put("followersCount", 500L);
        firestoreMap.put("isPrivate", true);

        MastodonAccount account = MastodonAccountMapper.fromFirestoreMap(firestoreMap);

        assertNotNull(account);
        assertEquals("firestore-user", account.getUserID());
        assertEquals(Optional.of(500L), Optional.of(account.getFollowersCount()));
        assertTrue(account.isPrivate());
    }

    @Test
    public void fromFirestoreMapMissingData() {
        Map<String, Object> firestoreMap = new HashMap<>();
        firestoreMap.put("userID", "minimal-user");

        MastodonAccount account = MastodonAccountMapper.fromFirestoreMap(firestoreMap);

        assertNotNull(account);
        assertEquals(Optional.of(0L), Optional.of(account.getFollowersCount()));
        assertFalse(account.isPrivate());
    }
}

