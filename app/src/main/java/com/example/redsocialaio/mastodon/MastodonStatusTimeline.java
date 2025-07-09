package com.example.redsocialaio.mastodon;

import android.util.Log;

import com.example.redsocialaio.core.repositories.SocialAccountInfo;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MastodonStatusTimeline extends MastodonStatusBase {

    private SocialAccountInfo account;

    @Nullable
    private final List<MastodonFile> mediaAttachments;

    private int repliesCount = 0;
    private int reblogsCount = 0;
    private int favouritesCount = 0;

    private boolean favourited = false;
    private boolean reblogged = false;
    private boolean bookmarked = false;

    @Nullable
    private MastodonStatusTimeline reblog; // Si es reblog
    @Nullable
    private String inReplyToId;  // Si es respuesta
    @Nullable
    private String inReplyToAccountId;

    @Nullable
    private final List<String> tags;
    @Nullable
    private String language;
    @Nullable
    private String uri; // URL canónica

    public MastodonStatusTimeline() {
        this.mediaAttachments = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    public static MastodonStatusTimeline fromJSON(JSONObject json) throws JSONException {
        MastodonStatusTimeline status = new MastodonStatusTimeline();

        Log.d("I swear if the json is empty.", json.toString());
        // Campos básicos
        status.id = json.getString("id");
        status.content = json.optString("content", "");
        status.spoilerText = json.optString("spoiler_text", null);

        // DEBUG: Log para ver qué estamos parseando
        Log.d("MastodonParse", "Parseando status ID: " + status.id);
        Log.d("MastodonParse", "Content: " + status.content);


        // masodon usa ISO 8601
        String createdAtStr = json.getString("created_at");
        status.createdAt = parseDate(createdAtStr);


        String visibilityStr = json.optString("visibility", "public");
        status.visibility = Visibility.fromApiValue(visibilityStr);


        JSONObject accountJson = json.optJSONObject("account");
        if (accountJson != null) {
            status.account = createMastodonAccountFromJSON(accountJson);
        }


        status.repliesCount = json.optInt("replies_count", 0);
        status.reblogsCount = json.optInt("reblogs_count", 0);
        status.favouritesCount = json.optInt("favourites_count", 0);


        status.favourited = json.optBoolean("favourited", false);
        status.reblogged = json.optBoolean("reblogged", false);
        status.bookmarked = json.optBoolean("bookmarked", false);

        // Media
        JSONArray mediaArray = json.optJSONArray("media_attachments");
        if (mediaArray != null) {
            for (int i = 0; i < mediaArray.length(); i++) {
                JSONObject mediaJson = mediaArray.getJSONObject(i);
                status.mediaAttachments.add(MastodonFile.fromJSON(mediaJson));
            }
        }

        // Respuesta
        status.inReplyToId = json.optString("in_reply_to_id", null);
        status.inReplyToAccountId = json.optString("in_reply_to_account_id", null);

        // IMPORTANTE: Parsear reblog
        JSONObject reblogJson = json.optJSONObject("reblog");
        if (reblogJson != null) {
            Log.d("MastodonParse", "Parseando reblog...");
            try {
                status.reblog = MastodonStatusTimeline.fromJSON(reblogJson);
                Log.d("MastodonParse", "Reblog parseado - Content: " +
                        (status.reblog != null ? status.reblog.getContent() : "null"));
            } catch (Exception e) {
                Log.e("MastodonParse", "Error parseando reblog", e);
            }
        }

        // Tags
        JSONArray tagsArray = json.optJSONArray("tags");
        if (tagsArray != null) {
            for (int i = 0; i < tagsArray.length(); i++) {
                JSONObject tagJson = tagsArray.getJSONObject(i);
                status.tags.add(tagJson.getString("name"));
            }
        }


        status.language = json.optString("language", null);
        status.uri = json.optString("uri", null);

        return status;
    }

    // Helper para crear SocialAccountInfo desde JSON de Mastodon
    private static SocialAccountInfo createMastodonAccountFromJSON(JSONObject accountJson) throws JSONException {
        return new SocialAccountInfo() {
            {
                setUserID(accountJson.getString("id"));
                setUserName(accountJson.getString("username"));
                setDisplayName(accountJson.optString("display_name", accountJson.getString("username")));
                setAvatarURL(accountJson.getString("avatar"));
                setFollowersCount((long) accountJson.optInt("followers_count", 0));
                setFollowingCount((long) accountJson.optInt("following_count", 0));
                setPostsCount((long) accountJson.optInt("statuses_count", 0));
            }

            @Override
            public String getNetworkType() {
                return "mastodon";
            }
        };
    }


    public SocialAccountInfo getUser() {
        return account;
    }

    public List<MastodonFile> getFiles() {
        return mediaAttachments != null ? mediaAttachments : new ArrayList<>();
    }

    public int getRepliesCount() {
        return repliesCount;
    }

    public int getReblogsCount() {
        return reblogsCount;
    }

    public int getFavouritesCount() {
        return favouritesCount;
    }

    public boolean isFavourited() {
        return favourited;
    }

    public boolean isReblogged() {
        return reblogged;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public MastodonStatusTimeline getReblog() {
        return reblog;
    }

    public List<String> getTags() {
        return tags != null ? tags : new ArrayList<>();
    }

    public String getLanguage() {
        return language;
    }

    public String getUri() {
        return uri;
    }

    // Helper para parsear fecha igual que Misskey
    private static Date parseDate(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            return format.parse(dateStr);
        } catch (ParseException e) {
            try {
                SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                simpleFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                return simpleFormat.parse(dateStr);
            } catch (ParseException e2) {
                return new Date();
            }
        }
    }


    public boolean hasFiles() {
        return mediaAttachments != null && !mediaAttachments.isEmpty();
    }

    public boolean hasContentWarning() {
        return spoilerText != null && !spoilerText.isEmpty();
    }

    public boolean isRepost() {
        return reblog != null;
    }

    public boolean hasReacted() {
        return favourited;
    }


    public int getTotalReactions() {
        return favouritesCount;
    }

    public boolean isRenoted() {
        return reblogged;
    }

    public int getRenoteCount() {
        return reblogsCount;
    }

    public String getText() {
        return getContent();
    }
}

