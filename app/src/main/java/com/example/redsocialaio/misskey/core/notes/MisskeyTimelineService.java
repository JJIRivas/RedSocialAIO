package com.example.redsocialaio.misskey.core.notes;

import android.content.Context;
import android.util.Log;

import com.example.redsocialaio.misskey.core.notes.MisskeyNoteTimeline;
import com.example.redsocialaio.misskey.userInfo.MisskeyAccountService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            return failFuture(future, "No autenticado");
        }

        accountService.getAccountWithToken(user.getUid())
                .addOnSuccessListener(result -> {
                    try {
                        JSONObject body = buildRequestBody(request, result.getToken());

                        String url = "https://" + result.getInstanceUrl() + "/api/notes/local-timeline";
                        Request httpRequest = new Request.Builder()
                                .url(url)
                                .post(RequestBody.create(body.toString(), JSON))
                                .build();

                        Log.d("MisskeyTimelineService", "Token: " + result.getToken());
                        Log.d("MisskeyTimelineService", "Instance: " + result.getInstanceUrl());
                        Log.d("MisskeyTimelineService", "Request: " + body);

                        client.newCall(httpRequest).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.e("TIMELINEREQUEST", "ERROR GETTING TIMELINE", e);
                                future.completeExceptionally(e);
                            }

                            @Override
                            public void onResponse(Call call, Response response) {
                                handleTimelineResponse(response, future);
                            }
                        });

                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }


    private CompletableFuture<List<MisskeyNoteTimeline>> failFuture(CompletableFuture<List<MisskeyNoteTimeline>> future, String message) {
        future.completeExceptionally(new Exception(message));
        return future;
    }

    private JSONObject buildRequestBody(TimelineRequest request, String token) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("i", token);
        body.put("limit", request.limit);

        if (request.sinceId != null) body.put("sinceId", request.sinceId);
        if (request.untilId != null) body.put("untilId", request.untilId);
        if (request.sinceDate > 0) body.put("sinceDate", request.sinceDate);
        if (request.untilDate > 0) body.put("untilDate", request.untilDate);
        if (request.channelId != null) body.put("channelId", request.channelId);

        body.put("allowPartial", request.allowPartial);
        return body;
    }

    private void handleTimelineResponse(Response response, CompletableFuture<List<MisskeyNoteTimeline>> future) {
        try (ResponseBody responseBody = response.body()) {
            if (responseBody == null) {
                future.completeExceptionally(new IOException("Cuerpo de respuesta nulo"));
                return;
            }

            String json = responseBody.string();
            Log.d("MisskeyTimelineService", "HTTP Response: " + response.code());
            Log.d("MisskeyTimelineService", "Body: " + json);

            JSONArray notesArray = new JSONArray(json);
            List<MisskeyNoteTimeline> notes = new ArrayList<>();

            for (int i = 0; i < notesArray.length(); i++) {
                notes.add(MisskeyNoteTimeline.fromJSON(notesArray.getJSONObject(i)));
            }

            future.complete(notes);

        } catch (Exception e) {
            future.completeExceptionally(e);
        }
    }


    /**
     * Clase para parámetros del timeline
     */
    public static class TimelineRequest {
        public String channelId = null;
        public int limit = 20;
        public String sinceId = null;
        public String untilId = null;
        public long sinceDate = 0;
        public long untilDate = 0;
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
