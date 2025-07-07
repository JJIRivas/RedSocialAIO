package com.example.redsocialaio.misskey.core.notes;

import org.json.JSONObject;

public class MisskeyEmoji {
    private String name;        // ":blob_cat:"
    private String url;         // URL de la imagen
    private String category;    // Categoría del emoji
    private boolean isSensitive;

    public static MisskeyEmoji fromJSON(JSONObject json) {
        MisskeyEmoji emoji = new MisskeyEmoji();
        emoji.name = json.optString("name");
        emoji.url = json.optString("url");
        emoji.category = json.optString("category");
        emoji.isSensitive = json.optBoolean("isSensitive", false);
        return emoji;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public boolean isCustom() {
        return name.startsWith(":") && name.endsWith(":");
    }
}
