package com.example.redsocialaio.mastodon;

import org.json.JSONException;
import org.json.JSONObject;

public class MastodonFile {
    private String id;
    private String type; // "image", "video", "audio", "unknown"
    private String url;
    private String previewUrl;
    private String remoteUrl;
    private boolean sensitive;
    private String description; // alt text
    private String blurhash; // Para preview placeholders

    // Constructor desde JSON
    public static MastodonFile fromJSON(JSONObject json) throws JSONException {
        MastodonFile file = new MastodonFile();

        file.id = json.getString("id");
        file.type = json.getString("type");
        file.url = json.getString("url");
        file.previewUrl = json.optString("preview_url", file.url);
        file.remoteUrl = json.optString("remote_url", "");
        file.sensitive = json.optBoolean("sensitive", false);
        file.description = json.optString("description", "");
        file.blurhash = json.optString("blurhash", "");


        return file;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    // Helper methods - similar a MisskeyFile
    public boolean isImage() {
        return "image".equals(type);
    }

    public boolean isVideo() {
        return "video".equals(type);
    }

    public boolean isAudio() {
        return "audio".equals(type);
    }

    public String getBlurhash() {
        return blurhash;
    }

    public void setBlurhash(String blurhash) {
        this.blurhash = blurhash;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean hasDescription() {
        return description != null && !description.isEmpty();
    }
}

