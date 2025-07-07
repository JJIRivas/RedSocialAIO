package com.example.redsocialaio.misskey.core.notes;

import org.json.JSONException;
import org.json.JSONObject;

public class MisskeyFile {
    private String id;
    private String type;
    private String url;
    private String thumbnailUrl;
    private boolean isSensitive;

    public static MisskeyFile fromJSON(JSONObject json) throws JSONException {
        MisskeyFile file = new MisskeyFile();

        file.id = json.getString("id");
        file.type = json.getString("type");
        file.url = json.getString("url");
        file.thumbnailUrl = json.optString("thumbnailUrl", file.url);
        file.isSensitive = json.optBoolean("isSensitive", false);

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

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public boolean isSensitive() {
        return isSensitive;
    }

    public boolean isImage() {
        return type != null && type.startsWith("image/");
    }
}
