package com.example.redsocialaio;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

// Aquí definimos la llamada a la API pública de Mastodon para obtener los posts públicos (timeline)
public interface MastodonApiService {
    @GET("api/v1/timelines/public")
    Call<List<Toot>> getPublicTimeline();
}
