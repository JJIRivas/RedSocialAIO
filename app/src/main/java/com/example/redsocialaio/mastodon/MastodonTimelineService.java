package com.example.redsocialaio.mastodon;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MastodonTimelineService {
    private static final String TAG = "MastodonTimelineService";

    private final Context context;
    private final OkHttpClient client;
    private final MastodonAccountService mastodonAccountService;

    public MastodonTimelineService(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
        this.mastodonAccountService = new MastodonAccountService(context);
    }

    /**
     * Obtiene el timeline home con parámetros por defecto
     */
    public CompletableFuture<List<MastodonStatusTimeline>> getHomeTimeline() {
        return getHomeTimeline(new TimelineRequest());
    }

    /**
     * Obtiene el timeline home con parámetros personalizados
     */
    public CompletableFuture<List<MastodonStatusTimeline>> getHomeTimeline(TimelineRequest request) {
        CompletableFuture<List<MastodonStatusTimeline>> future = new CompletableFuture<>();


        getAccountInfo().thenAccept(accountInfo -> {
            try {

                HttpUrl.Builder urlBuilder = HttpUrl.parse(accountInfo.getInstanceUrl() + "/api/v1/timelines/home").newBuilder()
                        .addQueryParameter("limit", String.valueOf(request.limit));

                // Parámetros opcionales para paginación
                if (request.maxId != null) {
                    urlBuilder.addQueryParameter("max_id", request.maxId);
                }
                if (request.sinceId != null) {
                    urlBuilder.addQueryParameter("since_id", request.sinceId);
                }
                if (request.minId != null) {
                    urlBuilder.addQueryParameter("min_id", request.minId);
                }

                Log.d(TAG, "Token: " + accountInfo.getToken());
                Log.d(TAG, "Instance: " + accountInfo.getInstanceUrl());
                Log.d(TAG, "URL: " + urlBuilder.build());

                Request httpRequest = new Request.Builder()
                        .url(urlBuilder.build())
                        .addHeader("Authorization", "Bearer " + accountInfo.getToken())
                        .build();

                // Ejecutar
                client.newCall(httpRequest).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "ERROR GETTING HOME TIMELINE", e);
                        future.completeExceptionally(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String responseBody = response.body().string();

                            Log.d(TAG, "HTTP Response: " + response.code());
                            Log.d(TAG, "Body preview: " + responseBody.substring(0, Math.min(200, responseBody.length())));

                            if (!response.isSuccessful()) {
                                throw new IOException("Error: " + response.code() + " - " + responseBody);
                            }

                            JSONArray statusesArray = new JSONArray(responseBody);

                            List<MastodonStatusTimeline> statuses = new ArrayList<>();
                            for (int i = 0; i < statusesArray.length(); i++) {
                                JSONObject statusJson = statusesArray.getJSONObject(i);
                                statuses.add(MastodonStatusTimeline.fromJSON(statusJson));
                            }

                            future.complete(statuses);

                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing timeline", e);
                            future.completeExceptionally(e);
                        }
                    }
                });

            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }


    /**
     * Obtiene el timeline público local
     */
    public CompletableFuture<List<MastodonStatusTimeline>> getPublicTimeline() {
        return getPublicTimeline(new TimelineRequest(), true);
    }

    /**
     * Obtiene el timeline público con parámetros
     */
    public CompletableFuture<List<MastodonStatusTimeline>> getPublicTimeline(TimelineRequest request, boolean local) {
        CompletableFuture<List<MastodonStatusTimeline>> future = new CompletableFuture<>();

        getAccountInfo().thenAccept(accountInfo -> {
            String instanceUrl = accountInfo.getInstanceUrl();
            if (instanceUrl == null) {
                instanceUrl = "https://mastodon.social"; // Fallback para timeline público
            }

            try {

                HttpUrl.Builder urlBuilder = HttpUrl.parse(instanceUrl + "/api/v1/timelines/public").newBuilder()
                        .addQueryParameter("limit", String.valueOf(request.limit))
                        .addQueryParameter("local", String.valueOf(local));

                // Parámetros opcionales para paginación
                if (request.maxId != null) {
                    urlBuilder.addQueryParameter("max_id", request.maxId);
                }
                if (request.sinceId != null) {
                    urlBuilder.addQueryParameter("since_id", request.sinceId);
                }

                Log.d(TAG, "Public timeline URL: " + urlBuilder.build());

                Request httpRequest = new Request.Builder()
                        .url(urlBuilder.build())
                        .build();

                // Ejecutar
                client.newCall(httpRequest).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "ERROR GETTING PUBLIC TIMELINE", e);
                        future.completeExceptionally(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String responseBody = response.body().string();

                            Log.d(TAG, "Public timeline HTTP Response: " + response.code());

                            if (!response.isSuccessful()) {
                                throw new IOException("Error: " + response.code() + " - " + responseBody);
                            }

                            JSONArray statusesArray = new JSONArray(responseBody);

                            List<MastodonStatusTimeline> statuses = new ArrayList<>();
                            for (int i = 0; i < statusesArray.length(); i++) {
                                JSONObject statusJson = statusesArray.getJSONObject(i);
                                statuses.add(MastodonStatusTimeline.fromJSON(statusJson));
                            }

                            Log.d(TAG, "Public timeline parsed: " + statuses.size() + " statuses");
                            future.complete(statuses);

                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing public timeline", e);
                            future.completeExceptionally(e);
                        }
                    }
                });

            } catch (Exception e) {
                future.completeExceptionally(e);
            }


        });
        return future;
    }

    /**
     * Clase para parámetros del timeline - similar a Misskey
     */
    public static class TimelineRequest {
        public int limit = 20;
        public String maxId = null;
        public String sinceId = null;
        public String minId = null;

        // Builder pattern para construcción fácil - igual que Misskey
        public TimelineRequest withLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public TimelineRequest withMaxId(String maxId) {
            this.maxId = maxId;
            return this;
        }

        public TimelineRequest withSinceId(String sinceId) {
            this.sinceId = sinceId;
            return this;
        }

        public TimelineRequest withMinId(String minId) {
            this.minId = minId;
            return this;
        }
    }

    /**
     * Helper para obtener token de acceso
     */
    private CompletableFuture<MastodonAccountService.AccountWithToken> getAccountInfo() {
        CompletableFuture<MastodonAccountService.AccountWithToken> future = new CompletableFuture<>();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            future.completeExceptionally(new Exception("No autenticado"));
            return future;
        }

        mastodonAccountService.getAccountWithToken(firebaseUser.getUid())
                .addOnSuccessListener(future::complete)
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }
}

