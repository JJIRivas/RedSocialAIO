package com.example.redsocialaio;

import com.google.gson.annotations.SerializedName;

// Representa un post (toot) que viene desde la API de Mastodon
public class Toot {
    @SerializedName("id")
    private String id;

    @SerializedName("content")
    private String content;

    @SerializedName("created_at")
    private String createdAt;

    // Getters para obtener los datos desde fuera de la clase
    public String getId() { return id; }
    public String getContent() { return content; }
    public String getCreatedAt() { return createdAt; }
}
