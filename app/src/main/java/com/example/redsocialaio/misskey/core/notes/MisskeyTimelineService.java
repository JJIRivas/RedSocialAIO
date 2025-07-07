package com.example.redsocialaio.misskey.core.notes;

import android.content.Context;
import android.util.Log;

import com.example.redsocialaio.misskey.core.notes.MisskeyNoteTimeline;
import com.example.redsocialaio.misskey.userInfo.MisskeyAccountService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MisskeyTimelineService {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final Context context;
    private final OkHttpClient client;
    private final MisskeyAccountService accountService;

    public MisskeyTimelineService(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
        this.accountService = new MisskeyAccountService(context);
    }

    /**
     * Obtiene el timeline con parámetros por defecto
     */
    public CompletableFuture<List<MisskeyNoteTimeline>> getHomeTimeline() {
        return getHomeTimeline(new TimelineRequest());
    }

    /**
     * Obtiene el timeline con parámetros personalizados
     */
    public CompletableFuture<List<MisskeyNoteTimeline>> getHomeTimeline(TimelineRequest request) {
        CompletableFuture<List<MisskeyNoteTimeline>> future = new CompletableFuture<>();

        // Obtener cuenta e info
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            future.completeExceptionally(new Exception("No autenticado"));
            return future;
        }

        accountService.getAccountWithToken(user.getUid())
                .addOnSuccessListener(result -> {
                    try {
                        // Crear request body con todos los parámetros posibles
                        JSONObject body = new JSONObject();
                        body.put("i", result.getToken());
                        body.put("limit", request.limit);

                        // Parámetros opcionales para paginación
                        if (request.sinceId != null) {
                            body.put("sinceId", request.sinceId);
                        }
                        if (request.untilId != null) {
                            body.put("untilId", request.untilId);
                        }
                        if (request.sinceDate > 0) {
                            body.put("sinceDate", request.sinceDate);
                        }
                        if (request.untilDate > 0) {
                            body.put("untilDate", request.untilDate);
                        }

                        // Canal específico
                        if (request.channelId != null) body.put("channelId", request.channelId);

                        // Permitir respuestas parciales
                        body.put("allowPartial", request.allowPartial);

                        Log.d("MisskeyTimelineService", "Token: " + result.getToken());
                        Log.d("MisskeyTimelineService", "Instance: " + result.getInstanceUrl());


                        Request httpRequest = new Request.Builder()
                                .url("https://" + result.getInstanceUrl() + "/api/notes/local-timeline")
                                .post(RequestBody.create(body.toString(), JSON))
                                .build();

                        Log.d("MisskeyTimelineService", "Request: " + body);
                        // Ejecutar
                        client.newCall(httpRequest).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.d("TIMELINEREQUEST", "ERROR GETTING TIMELINE");
                                future.completeExceptionally(e);
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    String responseBody = response.body().string();
                                    JSONArray notesArray = new JSONArray(responseBody);

                                    Log.d("MisskeyTimelineService", "HTTP Response: " + response.code());
                                    Log.d("MisskeyTimelineService", "Body: " + responseBody);

                                    List<MisskeyNoteTimeline> notes = new ArrayList<>();
                                    for (int i = 0; i < notesArray.length(); i++) {
                                        JSONObject noteJson = notesArray.getJSONObject(i);
                                        notes.add(MisskeyNoteTimeline.fromJSON(noteJson));
                                    }

                                    future.complete(notes);

                                } catch (Exception e) {
                                    future.completeExceptionally(e);
                                }
                            }
                        });

                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    /**
     * Clase para parámetros del timeline
     */
    public static class TimelineRequest {
        public String channelId = null;
        public int limit = 20;
        public String sinceId = null;  // Para obtener posts más nuevos que este ID
        public String untilId = null;  // Para obtener posts más viejos que este ID (paginación)
        public long sinceDate = 0;     // Unix timestamp en milisegundos
        public long untilDate = 0;     // Unix timestamp en milisegundos
        public boolean allowPartial = false;

        // Builder pattern para construcción fácil
        public TimelineRequest withLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public TimelineRequest withUntilId(String untilId) {
            this.untilId = untilId;
            return this;
        }

        public TimelineRequest withChannel(String channelId) {
            this.channelId = channelId;
            return this;
        }
    }
}
